package software.ulpgc.code

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import software.ulpgc.code.application.ui.App

fun main() = application {

    fun cleanup() {

    }

    Window(
        onCloseRequest = {
            cleanup()
            exitApplication()
        },
        title = "Proyecto_PS",
    ) {
        App(JavaDatabaseDriverFactory())
    }
}