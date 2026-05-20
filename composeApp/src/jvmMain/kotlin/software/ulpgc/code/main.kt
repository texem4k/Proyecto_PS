package software.ulpgc.code

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.mmk.kmpnotifier.notification.NotifierManager
import com.mmk.kmpnotifier.notification.configuration.NotificationPlatformConfiguration
import software.ulpgc.code.application.io.cloudDB.SupabaseProvider
import software.ulpgc.code.application.ui.App
import software.ulpgc.code.architecture.control.coroutines.CoroutineManager
import software.ulpgc.code.architecture.control.logs.LogMaster


fun main() = application {
    LogMaster.startLogger(JavaLogStorer())
    NotifierManager.initialize(
        NotificationPlatformConfiguration.Desktop(
            showPushNotification = true,
            notificationIconPath = null
        )
    )
    NotifierManager.setLogger { message ->
        LogMaster.log("**KMPNotifier** $message")
    }

    val windowState = rememberWindowState()

    Window(
        onCloseRequest = {
            CoroutineManager.dispose {
                exitApplication()
            }
        },
        title = "Proyecto_PS",
        undecorated = true,
        state = windowState,
    ) {
        App(
            databaseDriverFactory = JavaDatabaseDriverFactory(),
            titleBar = {
                AppTitleBar(
                    windowState = windowState,
                    onClose = {
                        CoroutineManager.dispose {
                            exitApplication()
                        }
                    }
                )
            }
        )
    }
}