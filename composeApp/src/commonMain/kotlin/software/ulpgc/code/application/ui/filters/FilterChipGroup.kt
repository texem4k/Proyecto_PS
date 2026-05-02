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
import software.ulpgc.code.architecture.control.commands.CommandBuilder
import software.ulpgc.code.architecture.control.commands.CommandLauncher
import software.ulpgc.code.architecture.control.commands.CommandType
import software.ulpgc.code.architecture.io.Storage
import kotlin.uuid.Uuid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import software.ulpgc.code.architecture.model.Priority


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
    var createTopic by remember { mutableStateOf(false) }
    var deleteTopic by remember { mutableStateOf(false) }
    var expandedTopics by remember { mutableStateOf(false) }
    var selectedTopic = store.topics().first()
    var createTag by remember { mutableStateOf(false) }
    var topicData by remember { mutableStateOf(modifingForm()) }
    var tagData by remember { mutableStateOf(modifingForm()) }

    var editTopic by remember { mutableStateOf(false) }
    var editTag by remember { mutableStateOf(false) }
    var selectedElementForEdit by remember { mutableStateOf<String?>(null) }

    Column {

        Row(){
            Text(title, style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(end = 16.dp))

            Spacer(Modifier.height(8.dp))
            when(title){
                "Tags" -> {
                    Button(
                        modifier = Modifier.padding(end = 8.dp),
                        onClick = {
                            createTag = true;
                        },
                    ) { Text("Crear Tag") }

                    Button(
                        modifier = Modifier.padding(end = 8.dp),
                        onClick = {
                            editTag = true;
                        },
                    ) { Text("Editar Tag") }
                    if(editTag){
                        Text("Selecciona el tag a editar")
                    }
                }
                "Tópicos" -> {
                    Button(
                        modifier = Modifier.padding(end = 8.dp),
                        onClick = {
                            createTopic = true },
                    ) { Text("Crear tópico") }
                    Button(
                        modifier = Modifier.padding(end = 8.dp),
                        onClick = {
                            editTopic = !editTopic },
                    ) { Text("Editar tópico") }
                    if(editTopic){
                        Text("Selecciona el tópico a editar")
                    }
                }
            }

        }

        if(createTopic) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("Creación de tópico") },
                text = {
                    Column(){
                        TextField(value = topicData.name,
                            onValueChange = { topicData = topicData.copy(name = it) },
                            label = { Text("Nombre del tópico") },
                            isError = topicData.name.isBlank())

                        topicData.error?.let {
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
                        if (store.topics().any { it.name == topicData.name &&
                                    it.id != topicData.id }) { // Excluir el propio tópico al editar
                            topicData = topicData.copy(
                                error = "Ya existe un tópico con ese nombre"
                            )
                        } else {
                            val command = CommandBuilder(store).set("name", topicData.name).set("color", "16")
                            if (topicData.isEditing) {
                                CommandLauncher.launch(command.set("topic", topicData.id.toString())
                                    .build(CommandType.UPDATE_TOPIC))
                            } else {
                                CommandLauncher.launch(command.build(CommandType.CREATE_TOPIC))
                            }
                            topicData = modifingForm()
                            onDismiss()
                            onNavigate(Screen.HOME)
                        }
                    }) {
                        Text(if (topicData.isEditing) "Guardar cambios" else "Crear tópico")
                    }

                },
                dismissButton = {
                    Button(
                        onClick = { topicData = modifingForm()
                            createTopic=false},
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }

        //Creación de Tags
        else if(createTag) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("Creación de un tag") },
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp),
                text = {
                    Column() {
                        TextField(
                            value = tagData.name,
                            onValueChange = { tagData = tagData.copy(name = it) },
                            label = { Text("Nombre del tag") },
                            isError = tagData.name.isBlank()
                        )

                        tagData.error?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Text("Selecciona el tópico para añadirle el tag:")

                        Box {
                            Button(onClick = { expandedTopics = true }) {
                                Text(selectedTopic.name)
                            }
                            DropdownMenu(
                                expanded = expandedTopics,
                                onDismissRequest = { expandedTopics = false },
                                modifier = Modifier.fillMaxWidth(0.15f)
                            ) {
                                for (t in store.topics()) {
                                    DropdownMenuItem(
                                        text = { Text(t.name) },
                                        onClick = {
                                            expandedTopics = false
                                            selectedTopic = t
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (store.tags().any {
                                it.name == tagData.name &&
                                        it.id != tagData.id
                            }) { // Excluir el propio tópico al editar
                            tagData = tagData.copy(
                                error = "Ya existe un tag con ese nombre"
                            )
                        } else {
                            CommandLauncher.launch(
                                CommandBuilder(store).set("name", tagData.name)
                                    .set("topicId", selectedTopic.id.toString())
                                    .build(CommandType.CREATE_TAG)
                            )
                            topicData = modifingForm()
                            onDismiss()
                            onNavigate(Screen.HOME)

                        }
                    }) {
                        Text("Crear tag asociado al tópico: \"${selectedTopic.name}\"")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            tagData = modifingForm()
                            createTag = false
                        },
                    ) {
                        Text("Cancelar")
                    }
                }
            )
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
                        if (editTopic || editTag) {
                            selectedElementForEdit = option
                        } else {
                            val newSelection =
                                if (selectedOptions.contains(option))
                                    selectedOptions - option
                                else
                                    selectedOptions + option
                            onSelectionChange(newSelection)
                        }
                    },
                    label = { Text(option) },
                    trailingIcon = if(Priority.entries.none { it.text == option }) {
                        {
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
                        )}
                    }else null
                )
            }
        }
    }
    if(editTopic) {

        selectedElementForEdit?.let { topicName ->
            val currentTopic = store.topics().find { it.name == topicName }

            LaunchedEffect(topicName) {
                topicData = topicData.copy(name = topicName)
            }

            AlertDialog(
                onDismissRequest = {
                    selectedElementForEdit = null
                    editTopic = false
                },
                title = { Text("Cambio del nombre del tópico") },
                text = {
                    Column {
                        TextField(
                            value = topicData.name,
                            onValueChange = { topicData = topicData.copy(name = it) },
                            isError = topicData.name.isBlank()
                        )
                        if (topicData.error?.isNotBlank() == true) {
                            topicData.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
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

                            else -> {

                                CommandLauncher.launch(
                                    CommandBuilder(store)
                                        .set("id", currentTopic?.id.toString())
                                        .set("name", topicData.name)
                                        .set("color", "16")
                                        .build(CommandType.UPDATE_TOPIC)
                                )
                                topicData = modifingForm()
                                selectedElementForEdit = null
                                editTopic = false
                                onDismiss()
                                onNavigate(Screen.HOME)
                            }
                        }
                    }) {
                        Text("Actualizar tópico")
                    }
                }
            )
        }
    }
    else if(editTag) {
        selectedElementForEdit?.let { tagName ->
            val currentTag = store.tags().find { it.name == tagName }
            selectedTopic=store.topics().first()
            LaunchedEffect(tagName) {
                tagData = tagData.copy(name = tagName)
            }

            AlertDialog(
                onDismissRequest = {
                    selectedElementForEdit = null
                    editTag = false
                },
                title = { Text("Cambio del nombre del tópico") },
                text = {
                    Column {
                        TextField(
                            value = tagData.name,
                            onValueChange = { tagData = tagData.copy(name = it) },
                            isError = tagData.name.isBlank(),
                            label = { Text("Nombre del tag") }
                        )
                        if (tagData.error?.isNotBlank() == true) {
                            tagData.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                        }

                        Text("Selecciona el tópico para adjuntar:",modifier = Modifier.padding(top = 8.dp, bottom = 8.dp))
                        Box {
                            Button(onClick = { expandedTopics = true }, modifier = Modifier.padding(bottom = 8.dp)) {
                                Text(selectedTopic.name)
                            }

                            DropdownMenu(
                                expanded = expandedTopics,
                                onDismissRequest = { expandedTopics = false }
                            ) {
                                store.topics().forEach { topic ->
                                    DropdownMenuItem(
                                        text = { Text(topic.name) },
                                        onClick = {
                                            selectedTopic = topic
                                            expandedTopics = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val exists = store.tags().any {
                            it.name == tagData.name && it.id != currentTag?.id
                        }
                        when {
                            exists -> tagData = tagData.copy(error = "Ya existe un tag con ese nombre")
                            tagData.name.isBlank() -> tagData =
                                tagData.copy(error = "El nombre no puede estar vacío")

                            else -> {

                                CommandLauncher.launch(
                                    CommandBuilder(store)
                                        .set("id", currentTag?.id.toString())
                                        .set("name", tagData.name)
                                        .set("topicId", selectedTopic.id.toString())
                                        .build(CommandType.UPDATE_TAG)
                                )
                                tagData = modifingForm()
                                selectedElementForEdit = null
                                editTag = false
                                onDismiss()
                                onNavigate(Screen.HOME)
                            }
                        }
                    }) {
                        Text("Actualizar tópico")
                    }
                }
            )
        }
    }
}


data class modifingForm(
    var name: String = "",
    var id: Uuid? = null,
    var isEditing: Boolean = false,
    var error: String?=null
)

