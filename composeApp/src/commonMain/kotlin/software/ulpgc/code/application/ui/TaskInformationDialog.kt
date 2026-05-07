package software.ulpgc.code.application.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import software.ulpgc.code.architecture.control.commands.CommandBuilder
import software.ulpgc.code.architecture.control.commands.CommandLauncher
import software.ulpgc.code.architecture.control.commands.CommandType
import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.tasks.Task

@Composable
fun TaskInformationDialog(
    selectedTask: Task,
    store: Storage,
    onDismiss: () -> Unit,
    onEdit: (Task) -> Unit = {},
    onDeleted: () -> Unit = {},
    onRequestEditNavigation: (() -> Unit)? = null
) {
    val tagNames = remember(selectedTask) {
        selectedTask.tags.mapNotNull { id -> store.tags().associateBy { it.id }[id]?.name }
    }
    val timeData = remember(selectedTask) {
        selectedTask.time.mostrar().split(",")
    }

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
            Button(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}