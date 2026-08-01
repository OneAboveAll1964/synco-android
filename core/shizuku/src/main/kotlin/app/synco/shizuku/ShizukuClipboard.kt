package app.synco.shizuku

import android.content.ClipData
import android.os.IBinder
import app.synco.logging.SyncoLog
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper
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
        val service = clipboardService() ?: run {
            SyncoLog.clipboard.warn("Shizuku could not reach the clipboard service")
            return null
        }
        val method = service.javaClass.methods.firstOrNull { it.name == GET_PRIMARY_CLIP } ?: run {
            if (!described) {
                described = true
                val names = service.javaClass.methods
                    .filter { it.name.contains("lip", ignoreCase = true) }
                    .joinToString(", ") { m ->
                        m.name + "(" + m.parameterTypes.joinToString(",") { it.simpleName } + ")"
                    }
                SyncoLog.clipboard.warn(
                    "no $GET_PRIMARY_CLIP on ${service.javaClass.name}; clip methods: $names",
                )
            }
            return null
        }
        val arguments = argumentsFor(method)
        describeOnce(method, arguments)
        return method.invoke(service, *arguments) as? ClipData
    }

    private fun describeOnce(method: Method, arguments: Array<Any?>) {
        if (described) return
        described = true
        val shape = method.parameterTypes.joinToString(", ") { it.simpleName }
        SyncoLog.clipboard.info("Shizuku calls $GET_PRIMARY_CLIP($shape) with ${arguments.toList()}")
    }

    private fun clipboardService(): Any? {
        HiddenApi.open()
        val raw: IBinder = SystemServiceHelper.getSystemService(CLIPBOARD_SERVICE) ?: return null
        val wrapped = ShizukuBinderWrapper(raw)
        val stub = Class.forName("android.content.IClipboard\$Stub")
        return stub.getMethod("asInterface", IBinder::class.java).invoke(null, wrapped)
    }

    private fun argumentsFor(method: Method): Array<Any?> {
        var stringsSeen = 0
        return method.parameterTypes.map { type ->
            when {
                type == String::class.java -> {
                    stringsSeen += 1
                    if (stringsSeen == 1) SHELL_PACKAGE else null
                }
                type == Int::class.javaPrimitiveType -> 0
                else -> null
            }
        }.toTypedArray()
    }

    private companion object {
        const val CLIPBOARD_SERVICE = "clipboard"
        const val GET_PRIMARY_CLIP = "getPrimaryClip"
        const val SHELL_PACKAGE = "com.android.shell"
    }
}
