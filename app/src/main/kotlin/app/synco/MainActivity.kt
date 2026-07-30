package app.synco

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.synco.service.syncoGraphOrNull
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
    }

    override fun onResume() {
        super.onResume()
        syncoGraphOrNull()?.commands?.refreshClipboard()
    }
}
