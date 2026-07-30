package app.synco.service

import android.content.Context
import android.net.wifi.WifiManager
import android.os.PowerManager

internal class SyncoRuntimeLocks(context: Context) {

    private val application = context.applicationContext

    private val wakeLock: PowerManager.WakeLock? = application
        .getSystemService(PowerManager::class.java)
        ?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG)
        ?.apply { setReferenceCounted(false) }

    private val multicastLock: WifiManager.MulticastLock? = application
        .getSystemService(WifiManager::class.java)
        ?.createMulticastLock(MULTICAST_LOCK_TAG)
        ?.apply { setReferenceCounted(false) }

    fun acquire() {
        wakeLock?.takeUnless { it.isHeld }?.acquire()
        multicastLock?.takeUnless { it.isHeld }?.acquire()
    }

    fun release() {
        multicastLock?.takeIf { it.isHeld }?.release()
        wakeLock?.takeIf { it.isHeld }?.release()
    }

    private companion object {
        const val WAKE_LOCK_TAG = "synco:clipboard-sync"
        const val MULTICAST_LOCK_TAG = "synco:mdns"
    }
}
