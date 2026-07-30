package app.synco.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

internal class EncryptedIdentitySecrets(context: Context) : IdentitySecrets {

    private val context: Context = context.applicationContext

    @Volatile
    private var opened: SharedPreferences? = null

    override fun read(): String? = preferences().getString(PRIVATE_KEY, null)

    override fun write(value: String) {
        val committed = preferences().edit().putString(PRIVATE_KEY, value).commit()
        check(committed) { "the secure identity store refused the write" }
    }

    private fun preferences(): SharedPreferences = opened ?: open().also { opened = it }

    private fun open(): SharedPreferences = EncryptedSharedPreferences.create(
        context,
        FILE_NAME,
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    private companion object {
        const val FILE_NAME = "synco.identity"
        const val PRIVATE_KEY = "static_private_key"
    }
}
