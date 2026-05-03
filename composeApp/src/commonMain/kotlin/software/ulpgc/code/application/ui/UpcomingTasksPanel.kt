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
import androidx.lifecycle.viewmodel.compose.viewModel
import software.ulpgc.code.application.ColorWheelPicker
import software.ulpgc.code.application.toRgbString
import software.ulpgc.code.application.ui.filters.CreateTagDialog
import software.ulpgc.code.application.ui.filters.RemoveTag
import software.ulpgc.code.application.ui.pages.CreateTask
import software.ulpgc.code.architecture.control.commands.CommandBuilder
import software.ulpgc.code.architecture.control.commands.CommandLauncher
import software.ulpgc.code.architecture.control.commands.CommandType
import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.tasks.Task
import kotlin.uuid.Uuid


@Composable
fun UpcomingTasksPanel(store: Storage, tareas: List<Task>? = null, title: String, refreshKey: Int = 0, onDelete: (Task) -> Unit = {}, onEdit: (Task) -> Unit = {}, onDeleted: () -> Unit = {}, screen: Screen, onRequestEditNavigation: (() -> Unit)? = null) {
    val tasks = tareas ?: store.tasks().toList()
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    var expandDropdown by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(-1) }
    var showDialog by remember { mutableStateOf(true) }




    val options = listOf("Editar tópico", "Eliminar tópico", "Añadir tag al tópico", "Eliminar un tag del tópico")

    Box(
        modifier = Modifier
            .widthIn(max=500.dp)
            .heightIn(max=310.dp)
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
                                        selectedOption=options.indexOf(e)
                                        expandDropdown = false
                                        showDialog = true
                                    }
                                )
                            }
                        }


                        if(showDialog) {
                            when(selectedOption){
                                0 -> EditTopic(store,title, onDismiss={showDialog=false})
                                1 -> DeleteTopic(store,title, onDismiss={showDialog=false})
                                2 -> CreateTagDialog(store, onClose = {showDialog=false}, title)
                                3 -> RemoveTag(store, onClose = {showDialog=false}, title)
                            }
                        }
                    }
                }
            }
            LazyColumn(
                modifier = Modifier.padding(vertical =0.5f.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(tasks) { task ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .clickable { selectedTask = task },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Botón de completar
                            IconButton(
                                onClick = { /* onTaskComplete(task) */ },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = "Completar tarea",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(Modifier.width(8.dp))

                            // Contenido de la tarea
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
                            onRequestEditNavigation?.invoke()
                        }) {
                            Text("Editar tarea")
                        }
                        Button(onClick = {
                            CommandLauncher.launch(
                                CommandBuilder(store).set("id", selectedTask!!
                                .id.toString()).build(CommandType.DELETE_TASK))
                            onDeleted()
                            selectedTask = null
                        }) {
                            Text("Eliminar tarea")
                        }
                        Button(onClick = { selectedTask = null }) {
                            Text("Cerrar")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun EditTopic(store: Storage ,topicName: String,onDismiss: () -> Unit) {

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

                        CommandLauncher.launch(
                            CommandBuilder(store)
                                .set("id", currentTopic?.id.toString())
                                .set("name", topicData.name)
                                .set("color", "16")
                                .build(CommandType.UPDATE_TOPIC)
                        )
                        topicData = modifingForm()
                        onDismiss()
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
fun DeleteTopic(store: Storage, topicName: String, onDismiss: () -> Unit){
    val currentTopic = store.topics().find { it.name == topicName }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eliminar tópico") },
        text = {
            Text("¿Seguro que quieres eliminar el tópico '${currentTopic?.name}' para eliminar?")
        },
        confirmButton = {
            Button(onClick = {
                CommandLauncher.launch(
                    CommandBuilder(store)
                        .set("id", currentTopic?.id.toString())
                        .build(CommandType.DELETE_TOPIC))
                onDismiss()
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

data class modifingForm(
    var name: String = "",
    var id: Uuid? = null,
    var isEditing: Boolean = false,
    var error: String?=null
)