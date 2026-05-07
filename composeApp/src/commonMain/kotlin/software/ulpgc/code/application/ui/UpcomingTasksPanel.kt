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

enum class TopicOption(val label: String) {
    EDIT_TOPIC("Editar tópico"),
    DELETE_TOPIC("Eliminar tópico"),
    ADD_TAG("Añadir tag al tópico"),
    REMOVE_TAG("Eliminar un tag del tópico"),
    EDIT_TAG("Editar tag")
}

@Composable
fun UpcomingTasksPanel(
    store: Storage,
    tareas: List<Task>? = null,
    title: String,
    onEdit: (Task) -> Unit = {},
    onDeleted: () -> Unit = {},
    screen: Screen,
    onRequestEditNavigation: (() -> Unit)? = null
) {
    val tasks = tareas ?: store.tasks().toList()
    val topic = store.topics().find { it.name == title }
    var selectedTask by remember { mutableStateOf<Task?>(null) }
    var expandDropdown by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf<TopicOption?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    val cardColor = topic?.color?.let { Color(it).copy(alpha = 0.25f) } ?: MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier
            .widthIn(max = 500.dp)
            .heightIn(max = 310.dp)
            .fillMaxWidth(0.8f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
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
                        TopicOption.entries.forEachIndexed { _, option ->
                            DropdownMenuItem(
                                text = { Text(option.label) },
                                onClick = {
                                    selectedOption = option
                                    expandDropdown = false
                                    showDialog = true
                                }
                            )
                        }
                    }

                    if (showDialog) {
                        when (selectedOption) {
                            TopicOption.EDIT_TOPIC -> EditTopic(store, title, onDismiss = { showDialog = false }) { onDeleted() }
                            TopicOption.DELETE_TOPIC -> DeleteTopic(store, title, onDismiss = { showDialog = false }) { onDeleted() }
                            TopicOption.ADD_TAG -> CreateTagDialog(store, onClose = { showDialog = false }, title)
                            TopicOption.REMOVE_TAG -> RemoveTag(store, onClose = { showDialog = false }, title)
                            TopicOption.EDIT_TAG -> EditTag(store, onClose = { showDialog = false }, title)
                            null -> {}
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

@Composable
fun MarkTaskIcon(store: Storage, task: Task, onDeleted: () -> Unit) {
    IconButton(
        onClick = {
            val command = CommandBuilder(store)
                .set("id", task.id.toString())
                .build(CommandType.MARK_COMPLETE)
            command
                .onSuccess { CommandLauncher.launch(it); onDeleted() }
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