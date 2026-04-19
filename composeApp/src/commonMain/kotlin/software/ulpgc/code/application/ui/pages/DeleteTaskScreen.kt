package software.ulpgc.code.application.ui.pages

import UpcomingTasksPanel
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import software.ulpgc.code.application.ui.Headers
import software.ulpgc.code.application.ui.Screen
import software.ulpgc.code.architecture.io.Storage

@Composable
fun DeleteTaskScreen(onNavigate: (Screen) -> Unit, store: Storage,  onDeleted: () -> Unit = {}) {

    Column (modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Headers(onNavigate, "Eliminar tarea")
        Box(modifier = Modifier.weight(0.5f)) {
            UpcomingTasksPanel(store, title = "Cual eliminamos", total = true, onDeleted = onDeleted)
        }
    }
}