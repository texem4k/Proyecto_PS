package software.ulpgc.code.application.ui.pages

import Screen
import UpcomingTasksPanel
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import software.ulpgc.code.application.ui.Headers
import software.ulpgc.code.architecture.io.Storage

@Composable
fun DeleteTaskScreen(onNavigate: (Screen) -> Unit, store: Storage,  onDeleted: () -> Unit = {}) {

    Column (modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Headers("Eliminar una tarea", onClose = { false } )
        Box(modifier = Modifier.weight(0.5f)) {
            UpcomingTasksPanel(store, title = "Cual eliminamos", total = true, onDeleted = onDeleted, screen = Screen.HOME)
        }
    }
}
