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
import androidx.compose.material3.FloatingActionButtonDefaults.elevation
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import software.ulpgc.code.application.ColorWheelPicker
import software.ulpgc.code.application.toRgbString
import software.ulpgc.code.application.ui.filters.CreateTagDialog
import software.ulpgc.code.application.ui.filters.RemoveTag
import software.ulpgc.code.application.ui.pages.toFormattedDate
import software.ulpgc.code.application.ui.pages.toFormattedDateDisplay
import software.ulpgc.code.application.ui.pages.toFormattedHour
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
    val topicColor = topic?.color?.let { Color(it) } ?: MaterialTheme.colorScheme.surface
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    var expandDropdown by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(-1) }
    var showDialog by remember { mutableStateOf(true) }

    val options = listOf("Editar tópico", "Eliminar tópico", "Añadir tag al tópico", "Eliminar un tag del tópico")

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
                                0 -> EditTopic(store, title, onDismiss={showDialog=false}, { onDeleted() })
                                1 -> DeleteTopic(store, title, onDismiss={showDialog=false}, { onDeleted() })
                                2 -> CreateTagDialog(store, onClose = {showDialog=false}, title)
                                3 -> RemoveTag(store, onClose = {showDialog=false}, title)
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
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    val command = CommandBuilder(store).set("id", task.id.toString()).build(CommandType.MARK_COMPLETE)

                                    command
                                        .onSuccess { CommandLauncher.launch(it) }
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

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.name,
                                    style = MaterialTheme.typography.titleSmall,
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "${store.topics().find { it.id == task.topicId }?.name ?: "Sin tópico"} ${task.time.end}",
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
fun EditTopic(store: Storage ,topicName: String,onDismiss: () -> Unit, onDeleted: () -> Unit = {} ) {

    val currentTopic = store.topics().find { it.name == topicName }
    var chosenColor by remember { mutableStateOf<Color?>(Color(currentTopic?.color!!)) }

    var topicData by remember(topicName) {
        mutableStateOf(modifingForm().copy(name = topicName))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar tópico") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TextField(
                    value = topicData.name,
                    onValueChange = { topicData = topicData.copy(name = it) },
                    isError = topicData.name.isBlank(),
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                ColorWheelPicker(
                    wheelSize = 130.dp,
                    onColorSelected = { color ->
                        chosenColor = color
                    }
                )
                Text("Color seleccionado: ${chosenColor?.toRgbString()}")
                if(topicData.error != null) {
                    Text(topicData.error.toString(), color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val exists = store.topics().any {
                    it.name == topicData.name && it.id != currentTopic?.id
                }
                when {
                    exists -> topicData = topicData.copy(error = "Ya existe un tópico con ese nombre")
                    topicData.name.isBlank() -> topicData =
                        topicData.copy(error = "El nombre no puede estar vacío")
                    chosenColor===null -> topicData.copy(error="Debes seleccionar un color para el tópico")

                    else -> {

                        val command = CommandBuilder(store).set("id", currentTopic?.id.toString()).set("name", topicData.name).set("color", chosenColor!!.toArgb().toString()).build(CommandType.UPDATE_TOPIC)

                        command
                            .onSuccess { CommandLauncher.launch(it) }
                            .onFailure { println("error: ${it.message}") }
                        topicData = modifingForm()
                        onDismiss()
                        onDeleted()
                    }
                }
            }) {
                Text("Actualizar tópico")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}


@Composable
fun DeleteTopic(store: Storage, topicName: String, onDismiss: () -> Unit, onDeleted: () -> Unit = {}){
    val currentTopic = store.topics().find { it.name == topicName }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eliminar tópico") },
        text = {
            Text("¿Seguro que quieres eliminar el tópico '${currentTopic?.name}' para eliminar?")
        },
        confirmButton = {
            Button(onClick = {
                val command = CommandBuilder(store).set("id", currentTopic?.id.toString()).build(CommandType.DELETE_TOPIC)
                command.onSuccess{CommandLauncher.launch(it)}.onFailure { println("error: ${it.message}") }
                onDismiss()
                onDeleted()
            }){
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
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
    val tz = TimeZone.currentSystemDefault()
    val startDate = selectedTask.time.start.toFormattedDateDisplay(tz)
    val startHour = selectedTask.time.start.toFormattedHour(tz)
    val endDate = selectedTask.time.end.toFormattedDateDisplay(tz)
    val endHour = selectedTask.time.end.toFormattedHour(tz)
    val tagNames = selectedTask.tags.mapNotNull { id ->
        store.tags().associateBy { it.id }[id]?.name
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(selectedTask.name) },
        text = {
            Text(
                "Descripción: ${selectedTask.description}\n" +
                        "Tema: ${store.topics().find { it.id == selectedTask.topicId }?.name ?: "Sin tópico"}\n" +
                        "Tags: ${tagNames.joinToString(", ")}\n" +
                        "Fecha de comienzo: $startDate $startHour\n" +
                        "Fecha de final: $endDate $endHour\n" +
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




data class modifingForm(
    var name: String = "",
    var id: Uuid? = null,
    var isEditing: Boolean = false,
    var error: String?=null
)