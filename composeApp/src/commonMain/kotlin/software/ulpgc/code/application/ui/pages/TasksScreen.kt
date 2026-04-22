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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import software.ulpgc.code.application.ui.Screen
import software.ulpgc.code.application.ui.filters.FilterContent
import software.ulpgc.code.application.ui.filters.TaskFilters
import software.ulpgc.code.application.ui.pages.SearchBar
import software.ulpgc.code.architecture.control.CommandLauncher
import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.tasks.Task
import kotlin.collections.component1
import kotlin.collections.component2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksSreen(
    onNavigate: (Screen) -> Unit,
    store: Storage,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    filters: TaskFilters,
    onEdit: (Task) -> Unit = {},
    onDeleted: () -> Unit = {}
) {
    var showFilters by remember { mutableStateOf(false) }
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

            Column(
                modifier = Modifier
                    .weight(0.5f)
                    .fillMaxHeight()
                    .background(Color(0xFF1E1E2E))
                    .padding(16.dp)
            ) {
                Text("📁 Archivos", color = Color.White)
                Text("⚙️ Ajustes", color = Color.White)
                Text("👤 Perfil", color = Color.White)
            }

            Column(
                modifier = Modifier
                    .weight(4f)
                    .fillMaxHeight()
                    .padding(16.dp)

            ){
            Button(onClick = { showFilters = true }) {
                Text("Filtrado de tareas")
            }

                // Fila superior: dos widgets lado a lado
                Row(modifier = Modifier.fillMaxWidth().weight(0.35f),
                    horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.Top) {
                    SearchBar(
                        text = searchText,
                        onTextChange = onSearchTextChange,
                        onSearch = { onNavigate(Screen.RESULTS) })
                    Row {
                        if (showFilters) {
                            ModalBottomSheet(
                                onDismissRequest = { showFilters = false }
                            ) {
                                FilterContent(
                                    onApply = { newFilters ->
                                        filters.topics = newFilters.topics
                                        filters.status = newFilters.status
                                        filters.priority = newFilters.priority
                                        filters.hasFilter = newFilters.hasFilter
                                        showFilters = false
                                        onNavigate(Screen.RESULTS)
                                    }, store,
                                    onNavigate = onNavigate,
                                    onDismiss = { showFilters = false }

                                )
                            }
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth().weight(0.55f),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.Center ) {
                    val group = store.tasks().groupBy { it.topicId }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxWidth(0.5f),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(32.dp),
                        verticalArrangement = Arrangement.spacedBy(64.dp)
                    ) {
                        items(group.entries.toList()) { (titulo, tareasGrupo) ->
                            val topicName = store.topics().find { it.id == titulo }?.name ?: "Sin tópico"
                            UpcomingTasksPanel(store, tareasGrupo, topicName, false, onEdit = onEdit)
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth().weight(0.1f).padding(bottom = 16.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center ){
                    IconButton(
                        onClick = { },
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
        }
    }
}