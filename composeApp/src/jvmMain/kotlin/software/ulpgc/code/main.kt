package software.ulpgc.code

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.asCoroutineDispatcher
import software.ulpgc.code.application.ui.App
import software.ulpgc.code.architecture.control.coroutines.CoroutineManager
import software.ulpgc.code.architecture.io.Store
import java.util.concurrent.Executors

fun main() = application {
    Window(
        onCloseRequest = {
            CoroutineManager.dispose()
            exitApplication()
        },
        title = "Proyecto_PS",
    ) {
        App(
            JavaDatabaseDriverFactory()
        )
    }
}