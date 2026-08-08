package app.synco.remote

import app.synco.protocol.message.RemoteInputEvent

object RemoteKeystrokes {

    fun key(usage: Int, mods: Int = 0): List<RemoteInputEvent> = listOf(
        RemoteInputEvent(kind = RemoteInputEvent.KEY, code = usage, mods = mods, down = true),
        RemoteInputEvent(kind = RemoteInputEvent.KEY, code = usage, mods = mods, down = false),
    )

    fun edits(previous: String, current: String, mods: Int = 0): List<RemoteInputEvent> {
        val shared = sharedPrefix(previous, current)
        val events = mutableListOf<RemoteInputEvent>()
        repeat(previous.length - shared) { events += key(HidKeyboard.USAGE_BACKSPACE) }
        events += typed(current.substring(shared), mods)
        return events
    }

    private fun typed(value: String, mods: Int): List<RemoteInputEvent> {
        if (value.isEmpty()) return emptyList()
        if (mods != 0) return value.flatMap { char -> usageFor(char)?.let { key(it, mods) }.orEmpty() }
        val events = mutableListOf<RemoteInputEvent>()
        val buffer = StringBuilder()
        value.forEach { char ->
            if (char == '\n') {
                flush(buffer, events)
                events += key(HidKeyboard.USAGE_ENTER)
            } else {
                buffer.append(char)
            }
        }
        flush(buffer, events)
        return events
    }

    private fun flush(buffer: StringBuilder, into: MutableList<RemoteInputEvent>) {
        if (buffer.isEmpty()) return
        into += RemoteInputEvent(kind = RemoteInputEvent.TEXT, text = buffer.toString())
        buffer.clear()
    }

    private fun usageFor(char: Char): Int? =
        HidKeyboard.usageForLetter(char) ?: HidKeyboard.usageForDigit(char)

    private fun sharedPrefix(previous: String, current: String): Int {
        val limit = minOf(previous.length, current.length)
        var index = 0
        while (index < limit && previous[index] == current[index]) index++
        return index
    }
}
