package software.ulpgc.code

import UpcomingTasksPanel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import software.ulpgc.code.architecture.model.Task

@Composable
fun HomeScreen() {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {

        SearchBar()


        Spacer(modifier = Modifier.height(80.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            UpcomingTasksPanel(listOf<Task>(
                Task("Estudiar PS", "Estudios", "asasas", 1),
                Task("Hacer proyecto PS", "Proyectos", "asasas", 1)))
            UpcomingTasksPanel(listOf<Task>(Task("Ejemplo1", "Topico1", "asasas", 1)))
        }
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            UpcomingTasksPanel(listOf<Task>(Task("Ejemplo2", "Topico2", "asas", 10)))
            UpcomingTasksPanel(listOf<Task>(Task("Ejemplo2", "Topico2", "asas", 10)))

        }
        Spacer(modifier = Modifier.height(80.dp))

        Row (modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        }
    }

}


@Composable
fun SearchBar(){
    var text by remember { mutableStateOf("") }

    Column (
        modifier= Modifier.padding(top=32.dp)
            .background(shape = RoundedCornerShape(32.dp),color = MaterialTheme.colorScheme.surfaceVariant),
        horizontalAlignment = Alignment.CenterHorizontally,

    ) {
        TextField(
            value = text,
            onValueChange = {text=it},
            placeholder = {Text("Buscar...")},
        )
    }
}