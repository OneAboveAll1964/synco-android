package app.synco.remote

class RemotePointer(x: Double = HALF, y: Double = HALF) {

    var x: Double = x.coerceIn(0.0, 1.0)
        private set

    var y: Double = y.coerceIn(0.0, 1.0)
        private set

    fun moveBy(deltaX: Double, deltaY: Double) {
        x = (x + deltaX).coerceIn(0.0, 1.0)
        y = (y + deltaY).coerceIn(0.0, 1.0)
    }

    fun moveTo(newX: Double, newY: Double) {
        x = newX.coerceIn(0.0, 1.0)
        y = newY.coerceIn(0.0, 1.0)
    }

    fun center() {
        x = HALF
        y = HALF
    }

    private companion object {
        const val HALF = 0.5
    }
}
