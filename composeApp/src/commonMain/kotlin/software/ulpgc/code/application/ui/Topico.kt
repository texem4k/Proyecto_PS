package software.ulpgc.code.application.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import software.ulpgc.code.application.ColorWheelPicker
import software.ulpgc.code.architecture.control.commands.CommandBuilder
import software.ulpgc.code.architecture.control.commands.CommandLauncher
import software.ulpgc.code.architecture.control.commands.CommandType
import software.ulpgc.code.architecture.control.logs.LogMaster
import software.ulpgc.code.architecture.io.Storage

@Composable
fun CreateTopicDialog(
    store: Storage,
    onClose: () -> Unit
) {

    var chosenColor by remember { mutableStateOf<Color?>(null) }
    var name by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Crear tópico") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") }
                )
                ColorWheelPicker(
                    wheelSize = 130.dp,
                    onColorSelected = { color ->
                        chosenColor = color
                    }
                )
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (store.topics().any { it.name == name }) {
                    error = "Ya existe un tópico"
                } else {
                    val command = CommandBuilder(store).set("name", name).set("color", chosenColor?.toArgb().toString()).build(CommandType.CREATE_TOPIC)
                    command.onSuccess{CommandLauncher.launch(it)}.onFailure { println("error: ${it.message}") }

                    onClose()
                }
            }) {
                Text("Crear")
            }
        },
        dismissButton = {
            Button(onClick = {onClose()}){Text("Cancelar")}
        }
    )
}