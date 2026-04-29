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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import software.ulpgc.code.application.ui.DialMenu
import software.ulpgc.code.application.ui.SideBar
import software.ulpgc.code.application.ui.filters.CreateTagDialog
import software.ulpgc.code.application.ui.filters.CreateTopicDialog
import software.ulpgc.code.application.ui.filters.FilterContent
//import software.ulpgc.code.application.ui.filters.FilterContent
import software.ulpgc.code.application.ui.filters.TaskFilters
import software.ulpgc.code.application.ui.pages.CreateTask
import software.ulpgc.code.application.ui.pages.SearchBar
import software.ulpgc.code.architecture.control.commands.CommandLauncher
import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.tasks.Task
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    onNavigate: (Screen) -> Unit,
    store: Storage,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    filters: TaskFilters,
    onEdit: (Task) -> Unit = {},
    onDeleted: () -> Unit = {},
    autoOpenCreate: Boolean = false
) {
    var showFilters by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    var showCreateTaskcopy by remember { mutableStateOf(false) }
    var showCreateTopic by remember { mutableStateOf(false) }
    var showCreateTag by remember { mutableStateOf(false) }

    LaunchedEffect(autoOpenCreate) {
        if (autoOpenCreate) showCreateTaskcopy = true
    }

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

            SideBar(selectedScreen = Screen.TASKS,  // valor fijo según la pantalla
                onNavigate = onNavigate)

            Column(
                modifier = Modifier
                    .weight(4f)
                    .fillMaxHeight()
                    .padding(16.dp)
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.30f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SearchBar(
                        text = searchText,
                        onTextChange = onSearchTextChange,
                        onSearch = { onNavigate(Screen.RESULTS) }
                    )

                    IconButton(onClick = { showFilters = true }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filtrar tareas",
                            tint = Color.Gray
                        )
                    }
                }

                if (showFilters) {
                    ModalBottomSheet(
                        onDismissRequest = { showFilters = false },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        FilterContent(
                            onApply = { newFilters ->
                                filters.topics = newFilters.topics
                                filters.status = newFilters.status
                                filters.priority = newFilters.priority
                                filters.hasFilter = newFilters.hasFilter
                                showFilters = false
                                onNavigate(Screen.RESULTS)
                            },
                            store,
                            onNavigate = onNavigate,
                            onDismiss = { showFilters = false }
                        )




                    }
                }

                // Grid de tareas
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.60f),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.Center
                ) {
                    var taskList by remember { mutableStateOf(store.tasks().toList()) }
                    val group = taskList.groupBy { it.topicId }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxWidth(0.5f),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(32.dp),
                        verticalArrangement = Arrangement.spacedBy(64.dp)
                    ) {
                        items(group.entries.toList()) { (titulo, tareasGrupo) ->
                            val topicName = store.topics().find { it.id == titulo }?.name ?: "Sin tópico"
                            UpcomingTasksPanel(
                                store,
                                tareasGrupo,
                                topicName,
                                onEdit = onEdit,
                                onDeleted = {
                                    taskList = store.tasks().toList()
                                    onDeleted()
                                },
                                screen = Screen.TASKS
                            )                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.1f)
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                )
                {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(300.dp)
                    ) {
                        DialMenu(onNavigate = onNavigate,
                            onCreateTask = { showCreateTaskcopy = true },
                            onCreateTopic = { showCreateTopic = true },
                            onCreateTag = { showCreateTag = true }
                        )
                    }
                }
            }
        }
    }
    if (showCreateTaskcopy) {
        Dialog(
            onDismissRequest = { showCreateTaskcopy = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .fillMaxHeight(0.9f),
                shape = RoundedCornerShape(16.dp)
            ) {
                CreateTask(store = store, onClose = { showCreateTaskcopy = false })
            }
        }
    }

    if (showCreateTopic) {
        CreateTopicDialog(
            store = store,
            onClose = { showCreateTopic = false }
        )
    }

    if (showCreateTag) {
        CreateTagDialog(
            store = store,
            onClose = { showCreateTag = false }
        )
    }
}
