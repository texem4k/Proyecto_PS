package software.ulpgc.code.application.ui.filters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import software.ulpgc.code.application.ui.Screen
import software.ulpgc.code.architecture.control.CommandBuilder
import software.ulpgc.code.architecture.control.CommandLauncher
import software.ulpgc.code.architecture.control.CommandType
import software.ulpgc.code.architecture.io.Storage
import kotlin.uuid.Uuid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextField

@Composable
fun FilterChipGroup(
    title: String,
    options: List<String>,
    selectedOptions: Set<String>,
    onSelectionChange: (Set<String>) -> Unit,
    store: Storage,
    onNavigate: (Screen) -> Unit,
    onDismiss: () -> Unit = {}

) {
    var editTopic by remember { mutableStateOf(false) }
    var deleteTopic by remember { mutableStateOf(false) }
    var expandedTopics by remember { mutableStateOf(false) }
    var selectedTopic: Uuid? = null
    var editTag by remember { mutableStateOf(false) }
    var topicEditingValues by remember { mutableStateOf(modifingtopic()) }

    Column {

        Row(){
            Text(title, style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(end = 16.dp))

            Spacer(Modifier.height(8.dp))
            when(title){
                "Tags" -> {
                    Button(
                        modifier = Modifier.padding(end = 4.dp),
                        onClick = {
                            editTag = true;
                        },
                    ) { Text("Crear Tag") }
                }
                "Tópicos" -> {
                    Button(
                        modifier = Modifier.padding(end = 4.dp),
                        onClick = {
                            editTopic = true },
                    ) { Text("Crear tópico") }

                    Button(
                        modifier = Modifier.padding(end = 4.dp),
                        onClick = {
                            deleteTopic = true },
                    ) { Text("Eliminar tópico") }

                }
            }
        }

        if(editTopic) {
            var m = ""
            if(topicEditingValues.isEditing){
                m = "Edición de tópico"
                //topicEditingValues = topicEditingValues.copy(id = store.topics().filter{ it.name==topicEditingValues.name}.first().id)
            }
            else{
                m= "Creación de tópico"
            }
            AlertDialog(
                onDismissRequest = { },
                title = { Text(m) },
                text = {
                    Column(){
                        TextField(value = topicEditingValues.name,
                            onValueChange = { topicEditingValues = topicEditingValues.copy(name = it) },
                            label = { Text("Nombre del tópico") },
                            isError = topicEditingValues.name.isBlank())

                        topicEditingValues.error?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (store.topics().any { it.name == topicEditingValues.name &&
                                    it.id != topicEditingValues.id }) { // Excluir el propio tópico al editar
                            topicEditingValues = topicEditingValues.copy(
                                error = "Ya existe un tópico con ese nombre"
                            )
                        } else {
                            val command = CommandBuilder(store).set("name", topicEditingValues.name).set("color", "16")
                            if (topicEditingValues.isEditing) {
                                CommandLauncher.launch(command.set("topic", topicEditingValues.id.toString())
                                    .build(CommandType.UPDATE_TOPIC))
                            } else {
                                CommandLauncher.launch(command.build(CommandType.CREATE_TOPIC))
                            }
                            topicEditingValues = modifingtopic()
                            onDismiss()
                            onNavigate(Screen.HOME)

                        }
                    }) {
                        Text(if (topicEditingValues.isEditing) "Guardar cambios" else "Crear tópico")
                    }

                    if (topicEditingValues.isEditing) { // Solo mostrar al editar
                        Button(onClick = {
                            CommandBuilder(store)
                                .set("id", topicEditingValues.id.toString()) // Usar ID guardado
                                .build(CommandType.DELETE_TOPIC)
                            editTopic = false
                            topicEditingValues = modifingtopic()
                        }) {
                            Text("Eliminar tópico")
                        }
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { topicEditingValues = modifingtopic()
                            editTopic=false},
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        } else if (deleteTopic) {

        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.heightIn(max = 200.dp)
        ) {
            items(options.size) { index ->
                val option = options[index]

                FilterChip(
                    selected = selectedOptions.contains(option),
                    onClick = {
                        val newSelection =
                            if (selectedOptions.contains(option))
                                selectedOptions - option
                            else
                                selectedOptions + option

                        onSelectionChange(newSelection)
                    },
                    label = { Text(option) },
                    trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Eliminar $option",
                                modifier = Modifier
                                    .size(12.dp)
                                    .clickable {
                                        onSelectionChange(selectedOptions - option)

                                        val topic = store.topics().find { it.name == option }
                                        val tag = store.tags().find { it.name == option }

                                        when {
                                            topic != null -> CommandLauncher.launch(
                                                CommandBuilder(store)
                                                    .set("id", topic.id.toString())
                                                    .build(CommandType.DELETE_TOPIC)
                                            )
                                            tag != null -> CommandLauncher.launch(
                                                CommandBuilder(store)
                                                    .set("id", tag.id.toString())
                                                    .build(CommandType.DELETE_TAG)
                                            )
                                        }
                                    }
                            )
                        }
                )
            }
        }
    }
}


data class modifingtopic(
    var name: String = "",
    var id: Uuid? = null,
    var isEditing: Boolean = false,
    var error: String?=null
)

