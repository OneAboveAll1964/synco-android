package app.synco.sync

import android.view.Surface
import app.synco.protocol.DeviceId
import app.synco.protocol.framing.MediaFrame
import app.synco.protocol.message.RemoteInputEvent
import app.synco.protocol.message.RemoteStart
import app.synco.remote.H264Decoder
import app.synco.remote.MediaFrameAssembler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RemoteController(
    private val scope: CoroutineScope,
) : RemoteSignals, RemoteMediaSink {

    @Volatile
    private var engine: SyncEngine? = null

    fun bind(engine: SyncEngine) {
        this.engine = engine
    }

    private val stateFlow = MutableStateFlow<RemoteState>(RemoteState.Idle)
    val state: StateFlow<RemoteState> = stateFlow.asStateFlow()

    private val assembler = MediaFrameAssembler()

    @Volatile
    private var decoder: H264Decoder? = null

    @Volatile
    private var surface: Surface? = null

    @Volatile
    private var deviceId: DeviceId? = null

    fun connect(deviceId: DeviceId, maxWidth: Int, maxHeight: Int, fps: Int) {
        this.deviceId = deviceId
        stateFlow.value = RemoteState.Requesting(deviceId)
        val target = engine ?: return
        scope.launch {
            target.startRemote(deviceId, RemoteStart(maxWidth, maxHeight, fps, input = true))
        }
    }

    fun attachSurface(surface: Surface) {
        this.surface = surface
        startDecoderIfReady()
    }

    fun detachSurface() {
        surface = null
        decoder?.stop()
        decoder = null
    }

    fun disconnect() {
        val target = deviceId
        deviceId = null
        decoder?.stop()
        decoder = null
        stateFlow.value = RemoteState.Idle
        val active = engine
        if (target != null && active != null) scope.launch { active.stopRemote(target) }
    }

    fun sendInput(events: List<RemoteInputEvent>) {
        val target = deviceId ?: return
        val active = engine ?: return
        scope.launch { active.sendRemoteInput(target, events) }
    }

    override fun onRemoteAccepted(width: Int, height: Int, input: Boolean) {
        val target = deviceId ?: return
        stateFlow.value = RemoteState.Streaming(target, width, height, input)
        startDecoderIfReady()
    }

    override fun onRemoteRejected(reason: String) {
        decoder?.stop()
        decoder = null
        stateFlow.value = RemoteState.Rejected(reason)
    }

    override fun onRemoteStopped() {
        decoder?.stop()
        decoder = null
        if (stateFlow.value !is RemoteState.Rejected) stateFlow.value = RemoteState.Idle
    }

    override fun onMediaFrame(frame: MediaFrame) {
        val unit = assembler.accept(frame) ?: return
        decoder?.submit(unit)
    }

    private fun startDecoderIfReady() {
        val streaming = stateFlow.value as? RemoteState.Streaming ?: return
        val target = surface ?: return
        if (decoder != null) return
        decoder = H264Decoder(streaming.width, streaming.height, target).also { it.start() }
    }
}
