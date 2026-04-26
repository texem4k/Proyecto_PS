package software.ulpgc.code.application.ui.pages

import Screen
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
import software.ulpgc.code.application.ui.filters.TaskFilters
import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.Priority
import software.ulpgc.code.architecture.model.tasks.Task

@Composable
fun SearchTaskScreen(
    onNavigate: (Screen) -> Unit,
    store: Storage,
    value: String,
    onSearchTextChange: (String) -> Unit,
    filters: TaskFilters
) {
    val topicsList = mutableListOf<String>()
    val priorityList = mutableListOf<String>()
    val tagsList = mutableListOf<String>()

    var search: List<Task> = listOf()
    if (filters.hasFilter) {

        filters.topics.forEach { topicFilter ->
            val topicId = store.topics().filter { it.name == topicFilter }.first().id
            topicsList.add(topicFilter)
            val temp = store.tasks().filter { it.topicId == topicId }.toList()
            search = temp + search.toMutableList()
        }

        for (f in filters.priority) {
            priorityList.add(f)
            val priority = Priority.entries.first { p -> p.text == f }
            val temp = store.tasks().filter { task -> priority.values.contains(task.priority) }.toList()
            search = temp + search.toMutableList()
        }

        filters.tags.forEach { t -> tagsList.add(t)
            val tagId = store.tags().filter { it.name == t }.first().id
            val temp = store.tasks().filter { it.tags.contains(tagId) }.toList()
            search = temp + search.toMutableList()
        }
    } else {
        search = store.tasks().filter { it.name.contains(value, true) }.toList()
    }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        if (search.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                if (filters.hasFilter) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Text("Resultado mediante filtrado", modifier = Modifier.padding(bottom = 16.dp))
                        if (topicsList.isNotEmpty()) Text("Tópicos: ${topicsList.joinToString(", ")}", modifier = Modifier.padding(bottom = 8.dp))
                        if (priorityList.isNotEmpty()) Text("Prioridad: ${priorityList.joinToString(", ")}", modifier = Modifier.padding(bottom = 8.dp))
                        if (tagsList.isNotEmpty()) Text("Tags: ${tagsList.joinToString(", ")}", modifier = Modifier.padding(bottom = 8.dp))
                    }
                } else {
                    Text("Resultado de $value", modifier = Modifier.align(Alignment.Center))
                }
                Button(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    onClick = { onSearchTextChange(""); filters.hasFilter=false;onNavigate(Screen.TASKS) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) { Text("✖\uFE0E") }
            }

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                UpcomingTasksPanel(store, search, title = "Resultados", total = true, screen = Screen.RESULTS)
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No ha habido coincidencia con $value")
                    Button(onClick = { onSearchTextChange(""); filters.hasFilter=false;onNavigate(Screen.TASKS) }) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }
}