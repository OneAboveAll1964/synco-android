package app.synco.remote

data class Touch(val id: Int, val x: Double, val y: Double)

enum class TouchPhase { START, MOVE, END, CANCEL }
