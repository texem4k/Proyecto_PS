package software.ulpgc.code.application.ui.CRUDs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import software.ulpgc.code.application.ui.ColorWheelPicker
import software.ulpgc.code.application.ui.TextFieldCustom
import software.ulpgc.code.application.ui.toRgbString
import software.ulpgc.code.architecture.control.commands.CommandBuilder
import software.ulpgc.code.architecture.control.commands.CommandLauncher
import software.ulpgc.code.architecture.control.commands.CommandType
import software.ulpgc.code.architecture.io.Storage
import kotlin.uuid.Uuid


data class ModifingForm(
    var name: String = "",
    var id: Uuid? = null,
    var isEditing: Boolean = false,
    var error: String? = null
)
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
        text ={
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TextFieldCustom(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nombre",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)

                )

                ColorWheelPicker(
                    wheelSize = 130.dp,
                    onColorSelected = { color ->
                        chosenColor = color
                    }
                )
                Text("Color seleccionado: ${chosenColor?.toRgbString()}")

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


@Composable
fun EditTopic(store: Storage ,topicName: String,onDismiss: () -> Unit, onDeleted: () -> Unit = {} ) {

    val currentTopic = store.topics().find { it.name == topicName }
    var chosenColor by remember { mutableStateOf<Color?>(Color(currentTopic?.color!!)) }

    var topicData by remember(topicName) {
        mutableStateOf(ModifingForm().copy(name = topicName))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar tópico") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                TextFieldCustom(
                    value = topicData.name,
                    onValueChange = { topicData = topicData.copy(name = it) },
                    label = "Editar un topico",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)

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

                        val command = CommandBuilder(store).set("id", currentTopic?.id.toString()).set("name", topicData.name).set("color", chosenColor!!.toArgb().toString()).build(CommandType.UPDATE_TOPIC)

                        command
                            .onSuccess { CommandLauncher.launch(it) }
                            .onFailure { println("error: ${it.message}") }
                        topicData = ModifingForm()
                        onDismiss()
                        onDeleted()
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
fun DeleteTopic(store: Storage, topicName: String, onDismiss: () -> Unit, onDeleted: () -> Unit = {}){
    val currentTopic = store.topics().find { it.name == topicName }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eliminar tópico") },
        text = {
            Text("¿Seguro que quieres eliminar el tópico '${currentTopic?.name}' para eliminar?")
            Text("(Eliminar el tópico conlleva la eliminación de las tareas y tags asociados al tópico)",
                fontWeight = FontWeight.Bold, modifier = Modifier.padding(top=32.dp))
        },
        confirmButton = {
            Button(onClick = {
                val command = CommandBuilder(store).set("id", currentTopic?.id.toString()).build(CommandType.DELETE_TOPIC)
                command.onSuccess{CommandLauncher.launch(it)}.onFailure { println("error: ${it.message}") }
                onDismiss()
                onDeleted()
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