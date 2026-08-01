package app.synco.shizuku

import android.content.ClipData
import android.os.IBinder
import app.synco.logging.SyncoLog
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

class ShizukuClipboard {

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
        val service = clipboardService() ?: return null
        val method = service.javaClass.methods.firstOrNull { it.name == GET_PRIMARY_CLIP } ?: return null
        return method.invoke(service, *argumentsFor(method)) as? ClipData
    }

    private fun clipboardService(): Any? {
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
