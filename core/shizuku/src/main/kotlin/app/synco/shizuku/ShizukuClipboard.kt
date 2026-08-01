package app.synco.shizuku

import android.content.ClipData
import app.synco.logging.SyncoLog
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

class ShizukuClipboard {

    @Volatile
    private var described = false

    fun read(): ShizukuRead = try {
        ShizukuRead.Clip(readThroughShell())
    } catch (denied: SecurityException) {
        SyncoLog.clipboard.warn("Shizuku refused the clipboard call", denied)
        ShizukuRead.Denied
    } catch (invocation: InvocationTargetException) {
        classify(invocation.targetException ?: invocation)
    } catch (failure: Throwable) {
        classify(failure)
    }

    fun primaryClip(): ClipData? = (read() as? ShizukuRead.Clip)?.data

    private fun classify(failure: Throwable): ShizukuRead {
        if (failure is SecurityException) {
            SyncoLog.clipboard.warn("Shizuku refused the clipboard call", failure)
            return ShizukuRead.Denied
        }
        SyncoLog.clipboard.warn("Shizuku could not read the clipboard", failure)
        return ShizukuRead.Unavailable
    }

    private fun readThroughShell(): ClipData? {
        val service = ClipboardService.connect() ?: run {
            SyncoLog.clipboard.warn("Shizuku could not reach the clipboard service")
            return null
        }
        val method = ClipboardService.methodNamed(service, GET_PRIMARY_CLIP) ?: run {
            SyncoLog.clipboard.warn("no $GET_PRIMARY_CLIP on ${service.javaClass.name}")
            return null
        }
        val arguments = ClipboardService.argumentsFor(method)
        describeOnce(method, arguments)
        return method.invoke(service, *arguments) as? ClipData
    }

    private fun describeOnce(method: Method, arguments: Array<Any?>) {
        if (described) return
        described = true
        val shape = method.parameterTypes.joinToString(", ") { it.simpleName }
        SyncoLog.clipboard.info("Shizuku calls $GET_PRIMARY_CLIP($shape) with ${arguments.toList()}")
    }

    private companion object {
        const val GET_PRIMARY_CLIP = "getPrimaryClip"
    }
}
