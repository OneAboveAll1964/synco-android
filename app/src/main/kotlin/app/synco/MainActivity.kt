package app.synco

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.synco.service.syncoGraphOrNull
import app.synco.sync.QRPairPayload
import app.synco.ui.SyncoApp
import app.synco.ui.theme.SyncoTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            SyncoTheme {
                SyncoApp()
            }
        }
        adoptPairingLink(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        adoptPairingLink(intent)
    }

    override fun onResume() {
        super.onResume()
        syncoGraphOrNull()?.commands?.refreshClipboard()
    }

    private fun adoptPairingLink(intent: Intent?) {
        if (intent?.action != Intent.ACTION_VIEW) return
        val text = intent.dataString ?: return
        intent.data = null
        val payload = QRPairPayload.parse(text)
        if (payload == null) {
            Toast.makeText(this, R.string.qr_pair_bad_code, Toast.LENGTH_SHORT).show()
            return
        }
        syncoGraphOrNull()?.commands?.adoptQRPeer(payload)
        Toast.makeText(
            this,
            getString(R.string.qr_pair_done, payload.displayName),
            Toast.LENGTH_SHORT,
        ).show()
    }
}
