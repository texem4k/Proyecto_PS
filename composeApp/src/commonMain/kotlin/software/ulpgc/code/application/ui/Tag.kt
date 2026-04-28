package software.ulpgc.code.application.ui.filters

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import software.ulpgc.code.architecture.control.CommandBuilder
import software.ulpgc.code.architecture.control.CommandLauncher
import software.ulpgc.code.architecture.control.CommandType
import software.ulpgc.code.architecture.io.Storage

@Composable
fun CreateTagDialog(
    store: Storage,
    onClose: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedTopic = store.topics().first()
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Crear Tag") },
        text = {
            Column {

                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") }
                )

                Spacer(Modifier.height(8.dp))

                Button(onClick = { expanded = true }) {
                    Text(selectedTopic.name)
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    store.topics().forEach {
                        DropdownMenuItem(
                            text = { Text(it.name) },
                            onClick = {
                                selectedTopic = it
                                expanded = false
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                CommandLauncher.launch(
                    CommandBuilder(store)
                        .set("name", name)
                        .set("topicId", selectedTopic.id.toString())
                        .build(CommandType.CREATE_TAG)
                )
                onClose()
            }) {
                Text("Crear")
            }
        }
    )
}