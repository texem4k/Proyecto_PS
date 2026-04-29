package software.ulpgc.code.application.ui.filters

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.*
import software.ulpgc.code.architecture.control.commands.CommandBuilder
import software.ulpgc.code.architecture.control.commands.CommandLauncher
import software.ulpgc.code.architecture.control.commands.CommandType
import software.ulpgc.code.architecture.io.Storage

@Composable
fun CreateTopicDialog(
    store: Storage,
    onClose: () -> Unit
) {
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
                    CommandLauncher.launch(
                        CommandBuilder(store)
                            .set("name", name)
                            .set("color", "16")
                            .build(CommandType.CREATE_TOPIC)
                    )
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