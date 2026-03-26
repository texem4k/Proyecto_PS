package software.ulpgc.code

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Proyecto_PS",
    ) {
        App()
    }
}