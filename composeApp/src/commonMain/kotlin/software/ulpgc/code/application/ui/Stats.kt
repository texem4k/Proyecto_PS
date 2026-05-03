package software.ulpgc.code.application.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import software.ulpgc.code.architecture.io.Storage

@Composable
fun menuTareas(store: Storage) {
    Row(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier.fillMaxWidth(0.33f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Tareas completadas")
        }

        Column(
            modifier = Modifier.fillMaxWidth(0.33f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Tareas próxima")
        }

        Column(
            modifier = Modifier.fillMaxWidth(0.33f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Recomendación de tareas")
        }
    }
}