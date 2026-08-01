package app.synco.shizuku

import android.os.IBinder
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper
import java.lang.reflect.Method

internal object ClipboardService {

    const val SHELL_PACKAGE = "com.android.shell"

    fun connect(): Any? {
        HiddenApi.open()
        val raw: IBinder = SystemServiceHelper.getSystemService(SERVICE) ?: return null
        val stub = Class.forName("android.content.IClipboard\$Stub")
        return stub.getMethod("asInterface", IBinder::class.java)
            .invoke(null, ShizukuBinderWrapper(raw))
    }

    fun methodNamed(service: Any, name: String): Method? =
        service.javaClass.methods.firstOrNull { it.name == name }

    fun argumentsFor(method: Method, listener: Any? = null): Array<Any?> {
        var stringsSeen = 0
        return method.parameterTypes.map { type ->
            when {
                listener != null && type.isInstance(listener) -> listener
                type == String::class.java -> {
                    stringsSeen += 1
                    if (stringsSeen == 1) SHELL_PACKAGE else null
                }

                type == Int::class.javaPrimitiveType -> 0
                else -> null
            }
        }.toTypedArray()
    }

    private const val SERVICE = "clipboard"
}
