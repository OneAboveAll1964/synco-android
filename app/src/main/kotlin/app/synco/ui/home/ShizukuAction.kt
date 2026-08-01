package app.synco.ui.home

import android.content.Intent
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import app.synco.R
import app.synco.shizuku.ShizukuAvailability
import app.synco.shizuku.ShizukuPermission
import app.synco.shizuku.ShizukuState

@Composable
fun ShizukuAction(state: ShizukuState) {
    val context = LocalContext.current
    when (state) {
        ShizukuState.PERMISSION_DENIED -> TextButton(
            onClick = {
                ShizukuAvailability(context).requestPermission(ShizukuPermission.REQUEST_CODE)
            },
        ) {
            Text(text = stringResource(R.string.shizuku_grant))
        }

        ShizukuState.NOT_RUNNING -> TextButton(
            onClick = {
                val launch = context.packageManager.getLaunchIntentForPackage(SHIZUKU_PACKAGE)
                    ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (launch != null) runCatching { context.startActivity(launch) }
            },
        ) {
            Text(text = stringResource(R.string.shizuku_open))
        }

        ShizukuState.NOT_INSTALLED -> TextButton(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(SHIZUKU_SITE))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                runCatching { context.startActivity(intent) }
            },
        ) {
            Text(text = stringResource(R.string.shizuku_get))
        }

        ShizukuState.READY -> Unit
    }
}

private const val SHIZUKU_PACKAGE = "moe.shizuku.privileged.api"
private const val SHIZUKU_SITE = "https://shizuku.rikka.app"
