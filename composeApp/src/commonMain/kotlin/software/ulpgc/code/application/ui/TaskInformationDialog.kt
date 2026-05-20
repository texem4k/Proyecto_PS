package software.ulpgc.code.application.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import software.ulpgc.code.architecture.control.commands.CommandBuilder
import software.ulpgc.code.architecture.control.commands.CommandLauncher
import software.ulpgc.code.architecture.control.commands.CommandType
import software.ulpgc.code.architecture.io.Store
import software.ulpgc.code.architecture.model.tasks.Task

@Composable
fun TaskInformationDialog(
    selectedTask: Task,
    onDismiss: () -> Unit,
    onEdit: (Task) -> Unit = {},
    onDeleted: () -> Unit = {},
    onRequestEditNavigation: (() -> Unit)? = null
) {
    val tagNames = remember(selectedTask) {
        selectedTask.tags.mapNotNull { id -> Store.tags().associateBy { it.id }[id]?.name }
    }
    val timeData = remember(selectedTask) {
        selectedTask.time.mostrar().split(",")
    }

    val userName = Store.users().find { it.id == Store.currentUser() }?.name ?: "Usuario"

    val isRoot = userName == "root"
    val assignedUsers = remember(selectedTask) {
        if (isRoot) emptyList()
        else selectedTask.users.mapNotNull { id -> Store.users().find { it.id == id }?.name }
    }

    var showDeleteConfirmation by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(selectedTask.name) },
        text = {
            Text(
                "Descripción: ${selectedTask.description}\n" +
                        "Tema: ${Store.topics().find { it.id == selectedTask.topicId }?.name ?: "Sin tópico"}\n" +
                        "Tags: ${tagNames.joinToString(", ")}\n" +
                        "Fecha de comienzo: ${timeData[0]} ${timeData[1]}\n" +
                        "Fecha de final: ${timeData[2]} ${timeData[3]}\n" +
                        "Prioridad: ${selectedTask.priority.text} (${selectedTask.priority.value})" +
                        if (!isRoot && assignedUsers.isNotEmpty()) "\nUsuarios: ${assignedUsers.joinToString(", ")}" else ""

            )
        },
        confirmButton = {
                Button(onClick = {
                    onEdit(selectedTask)
                    onRequestEditNavigation?.invoke()
                }) {
                    Text("Editar tarea")
                }

                Button(onClick = { showDeleteConfirmation = true }) {
                    Text("Eliminar tarea")
                    if (showDeleteConfirmation) {
                        ConfirmDeleteDialog(
                            taskName = selectedTask.name,
                            onConfirm = {
                                val command = CommandBuilder()
                                    .set("id", selectedTask.id.toString())
                                    .build(CommandType.DELETE_TASK)
                                command
                                    .onSuccess { CommandLauncher.launch(it) }
                                    .onFailure { println("error: ${it.message}") }
                                showDeleteConfirmation = false
                                onDeleted()
                                onDismiss()
                            },
                            onDismiss = { showDeleteConfirmation = false }
                        )
                    }
                }
            Button(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}


@Composable
fun ConfirmDeleteDialog(
    taskName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eliminar tarea") },
        text = { Text("¿Estás seguro de que deseas eliminar \"$taskName\"? Esta acción no se puede deshacer.") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}