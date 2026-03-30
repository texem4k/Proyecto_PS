package software.ulpgc.code

import UpcomingTasksPanel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import software.ulpgc.code.architecture.model.Task
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items

@Composable
fun HomeScreen() {

    //Cuerpo principal
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {

        SearchBar()
        //Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.weight(1f)) {
            //Lista de lista de todas las tareas
            val tareas = listOf(
                Task("Estudiar PS", "Estudios", "asasas", 1),
                Task("Hacer proyec PS", "Proyectos", "asasas", 1),
                Task("Hacer proyecto PS", "Proyectos", "asasas", 1),
                Task("Ejemplo1", "Topico1", "asasas", 1),
                Task("Ejemplo2", "Topico1", "asasas", 1),
                Task("Ejemplo3", "Topico1", "asasas", 1),
                Task("Ejemplo4", "Topico1", "asasas", 1),
                Task("Ejemplo5", "Topico1", "asasas", 1),
                Task("Ejemplo6", "Topico2", "asasas", 1),
                Task("Ejemplo7", "Topico2", "asas", 10),
                Task("Ejemplo8", "Topico2", "asas", 10),
                Task("Ejemplo9", "Topico2", "asas", 10),
                Task("Ejemplo10", "Topico2", "asas", 10),
                Task("Ejemplo11", "Topico2", "asas", 10)
            )

            val group = tareas.groupBy { it.topic }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth(0.5f),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalArrangement = Arrangement.spacedBy(64.dp)
            ) {
                items(group.entries.toList()) { (titulo, tareas)  ->
                    UpcomingTasksPanel(tareas, titulo)
                }
            }
        }
        //Botones haciendo que ocupe el ancho
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = { }) { Text("Crear tarea") }
            Button(onClick = { }) { Text("Eliminar tarea") }

        }
    }


}


@Composable
fun SearchBar(){
    var text by remember { mutableStateOf("") }

    Column (
        modifier= Modifier.padding(top=32.dp, bottom = 32.dp)
            .background(shape = RoundedCornerShape(32.dp),color = MaterialTheme.colorScheme.surfaceVariant),
        horizontalAlignment = Alignment.CenterHorizontally,

    ) {
        //Campo de texto pero quita la linea
        OutlinedTextField(
            value = text,
            modifier = Modifier.fillMaxWidth(0.3f),
            shape = RoundedCornerShape(32.dp),
            onValueChange = {text=it},
            placeholder = {Text("Buscar...")},
        )
    }
}