package app.synco.remote

import android.media.MediaCodec
import android.media.MediaFormat
import android.view.Surface
import app.synco.logging.SyncoLog
import java.nio.ByteBuffer

class H264Decoder(
    private val width: Int,
    private val height: Int,
    private val surface: Surface,
) {
    private var codec: MediaCodec? = null
    private var started = false
    private var configured = false
    private var pendingConfig: ByteArray? = null

    fun start() {
        val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
            setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, width * height)
        }
        codec = runCatching {
            MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC).also {
                it.configure(format, surface, null, 0)
                it.start()
            }
        }.onFailure { SyncoLog.transfer.warn("could not start the H264 decoder", it) }.getOrNull()
        started = codec != null
    }

    fun submit(unit: MediaFrameAssembler.AccessUnit) {
        val codec = codec ?: return
        if (!started) return
        if (unit.isConfig) {
            pendingConfig = unit.data
            configured = false
        }
        val payload = if (!configured && !unit.isConfig) {
            configured = true
            (pendingConfig ?: ByteArray(0)) + unit.data
        } else {
            unit.data
        }
        val flags = if (unit.isConfig) MediaCodec.BUFFER_FLAG_CODEC_CONFIG else 0
        if (!queue(codec, payload, unit.ptsMicros, flags)) {
            SyncoLog.transfer.warn("the decoder ran out of input buffers and lost a picture")
        }
        drain(codec)
    }

    private fun queue(codec: MediaCodec, payload: ByteArray, ptsMicros: Long, flags: Int): Boolean {
        repeat(QUEUE_ATTEMPTS) {
            drain(codec)
            val index = runCatching { codec.dequeueInputBuffer(DEQUEUE_TIMEOUT_US) }.getOrDefault(-1)
            if (index >= 0) {
                val input: ByteBuffer = codec.getInputBuffer(index) ?: return false
                input.clear()
                input.put(payload)
                codec.queueInputBuffer(index, 0, payload.size, ptsMicros, flags)
                return true
            }
        }
        return false
    }

    private fun drain(codec: MediaCodec) {
        val info = MediaCodec.BufferInfo()
        while (true) {
            val index = runCatching { codec.dequeueOutputBuffer(info, 0) }.getOrDefault(-1)
            if (index < 0) break
            codec.releaseOutputBuffer(index, true)
        }
    }

    fun stop() {
        started = false
        runCatching { codec?.stop() }
        runCatching { codec?.release() }
        codec = null
    }

    private companion object {
        const val DEQUEUE_TIMEOUT_US = 10_000L
        const val QUEUE_ATTEMPTS = 3
    }
}
