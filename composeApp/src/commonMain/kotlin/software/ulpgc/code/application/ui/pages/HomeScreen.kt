package software.ulpgc.code.application.ui.pages

import UpcomingTasksPanel
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.input.ImeAction
import software.ulpgc.code.application.ui.filters.FilterContent
import software.ulpgc.code.application.ui.Screen
import software.ulpgc.code.application.ui.filters.TaskFilters
import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.tasks.Task
import software.ulpgc.code.architecture.control.CommandLauncher
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.sp
import software.ulpgc.code.application.ui.SideBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigate: (Screen) -> Unit,
    store: Storage,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onDeleted: () -> Unit = {}

) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false

                when {
                    event.isCtrlPressed && event.key == Key.Z -> {
                        CommandLauncher.undo()
                        onDeleted()
                        true
                    }
                    event.isCtrlPressed && event.key == Key.Y -> {
                        CommandLauncher.redo()
                        onDeleted()
                        true
                    }
                    else -> false
                }
            }
    ) {
        Row(modifier = Modifier.fillMaxSize()) {

            SideBar(selectedScreen = Screen.HOME,
                onNavigate = onNavigate,
            )

            Column(
                modifier = Modifier
                    .weight(2.7f)
                    .fillMaxHeight()
                    .padding(16.dp)

            ) {
                // Fila superior: dos widgets lado a lado
                Row(modifier = Modifier.fillMaxWidth().weight(0.35f),
                    horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.Top) {
                    SearchBar(
                        text = searchText,
                        onTextChange = onSearchTextChange,
                        onSearch = { onNavigate(Screen.RESULTS) })
                }

                Column(modifier = Modifier.fillMaxWidth(0.3f).weight(0.1f).padding(start = 52.dp),) {
                    Text("Tareas Prioritarias", fontSize = 24.sp)
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 24.dp),
                        thickness = 5.dp
                    )
                }
                Row(modifier = Modifier.fillMaxWidth().weight(0.55f),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.Center ) {
                    val group = store.tasks().groupBy { it.topicId }
                    val items = group.entries.toList().take(2)
                    items.forEach { (titulo, tareasGrupo) ->
                        val topicName = store.topics().find { it.id == titulo }?.name ?: "Sin tópico"
                        UpcomingTasksPanel(store, tareasGrupo, topicName, false)
                    }

                }

                Row(modifier = Modifier.fillMaxWidth().weight(0.1f).padding(bottom = 16.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center ){
                    IconButton(
                        onClick = { onNavigate(Screen.TASKS)
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .border(1.dp, Color.Gray, CircleShape)
                    ) {
                        Text(
                            text = "+",
                            color = Color.Gray,
                            fontSize = 24.sp
                        )
                    }

                }

            }
            Column(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight()
                    .padding(16.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Card(modifier = Modifier.weight(1f).padding(8.dp, ), shape = RoundedCornerShape(0.dp)) {

                        Text("Widget C")
                    }
                }
                Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Card(modifier = Modifier.weight(1f).padding(8.dp)) {
                        Text("Widget E")
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(text: String, onTextChange: (String) -> Unit, onSearch: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(top = 32.dp, bottom = 32.dp)
            .background(shape = RoundedCornerShape(32.dp), color = MaterialTheme.colorScheme.surfaceVariant),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        OutlinedTextField(
            value = text,
            modifier = Modifier.fillMaxWidth(0.7f).fillMaxHeight(0.3f),
            shape = RoundedCornerShape(32.dp),
            onValueChange = { onTextChange(it) },
            placeholder = { Text("Buscar...") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = { onSearch() }
            )
        )
    }
}