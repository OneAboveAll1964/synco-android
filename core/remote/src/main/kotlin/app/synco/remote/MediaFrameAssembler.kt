package app.synco.remote

import app.synco.protocol.framing.MediaFrame

class MediaFrameAssembler {

    data class AccessUnit(
        val data: ByteArray,
        val ptsMicros: Long,
        val isKeyframe: Boolean,
        val isConfig: Boolean,
    )

    private var currentSeq: Long = -1
    private val buffer = ArrayList<ByteArray>()
    private var bufferedBytes = 0
    private var keyframe = false
    private var config = false
    private var ptsMicros = 0L

    fun accept(frame: MediaFrame): AccessUnit? {
        if (frame.seq != currentSeq) {
            reset()
            currentSeq = frame.seq
        }
        buffer += frame.data
        bufferedBytes += frame.data.size
        keyframe = keyframe || frame.isKeyframe
        config = config || frame.isConfig
        ptsMicros = frame.ptsMicros
        if (!frame.isLastFragment) return null
        val assembled = combine()
        val unit = AccessUnit(assembled, ptsMicros, keyframe, config)
        reset()
        currentSeq = frame.seq
        return unit
    }

    private fun combine(): ByteArray {
        val out = ByteArray(bufferedBytes)
        var offset = 0
        for (chunk in buffer) {
            chunk.copyInto(out, offset)
            offset += chunk.size
        }
        return out
    }

    private fun reset() {
        buffer.clear()
        bufferedBytes = 0
        keyframe = false
        config = false
    }
}
