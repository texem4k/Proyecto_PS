package software.ulpgc.code.application.ui.CRUDs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
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
import software.ulpgc.code.application.ui.DropdownCustom
import software.ulpgc.code.application.ui.DropdownSelection
import software.ulpgc.code.architecture.io.Store
import kotlin.uuid.Uuid

@Composable
fun CreateTagDialog(
    onClose: () -> Unit,
    enterTopic: String?

) {
    var selectedTopicId by remember {
        mutableStateOf(
            enterTopic?.let { topicName ->
                Store.topics().find { it.name == topicName }?.id
            } ?: Store.topics().firstOrNull()?.id
        )
    }
    var name by remember { mutableStateOf("") }

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

                DropdownCustom(
                    section = "Tópico",
                    items = Store.topics().toList(),
                    selection = DropdownSelection.Single(selectedTopicId),
                    onItemSelected = { selectedTopicId = it },
                    itemId = { it.id },
                    itemName = { it.name }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val command = CommandBuilder().set("name", name).set("topicId", selectedTopicId.toString()).build((CommandType.CREATE_TAG))

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
fun RemoveTag(onClose: () -> Unit, topicName: String) {

    val currentTopic = Store.topics().find { x->x.name == topicName }
    val topicTags = Store.tags().filter { it.topicId == currentTopic?.id }.toList()
    var selectedTagId by remember { mutableStateOf<Uuid?>(null) }

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Elimina un tag") },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

                Text("Tópico seleccionado: $topicName")

                Spacer(Modifier.height(8.dp))

                DropdownCustom(
                    section = "Selecciona un tag",
                    items = topicTags,
                    selection = DropdownSelection.Single(selectedTagId),
                    onItemSelected = { selectedTagId = it },
                    itemId = { tag -> tag.id },
                    itemName = { tag -> tag.name }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val selectedTag = topicTags.find { it.id == selectedTagId }

                val command = CommandBuilder().set("name", selectedTag?.name ?: "").set("id", selectedTagId.toString()).build(CommandType.DELETE_TAG)

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
fun EditTag(onClose: () -> Unit, topicName: String) {

    val currentTopic = Store.topics().find { it.name == topicName }

    val topicTags = Store.tags()
        .filter { it.topicId == currentTopic?.id }
        .toList()

    val hasTags = topicTags.isNotEmpty()

    var errMessage by remember { mutableStateOf(false) }

    var selectedTagId by remember {
        mutableStateOf<Uuid?>(null)
    }

    var editedName by remember {
        mutableStateOf("")
    }

    AlertDialog(
        onDismissRequest = { onClose() },

        title = {
            Text("Edita un tag")
        },

        text = {

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text("Tópico seleccionado: $topicName")

                Spacer(Modifier.height(8.dp))

                DropdownCustom(
                    section = "Selecciona un tag",
                    items = topicTags,
                    selection = DropdownSelection.Single(selectedTagId),

                    onItemSelected = { id ->
                        selectedTagId = id

                        editedName = topicTags
                            .find { it.id == id }
                            ?.name ?: ""
                    },

                    itemId = { tag -> tag.id },
                    itemName = { tag -> tag.name }
                )

                Spacer(Modifier.height(8.dp))

                if (selectedTagId != null) {

                    TextFieldCustom(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = "Nuevo nombre para el tag",
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text
                        )
                    )
                }

                Spacer(Modifier.height(8.dp))

                if (errMessage) {
                    Text(
                        text =
                            if (!hasTags)
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

            Button(
                onClick = {

                    if (!hasTags || selectedTagId == null) {

                        errMessage = true

                    } else {

                        val command = CommandBuilder()
                            .set("id", selectedTagId.toString())
                            .set("name", editedName)
                            .set("topicId", currentTopic?.id.toString())
                            .build(CommandType.UPDATE_TAG)

                        command
                            .onSuccess { CommandLauncher.launch(it) }
                            .onFailure {
                                println("error: ${it.message}")
                            }

                        onClose()
                    }
                },

                enabled = hasTags && selectedTagId != null
            ) {

                Text("Actualizar tag")
            }
        },

        dismissButton = {

            Button(
                onClick = { onClose() }
            ) {
                Text("Cerrar")
            }
        }
    )
}