package app.synco.remote

import app.synco.protocol.message.RemoteInputEvent
import kotlin.math.abs
import kotlin.math.hypot

class TrackpadTranslator(
    private val viewWidth: Double,
    private val viewHeight: Double,
    private val pointer: RemotePointer = RemotePointer(),
    private val moveScale: Double = DEFAULT_MOVE_SCALE,
    private val scrollScale: Double = DEFAULT_SCROLL_SCALE,
    private val tapSlopPx: Double = DEFAULT_TAP_SLOP_PX,
    private val tapTimeoutMillis: Long = DEFAULT_TAP_TIMEOUT_MILLIS,
) {
    private var last: List<Touch> = emptyList()
    private var startAtMillis = 0L
    private var startCentroid: Pair<Double, Double>? = null
    private var maxPointers = 0
    private var movedBeyondSlop = false
    private var lastSpread: Double? = null

    fun onTouch(phase: TouchPhase, pointers: List<Touch>, atMillis: Long): List<RemoteInputEvent> =
        when (phase) {
            TouchPhase.START -> onStart(pointers, atMillis)
            TouchPhase.MOVE -> onMove(pointers)
            TouchPhase.END -> onEnd(atMillis)
            TouchPhase.CANCEL -> reset().let { emptyList() }
        }

    private fun onStart(pointers: List<Touch>, atMillis: Long): List<RemoteInputEvent> {
        if (last.isEmpty()) {
            startAtMillis = atMillis
            startCentroid = centroid(pointers)
            movedBeyondSlop = false
            maxPointers = 0
        }
        maxPointers = maxOf(maxPointers, pointers.size)
        last = pointers
        lastSpread = spread(pointers)
        return emptyList()
    }

    private fun onMove(pointers: List<Touch>): List<RemoteInputEvent> {
        maxPointers = maxOf(maxPointers, pointers.size)
        if (!movedBeyondSlop) {
            val origin = startCentroid
            val now = centroid(pointers)
            if (origin != null && now != null &&
                hypot(now.first - origin.first, now.second - origin.second) > tapSlopPx
            ) {
                movedBeyondSlop = true
            }
        }
        val events = when (pointers.size) {
            1 -> pointerMove(pointers)
            2 -> twoFinger(pointers)
            else -> emptyList()
        }
        last = pointers
        lastSpread = spread(pointers)
        return events
    }

    private fun onEnd(atMillis: Long): List<RemoteInputEvent> {
        val quick = atMillis - startAtMillis <= tapTimeoutMillis
        val tapped = quick && !movedBeyondSlop
        val events = when {
            tapped && maxPointers == 1 -> click(RemoteButtons.LEFT)
            tapped && maxPointers == 2 -> click(RemoteButtons.RIGHT)
            else -> emptyList()
        }
        reset()
        return events
    }

    private fun pointerMove(pointers: List<Touch>): List<RemoteInputEvent> {
        val previous = last.firstOrNull() ?: return emptyList()
        val current = pointers.first()
        pointer.moveBy(
            (current.x - previous.x) / viewWidth * moveScale,
            (current.y - previous.y) / viewHeight * moveScale,
        )
        return listOf(absolutePointer())
    }

    private fun twoFinger(pointers: List<Touch>): List<RemoteInputEvent> {
        val previous = centroid(last) ?: return emptyList()
        val now = centroid(pointers) ?: return emptyList()
        val events = mutableListOf<RemoteInputEvent>()
        val nowSpread = spread(pointers)
        val wasSpread = lastSpread
        if (wasSpread != null && nowSpread != null && wasSpread > 0.0 &&
            abs(nowSpread - wasSpread) > abs(now.second - previous.second)
        ) {
            events += RemoteInputEvent(kind = RemoteInputEvent.MAGNIFY, scale = nowSpread / wasSpread)
        } else {
            events += RemoteInputEvent(
                kind = RemoteInputEvent.SCROLL,
                dx = (now.first - previous.first) * scrollScale,
                dy = (now.second - previous.second) * scrollScale,
            )
        }
        return events
    }

    private fun click(button: String): List<RemoteInputEvent> = listOf(
        absolutePointer(),
        RemoteInputEvent(kind = RemoteInputEvent.BUTTON, button = button, down = true),
        RemoteInputEvent(kind = RemoteInputEvent.BUTTON, button = button, down = false),
    )

    private fun absolutePointer(): RemoteInputEvent =
        RemoteInputEvent(kind = RemoteInputEvent.POINTER_ABSOLUTE, x = pointer.x, y = pointer.y)

    private fun centroid(pointers: List<Touch>): Pair<Double, Double>? {
        if (pointers.isEmpty()) return null
        return pointers.sumOf { it.x } / pointers.size to pointers.sumOf { it.y } / pointers.size
    }

    private fun spread(pointers: List<Touch>): Double? {
        if (pointers.size < 2) return null
        val a = pointers[0]
        val b = pointers[1]
        return hypot(a.x - b.x, a.y - b.y)
    }

    private fun reset() {
        last = emptyList()
        startCentroid = null
        maxPointers = 0
        movedBeyondSlop = false
        lastSpread = null
    }

    companion object {
        const val DEFAULT_MOVE_SCALE = 1.4
        const val DEFAULT_SCROLL_SCALE = 1.0
        const val DEFAULT_TAP_SLOP_PX = 16.0
        const val DEFAULT_TAP_TIMEOUT_MILLIS = 220L
    }
}

object RemoteButtons {
    const val LEFT = "left"
    const val RIGHT = "right"
    const val MIDDLE = "middle"
}
