package software.ulpgc.code.application.ui.filters

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import software.ulpgc.code.architecture.control.commands.CommandBuilder
import software.ulpgc.code.architecture.control.commands.CommandLauncher
import software.ulpgc.code.architecture.control.commands.CommandType
import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.Topic

@Composable
fun CreateTagDialog(
    store: Storage,
    onClose: () -> Unit,
    enterTopic: String?

) {
    var selectedTopic: Topic?= null
    if(enterTopic != null) {
        selectedTopic = store.topics().find { it.name == enterTopic }
    }
    else{
        selectedTopic = store.topics().first()
    }
    var name by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Crear Tag") },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") }
                )

                Spacer(Modifier.height(8.dp))

                Button(onClick = { expanded = true }) {
                    selectedTopic?.name?.let { Text(it) }
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
                        .set("topicId", selectedTopic?.id.toString())
                        .build(CommandType.CREATE_TAG)
                )
                onClose()
            }) {
                Text("Crear")
            }
        }
    )
}


@Composable
fun RemoveTag(store: Storage,
              onClose: () -> Unit,
              topicName: String
) {

    val currentTopic = store.topics().find { x->x.name == topicName }
    var expanded by remember { mutableStateOf(false) }
    var selectedTag by remember { mutableStateOf("Ninguno") }
    var selectedTagUuid by remember { mutableStateOf("") }
    val tags = store.tags().filter { x -> x.topicId== currentTopic?.id }



    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Elimina un tag") },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

                Text("Tópico seleccionado: ${topicName}")

                Spacer(Modifier.height(8.dp))

                Button(onClick = { expanded = true }) {
                    Text(selectedTag)
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    store.tags().filter {x -> x.topicId== currentTopic?.id }.forEach {
                        DropdownMenuItem(
                            text = { Text(it.name) },
                            onClick = {
                                selectedTagUuid=it.id.toString()
                                selectedTag=it.name
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
                        .set("name", selectedTag)
                        .set("id", selectedTagUuid)
                        .build(CommandType.DELETE_TAG)
                )
                onClose()
            }) {
                Text("Eliminar tag")
            }
        },
        dismissButton = {
            Button(onClick = { onClose() }) {
                Text("Cerrar")
            }
        }
    )
}