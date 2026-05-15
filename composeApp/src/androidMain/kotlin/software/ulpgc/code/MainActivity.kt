package software.ulpgc.code

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import software.ulpgc.code.application.ui.App
import com.mmk.kmpnotifier.notification.NotifierManager
import com.mmk.kmpnotifier.notification.configuration.NotificationPlatformConfiguration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        NotifierManager.initialize(
            NotificationPlatformConfiguration.Android(
                notificationIconResId = android.R.drawable.ic_dialog_info,
                showPushNotification = true
            )
        )

        setContent {
            App(AndroidDatabaseDriverFactory(this@MainActivity))
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
}