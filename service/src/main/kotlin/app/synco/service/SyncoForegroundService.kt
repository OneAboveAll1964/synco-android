package app.synco.service

import android.content.Intent
import android.content.pm.ServiceInfo
import androidx.core.app.ServiceCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import app.synco.logging.SyncoLog
import app.synco.shizuku.ShizukuAvailability
import app.synco.shizuku.ShizukuClipboard
import app.synco.sync.SyncState
import kotlinx.coroutines.launch

class SyncoForegroundService : LifecycleService() {

    private val graph by lazy { requireSyncoGraph() }

    private val notifications by lazy { SyncoNotifications(this) }

    private val transferNotifications by lazy { TransferNotifications(this) }

    private val locks by lazy { SyncoRuntimeLocks(this) }

    private val watcher by lazy { ClipboardChangeWatcher(this, lifecycleScope) }

    private val shizuku by lazy {
        ShizukuCaptureLoop(
            availability = ShizukuAvailability(this),
            clipboard = ShizukuClipboard(),
            capture = graph.clipboard,
            tuning = graph.captureTuning,
        )
    }

    @Volatile
    private var syncing = false

    override fun onCreate() {
        super.onCreate()
        notifications.createChannel()
        transferNotifications.createChannel()
        lifecycleScope.launch { graph.state.collect(::publish) }
        watcher.start()
        lifecycleScope.launch { graph.transferProgress.collect(transferNotifications::publish) }
        lifecycleScope.launch { shizuku.run() }
        lifecycleScope.launch { ShizukuStatus.publish(shizuku.state) }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        enterForeground()
        when (intent?.action) {
            SyncoServiceActions.STOP -> shutDown()
            SyncoServiceActions.PAUSE -> setPaused(intent)
            else -> runSync()
        }
        return START_STICKY
    }

    override fun onTimeout(startId: Int, fgsType: Int) {
        shutDown()
    }

    override fun onDestroy() {
        transferNotifications.dismissAll()
        watcher.stop()
        syncing = false
        locks.release()
        super.onDestroy()
    }

    private fun enterForeground() {
        ServiceCompat.startForeground(
            this,
            SyncoNotifications.NOTIFICATION_ID,
            notifications.build(graph.state.value),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
        )
    }

    private fun runSync() {
        if (syncing) return
        syncing = true
        locks.acquire()
        graph.commands.start()
    }

    private fun setPaused(intent: Intent) {
        graph.commands.setPaused(intent.getBooleanExtra(SyncoServiceActions.EXTRA_PAUSED, true))
    }

    private fun shutDown() {
        syncing = false
        graph.commands.stop()
        locks.release()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun publish(state: SyncState) {
        if (syncing) notifications.update(state)
    }
}
