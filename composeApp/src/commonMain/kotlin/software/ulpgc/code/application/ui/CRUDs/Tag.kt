package software.ulpgc.code.application.ui.CRUDs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import software.ulpgc.code.application.ui.TextFieldCustom
import software.ulpgc.code.architecture.control.commands.CommandBuilder
import software.ulpgc.code.architecture.control.commands.CommandLauncher
import software.ulpgc.code.architecture.control.commands.CommandType
import software.ulpgc.code.architecture.io.Store
import software.ulpgc.code.architecture.model.Topic

@Composable
fun CreateTagDialog(
    store: Store,
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

                TextFieldCustom(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nombre",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)

                )

                Spacer(Modifier.height(8.dp))

                Button(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth(0.5f)) {
                    selectedTopic?.name?.let { Text(it) }

                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
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
                val command = CommandBuilder(store).set("name", name).set("topicId", selectedTopic?.id.toString()).build((CommandType.CREATE_TAG))

                command
                    .onSuccess { CommandLauncher.launch(it) }
                    .onFailure { println("error: ${it.message}") }
                onClose()
            }) {
                Text("Crear")
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoveTag(store: Store,
              onClose: () -> Unit,
              topicName: String
) {

    val currentTopic = store.topics().find { x->x.name == topicName }
    val topicTags = store.tags().filter { x -> x.topicId == currentTopic?.id }
    var expanded by remember { mutableStateOf(false) }
    var selectedTag by remember { mutableStateOf("Ninguno") }
    var selectedTagName by remember { mutableStateOf("Ninguno") }
    var selectedTagUuid by remember { mutableStateOf("") }


    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Elimina un tag") },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

                Text("Tópico seleccionado: $topicName")

                Spacer(Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { if (topicTags.toList().isNotEmpty()) expanded = it },
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    OutlinedTextField(
                        value = selectedTagName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Selecciona un tag") },
                        modifier = Modifier.menuAnchor().fillMaxWidth(0.50f),
                        shape = RoundedCornerShape(32.dp),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        topicTags.forEach { tag ->
                            val isSelected = tag.id.toString() == selectedTagUuid

                            DropdownMenuItem(
                                text = { Text(tag.name) },
                                onClick = {
                                    selectedTagUuid = tag.id.toString()
                                    selectedTagName = tag.name
                                    selectedTag = tag.name
                                    expanded = false
                                },
                                trailingIcon = {
                                    if (isSelected) Icon(Icons.Default.Check, contentDescription = null)
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val command = CommandBuilder(store).set("name", selectedTag).set("id", selectedTagUuid).build((CommandType.DELETE_TAG))

                command
                    .onSuccess { CommandLauncher.launch(it) }
                    .onFailure { println("error: ${it.message}") }

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


@Composable
fun EditTag(store: Store,
            onClose: () -> Unit,
            topicName: String){

    val currentTopic = store.topics().find { x->x.name == topicName }
    var expanded by remember { mutableStateOf(false) }
    var errMessage by remember { mutableStateOf(false) }
    var selectedTag by remember { mutableStateOf("Ninguno") }
    var selectedTagName by remember { mutableStateOf("Ninguno") }
    var selectedTagUuid by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onClose() },
        title = { Text("Edita un tag") },
        text = {
            val topicTags = store.tags().filter { x -> x.topicId == currentTopic?.id }
            val hasTags = topicTags.toList().isNotEmpty()
            val isTagSelected = hasTags && selectedTagName != "Ninguno"

            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

                Text("Tópico seleccionado: $topicName")

                Spacer(Modifier.height(8.dp))

                if (isTagSelected) {
                    TextFieldCustom(
                        value = selectedTag,
                        onValueChange = { selectedTag = it },
                        label = "Nuevo nombre para el tag",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    )
                }

                // 1. Primero el desplegable para seleccionar el tag
                Button(
                    onClick = { if (hasTags) expanded = true },
                    modifier = Modifier.fillMaxWidth(0.5f),
                    enabled = hasTags  // Deshabilitado si no hay tags
                ) {
                    Text(if (hasTags) selectedTagName else "Sin tags")
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    topicTags.forEach {
                        DropdownMenuItem(
                            text = { Text(it.name) },
                            onClick = {
                                selectedTagUuid = it.id.toString()
                                selectedTagName = it.name
                                selectedTag = selectedTagName
                                expanded = false
                            }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))



                Spacer(Modifier.height(8.dp))

                if (errMessage) {
                    Text(
                        text = if (!hasTags)
                            "El tópico no tiene tags asociados"
                        else
                            "Debes seleccionar un tag antes de actualizar",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            // Calcular aquí también para el botón de confirmar
            val hasTags = store.tags().any { x -> x.topicId == currentTopic?.id }

            Button(
                onClick = {
                    if (!hasTags || selectedTagName == "Ninguno") {
                        errMessage = true
                    } else {
                        val command = CommandBuilder(store)
                            .set("id", selectedTagUuid)
                            .set("name", selectedTag)
                            .set("topicId", currentTopic?.id.toString())
                            .build(CommandType.UPDATE_TAG)

                        command
                            .onSuccess { CommandLauncher.launch(it) }
                            .onFailure { println("error: ${it.message}") }

                        onClose()
                    }
                },
                enabled = hasTags  // Botón deshabilitado si no hay tags
            ) {
                Text("Actualizar tag")
            }
        },
        dismissButton = {
            Button(onClick = { onClose() }) {
                Text("Cerrar")
            }
        }
    )
}