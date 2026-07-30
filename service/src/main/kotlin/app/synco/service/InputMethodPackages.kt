package app.synco.service

import android.content.Context
import android.provider.Settings
import android.view.inputmethod.InputMethodManager

internal object InputMethodPackages {

    fun of(context: Context): Set<String> = buildSet {
        defaultInputMethod(context)?.let(::add)
        addAll(enabledInputMethods(context))
        add(SYSTEM_UI)
    }

    private fun defaultInputMethod(context: Context): String? = runCatching {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
            ?.substringBefore('/')
            ?.takeIf { it.isNotBlank() }
    }.getOrNull()

    private fun enabledInputMethods(context: Context): List<String> = runCatching {
        context.getSystemService(InputMethodManager::class.java)
            ?.enabledInputMethodList
            ?.map { it.packageName }
            .orEmpty()
    }.getOrDefault(emptyList())

    private const val SYSTEM_UI = "com.android.systemui"
}
