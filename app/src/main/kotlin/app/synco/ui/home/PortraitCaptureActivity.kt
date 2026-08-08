package app.synco.ui.home

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.google.zxing.client.android.R as ZxingR
import com.journeyapps.barcodescanner.CaptureActivity

class PortraitCaptureActivity : CaptureActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            window.isStatusBarContrastEnforced = false
        }
        super.onCreate(savedInstanceState)
        liftStatusAboveSystemBars()
    }

    private fun liftStatusAboveSystemBars() {
        val status = findViewById<TextView>(ZxingR.id.zxing_status_view) ?: return
        val basePadding = status.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(status) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(bottom = basePadding + bars.bottom)
            insets
        }
        ViewCompat.requestApplyInsets(status)
    }
}
