import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import software.ulpgc.code.application.ui.topics
import software.ulpgc.code.architecture.model.Task


@Composable
fun UpcomingTasksPanel(tasks: List<Task>, title: String, total: Boolean) {
    val maxHeight = if (total) 600.dp else 310.dp
    var selectedTask by remember { mutableStateOf<Task?>(null) }

    Box(
        modifier = Modifier
            .widthIn(max=300.dp)
            .heightIn(max=maxHeight)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(8.dp)
            .fillMaxWidth(0.8f),
        contentAlignment = Alignment.Center
    ) {
        Card(modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(text= title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp, top = 4.dp),
                        textAlign = TextAlign.Center)
                }
                LazyColumn(
                    modifier = Modifier.padding(vertical =0.5f.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(tasks) { task ->
                        Card(modifier = Modifier.fillMaxWidth(0.95f) .clickable { selectedTask = task }, RoundedCornerShape(8.dp)) {
                            Text(
                                text = task.name,
                                style = MaterialTheme.typography.titleSmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "${topics.find { it.id == task.topicId }?.name ?: "Sin tópico"} ${task.time.end}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                }
            if (selectedTask != null && !total) {
                AlertDialog(
                    onDismissRequest = { selectedTask = null },
                    title = { Text(selectedTask!!.name) },
                    text = {
                        Text("Tema: ${topics.find { it.id == selectedTask!!.topicId }?.name ?: "Sin tópico"}\nFecha: ${selectedTask!!.time.end}")
                    },
                    confirmButton = {
                        Button(onClick = { selectedTask = null }) {
                            Text("Cerrar")
                        }
                    }
                )
            } else if (selectedTask != null && total){
                AlertDialog(
                    onDismissRequest = { selectedTask = null },
                    title = { Text("Estas seguro que quieres eliminar la tarea")
                            },
                    confirmButton = {
                        Button(onClick = { selectedTask = null }) {
                            Text("No")
                        }
                        Button(onClick = {  }) {
                            Text("Eliminar")
                        }
                    }
                )
            }

        }
    }

}