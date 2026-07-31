package app.synco.shizuku

import android.content.ClipData
import android.os.IBinder
import app.synco.logging.SyncoLog
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper
import java.lang.reflect.Method

class ShizukuClipboard {

    fun primaryClip(): ClipData? = runCatching { readThroughShell() }
        .onFailure { SyncoLog.clipboard.warn("Shizuku could not read the clipboard", it) }
        .getOrNull()

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
