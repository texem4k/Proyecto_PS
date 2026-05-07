package software.ulpgc.code.application.ui

import Screen
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import software.ulpgc.code.application.ui.CRUDs.CreateTagDialog
import software.ulpgc.code.application.ui.CRUDs.DeleteTopic
import software.ulpgc.code.application.ui.CRUDs.EditTag
import software.ulpgc.code.application.ui.CRUDs.EditTopic
import software.ulpgc.code.application.ui.CRUDs.RemoveTag
import software.ulpgc.code.architecture.control.commands.CommandBuilder
import software.ulpgc.code.architecture.control.commands.CommandLauncher
import software.ulpgc.code.architecture.control.commands.CommandType
import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.tasks.Task
import kotlin.uuid.Uuid

@Composable
fun UpcomingTasksPanel(store: Storage, tareas: List<Task>? = null, title: String, refreshKey: Int = 0, onDelete: (Task) -> Unit = {}, onEdit: (Task) -> Unit = {}, onDeleted: () -> Unit = {}, screen: Screen, onRequestEditNavigation: (() -> Unit)? = null) {
    val tasks = tareas ?: store.tasks().toList()
    val topic = store.topics().find { x-> x.name==title }
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    var expandDropdown by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(-1) }
    var showDialog by remember { mutableStateOf(true) }

    val options = listOf(
        "Editar tópico",
        "Eliminar tópico",
        "Añadir tag al tópico",
        "Eliminar un tag del tópico",
        "Editar tag")

    Card(
        modifier = Modifier
            .widthIn(max = 500.dp)
            .heightIn(max = 310.dp)
            .fillMaxWidth(0.8f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = topic?.color?.let { Color(it) }?.copy(alpha = 0.25f) ?: MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ){
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
                        .fillMaxWidth()
                )

                if (screen == Screen.TASKS) {
                    Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                        IconButton(
                            onClick = { expandDropdown = true },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Opciones",
                            )
                        }

                        DropdownMenu(
                            expanded = expandDropdown,
                            onDismissRequest = { expandDropdown = false }
                        ) {
                            options.forEach { e ->
                                DropdownMenuItem(
                                    text = { Text(e) },
                                    onClick = {
                                        selectedOption = options.indexOf(e)
                                        expandDropdown = false
                                        showDialog = true
                                    }
                                )
                            }
                        }

                        if(showDialog) {
                            when(selectedOption){
                                0 -> EditTopic(store, title, onDismiss = { showDialog = false }, { onDeleted() })
                                1 -> DeleteTopic(store, title, onDismiss = { showDialog = false }, { onDeleted() })
                                2 -> CreateTagDialog(store, onClose = { showDialog = false }, title)
                                3 -> RemoveTag(store, onClose = { showDialog = false }, title)
                                4 -> EditTag(store, onClose = { showDialog = false }, title)
                            }
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.padding(vertical = 0.5f.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(tasks) { task ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .clickable { selectedTask = task },
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            MarkTaskIcon(store, task, onDeleted = { onDeleted() })
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.name,
                                    style = MaterialTheme.typography.titleSmall,
                                )
                                val tz = TimeZone.currentSystemDefault()
                                val endDate = task.time.end.toFormattedDateDisplay(tz)
                                val endHour = task.time.end.toFormattedHour(tz)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "${store.topics().find { it.id == task.topicId }?.name ?: "Sin tópico"} $endDate $endHour",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }

            if (selectedTask != null) {
                if (selectedTask != null) {
                    TaskInformationDialog(
                        selectedTask = selectedTask!!,
                        store = store,
                        onDismiss = { selectedTask = null },
                        onEdit = { onEdit(it) },
                        onDeleted = { onDeleted(); selectedTask = null },
                        onRequestEditNavigation = onRequestEditNavigation
                    )
                }
            }
        }
}

@Composable
fun TaskInformationDialog(
    selectedTask: Task,
    store: Storage,
    onDismiss: () -> Unit,
    showActions: Boolean = true,
    onEdit: (Task) -> Unit = {},
    onDeleted: () -> Unit = {},
    onRequestEditNavigation: (() -> Unit)? = null
) {
    val tagNames = selectedTask.tags.mapNotNull { id ->
        store.tags().associateBy { it.id }[id]?.name
    }
    val timeData = selectedTask.time.mostrar().split(",")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(selectedTask.name) },
        text = {
            Text(
                "Descripción: ${selectedTask.description}\n" +
                        "Tema: ${store.topics().find { it.id == selectedTask.topicId }?.name ?: "Sin tópico"}\n" +
                        "Tags: ${tagNames.joinToString(", ")}\n" +
                        "Fecha de comienzo: ${timeData[0]} ${timeData[1]}\n" +
                        "Fecha de final: ${timeData[2]} ${timeData[3]}\n" +
                        "Prioridad: ${selectedTask.priority}"
            )
        },
        confirmButton = {
            if (showActions) {
                Button(onClick = {
                    onEdit(selectedTask)
                    onRequestEditNavigation?.invoke()
                }) {
                    Text("Editar tarea")
                }
                Button(onClick = {
                    val command = CommandBuilder(store)
                        .set("id", selectedTask.id.toString())
                        .build(CommandType.DELETE_TASK)
                    command
                        .onSuccess { CommandLauncher.launch(it) }
                        .onFailure { println("error: ${it.message}") }
                    onDeleted()
                    onDismiss()
                }) {
                    Text("Eliminar tarea")
                }
            }
            Button(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}



@Composable
fun MarkTaskIcon(store: Storage, task: Task, onDeleted: () -> Unit){
    IconButton(
        onClick = {
            val command = CommandBuilder(store)
                .set("id", task.id.toString())
                .build(CommandType.MARK_COMPLETE)

            command
                .onSuccess {
                    CommandLauncher.launch(it)
                    onDeleted()}
                .onFailure { println("error: ${it.message}") }

            onDeleted()
        },
        modifier = Modifier.size(32.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.CheckCircle,
            contentDescription = "Completar tarea",
            tint = MaterialTheme.colorScheme.primary
        )
    }

    Spacer(Modifier.width(8.dp))
}


data class modifingForm(
    var name: String = "",
    var id: Uuid? = null,
    var isEditing: Boolean = false,
    var error: String?=null
)