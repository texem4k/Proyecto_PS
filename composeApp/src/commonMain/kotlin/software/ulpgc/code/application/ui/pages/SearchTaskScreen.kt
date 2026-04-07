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
import software.ulpgc.code.application.ui.filters.TaskFilters
import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.tasks.Task
import kotlin.collections.forEach

@Composable
fun SearchTaskScreen(
    onNavigate: (Screen) -> Unit,
    store: Storage,
    value: String,
    onSearchTextChange: (String) -> Unit,
    filters: TaskFilters
) {
    val topics = mutableListOf<String>()
    val priority = mutableListOf<String>()

    var search: List<Task> = listOf()
    if (filters.hasFilter) {
        filters.topics.forEach { topicFilter ->
            topics.add(topicFilter)
            val temp = store.tasks().filter { it.name == topicFilter }.toList()
            val temp1 = search.toMutableList()
            search = temp + temp1
        }
        filters.priority.forEach { prior ->
            priority.add(prior)
        }
    } else {
        search = store.tasks().filter { it.name.contains(value, true) }.toList()
    }


    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                if (search.isNotEmpty()) {

                    if (filters.hasFilter) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Resultado de mediante filtrado",
                                modifier = Modifier.padding(bottom = 16.dp).align(Alignment.CenterHorizontally),
                            )

                            if (topics.isNotEmpty()) {
                                var topicsText = ""
                                topics.forEach { text -> topicsText += "$text ," }
                                Text(
                                    text = "Tópicos: $topicsText",
                                    modifier = Modifier.padding(bottom = 32.dp).align(Alignment.CenterHorizontally),
                                )
                            }

                            if (priority.isNotEmpty()) {
                                var priorityText = ""
                                topics.forEach { text -> priorityText += "$text ," }
                                Text(
                                    text = "Prioridad: $priorityText",
                                    modifier = Modifier.padding(bottom = 32.dp),
                                )
                            }

                        }

                    } else {
                        Text(
                            text = "Resultado de $value",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }


                    Button(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        onClick = {
                            onSearchTextChange("")
                            onNavigate(Screen.HOME)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    ) {
                        Text("✖\uFE0E")
                    }
                }
            }

            if (search.isNotEmpty()) {
                Box(modifier = Modifier.weight(0.5f)) {
                    UpcomingTasksPanel(store, search, title = "Resultados", total = true)
                }
            } else {
                Box(modifier = Modifier.weight(0.5f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No ha habido coincidencia con el $value")
                        Button(
                            onClick = {
                                onSearchTextChange("")
                                onNavigate(Screen.HOME)
                            },
                        ) {
                            Text("Cerrar")
                        }
                    }
                }
            }
        }
    }
}