import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import software.ulpgc.code.architecture.control.CommandBuilder
import software.ulpgc.code.architecture.control.CommandLauncher
import software.ulpgc.code.architecture.control.CommandType
import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.tasks.Task


@Composable
fun UpcomingTasksPanel(store: Storage, tareas: List<Task>? = null, title: String, total: Boolean, onDelete: (Task) -> Unit = {}, onEdit: (Task) -> Unit = {}, onDeleted: () -> Unit = {}, screen: Screen) {
    val maxHeight = if (total) 600.dp else 310.dp
    val tasks = tareas ?: store.tasks().toList()
    var selectedTask by remember { mutableStateOf<Task?>(null) }

    Box(
        modifier = Modifier
            .widthIn(max=500.dp)
            .heightIn(max=maxHeight)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(8.dp)
            .fillMaxWidth(0.8f),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            /*
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
            */

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, top = 4.dp)
            ) {

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth() // importante para centrar el texto correctamente
                )

                if (screen == Screen.TASKS) {
                    IconButton(
                        onClick = { /* acción */ },
                        modifier = Modifier.align(Alignment.CenterEnd).size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Opciones",
                        )
                    }
                }
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
                            text = "${store.topics().find { it.id == task.topicId }?.name ?: "Sin tópico"} ${task.time.end}",
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

                val tagNames = selectedTask!!.tags.mapNotNull { id ->
                    store.tags().associateBy { it.id }[id]?.name
                }
                AlertDialog(
                    onDismissRequest = { selectedTask = null },
                    title = { Text(selectedTask!!.name) },
                    text = {
                        Text("Descripción: ${selectedTask!!.description}\nTema: ${store.topics().find
                        { it.id == selectedTask!!.topicId }?.name ?: "Sin tópico"}\nTags: " +tagNames.joinToString(", ")+
                                "\nFecha de comienzo: " +
                                "${selectedTask!!.time.start.toString().substring(0, 16)}\nFecha de final: ${selectedTask!!
                                    .time.end.toString().substring(0, 16)}\nPrioridad: ${selectedTask!!.priority}")
                    },
                    confirmButton = {
                        Button(onClick = {
                            onEdit(selectedTask!!)
                        }) {
                            Text("Editar tarea")
                        }
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
                        Button(onClick = {
                            onDelete(selectedTask!!)
                            CommandLauncher.launch(CommandBuilder(store).set("id", selectedTask!!
                                .id.toString()).build(CommandType.DELETE_TASK))
                            selectedTask = null
                            onDeleted()
                        }) {
                            Text("Eliminar")
                        }
                    }
                )
            }
        }
    }
}