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
import software.ulpgc.code.application.ui.Screen
import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.tasks.Task

@Composable
fun DeleteTaskScreen(onNavigate: (Screen) -> Unit, store: Storage) {

    Column (modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Text(
                text ="Eliminar tarea",
                modifier = Modifier.align(Alignment.Center)
            )
            Button(
                modifier = Modifier.align(Alignment.CenterEnd),
                onClick = { onNavigate(Screen.HOME) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            ) {
                Text("✖\uFE0E")
            }
        }
        Box(modifier = Modifier.weight(0.5f)) {
            UpcomingTasksPanel(store, "Cual eliminamos", true)
        }
    }
}
