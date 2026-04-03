package software.ulpgc.code.application.ui.pages

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import software.ulpgc.code.application.ui.FilterContent
import software.ulpgc.code.application.ui.Screen
import software.ulpgc.code.application.ui.TaskFilters
import software.ulpgc.code.architecture.model.Topic


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigate: (Screen) -> Unit,
    tareas: List<Task>,
    topics: List<Topic>,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    filters: TaskFilters
) {


    var showFilters by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        SearchBar(text = searchText, onTextChange = onSearchTextChange, onSearch = { onNavigate(Screen.RESULTS) })

        Button(onClick = { showFilters = true }) {
            Text("Filtrado de tareas")
        }
        Row() {
            if (showFilters) {
                ModalBottomSheet(
                    onDismissRequest = { showFilters = false }
                ) {
                    FilterContent(
                        onApply = { newFilters ->
                            filters.topics = newFilters.topics
                            filters.status = newFilters.status
                            filters.priority = newFilters.priority
                            filters.hasFilter = newFilters.hasFilter
                            showFilters = false
                            onNavigate(Screen.RESULTS)
                        })
                }
            }
        }
        Box(modifier = Modifier.weight(1f)) {
            val group = tareas.groupBy { it.topicId }
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth(0.5f),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalArrangement = Arrangement.spacedBy(64.dp)
            ) {
                items(group.entries.toList()) { (titulo, tareasGrupo) ->
                    val topicName = topics.find { it.id == titulo }?.name ?: "Sin tópico"
                    UpcomingTasksPanel(tareasGrupo, topicName, false)
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Blue,
                    contentColor = Color.White
                ),
                onClick = { onNavigate(Screen.CREATE_TASK) }) {
                Text("Crear tarea")
            }
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                ),
                onClick = { onNavigate(Screen.DELETE_TASK) }) {
                Text("Eliminar tarea")
            }
        }
    }
}


@Composable
fun SearchBar(text: String, onTextChange: (String) -> Unit, onSearch: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(top = 32.dp, bottom = 32.dp)
            .background(shape = RoundedCornerShape(32.dp), color = MaterialTheme.colorScheme.surfaceVariant),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        OutlinedTextField(
            value = text,
            modifier = Modifier.fillMaxWidth(0.3f),
            shape = RoundedCornerShape(32.dp),
            onValueChange = { onTextChange(it) },
            placeholder = { Text("Buscar...") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    onSearch()
                }
            )
        )
    }
}