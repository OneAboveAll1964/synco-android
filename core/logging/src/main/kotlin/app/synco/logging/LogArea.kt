package app.synco.logging

import android.util.Log

class LogArea(private val tag: String) {

    fun debug(message: () -> String) {
        if (Log.isLoggable(tag, Log.DEBUG)) Log.d(tag, message())
    }

    fun info(message: String) {
        Log.i(tag, message)
    }

    fun warn(message: String) {
        Log.w(tag, message)
    }

    fun warn(message: String, cause: Throwable) {
        Log.w(tag, "$message: ${Failures.describe(cause)}")
    }

    fun error(message: String, cause: Throwable) {
        Log.e(tag, "$message: ${Failures.describe(cause)}", cause)
    }
}
