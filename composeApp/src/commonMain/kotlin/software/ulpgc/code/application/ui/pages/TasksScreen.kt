import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import software.ulpgc.code.application.ui.SideBar
import software.ulpgc.code.application.ui.CRUDs.CreateTagDialog
import software.ulpgc.code.application.ui.CRUDs.CreateTopicDialog
import software.ulpgc.code.application.ui.filters.FilterContent
import software.ulpgc.code.application.ui.filters.TaskFilters
import software.ulpgc.code.application.ui.CreateTask
import software.ulpgc.code.application.ui.CreateGroup
import software.ulpgc.code.application.ui.pages.SearchBar
import software.ulpgc.code.application.ui.pages.ShowDialMenu
import software.ulpgc.code.application.ui.pages.setUndoRedo
import software.ulpgc.code.architecture.io.Store
import software.ulpgc.code.architecture.model.tasks.Task

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    onNavigate: (Screen) -> Unit,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    filters: TaskFilters,
    onEdit: (Task) -> Unit = {},
    onCreated: () -> Unit = {},
    onDeleted: () -> Unit = {},
    autoOpen: AutoOpen? = null,
    taskToEdit: Task? = null,
    onEditDone: () -> Unit = {},
    onShowResults: (Boolean) -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    var showFilters by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    var showCreateTaskcopy by remember { mutableStateOf(false) }
    var showCreateTopic by remember { mutableStateOf(false) }
    var showCreateTag by remember { mutableStateOf(false) }
    var showEditTask by remember { mutableStateOf(false) }
    var showCreateGroup by remember { mutableStateOf(false) }
    var version by remember { mutableStateOf(0) }
    val taskList = remember(version) {
        Store.tasks().toList()
    }
    val group = remember(version) {
        taskList
            .filter { !it.isCompleted }
            .groupBy { it.topicId }
    }

    LaunchedEffect(taskToEdit) {
        if (taskToEdit != null) showEditTask = true
    }

    LaunchedEffect(autoOpen) {
        when (autoOpen) {
            AutoOpen.TASK  -> showCreateTaskcopy = true
            AutoOpen.TOPIC -> showCreateTopic = true
            AutoOpen.TAG   -> showCreateTag = true
            null           -> {}
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(modifier = setUndoRedo(onDeleted, focusRequester)) {
        Row(modifier = Modifier.fillMaxSize()) {

            SideBar(
                onNavigate = onNavigate,
                selectedScreen = Screen.TASKS,
                onSettingsClick = onSettingsClick,
                onRefresh = {version++},
                version=version
            )

            Column(
                modifier = Modifier
                    .weight(4f)
                    .fillMaxHeight()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.17f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SearchBar(
                        text = searchText,
                        onTextChange = onSearchTextChange,
                        onSearch = {
                            filters.hasFilter = false
                            onShowResults(true)
                        }
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
                                filters.tags = newFilters.tags
                                filters.hasFilter = newFilters.hasFilter
                                showFilters = false
                                onShowResults(true)
                            },
                            onDismiss = { showFilters = false }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.73f),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (!Store.tasks().any { t -> !t.isCompleted }) {
                        Text(
                            "No tienes ninguna tarea ahora mismo, ¡Puedes descansar un poco \uD83D\uDE09!",
                            fontSize = 18.sp
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.fillMaxWidth(0.5f),
                            contentPadding = PaddingValues(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(32.dp),
                            verticalArrangement = Arrangement.spacedBy(64.dp)
                        ) {
                            items(group.entries.toList()) { (titulo, tareasGrupo) ->
                                val topicName =
                                    Store.topics().find { it.id == titulo }?.name ?: "Sin tópico"

                                _root_ide_package_.software.ulpgc.code.application.ui.UpcomingTasksPanel(
                                    tareasGrupo,
                                    topicName,
                                    onEdit = { task ->
                                        onEdit(task)
                                        showEditTask = true
                                    },
                                    onDeleted = {
                                        version++
                                        onDeleted()
                                    },
                                    screen = Screen.TASKS
                                )
                            }
                        }
                    }
                }

                ShowDialMenu(
                    onCreateTask  = { showCreateTaskcopy = true },
                    onCreateTopic = { showCreateTopic = true },
                    onCreateTag   = { showCreateTag = true },
                    onCreateGroup = { showCreateGroup = true },
                    modifier = Modifier.fillMaxWidth().weight(0.1f)
                )
            }
        }
    }

    if (showCreateTaskcopy) {
        Dialog(
            onDismissRequest = { },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {

            CreateTask(
                onClose = {
                    showCreateTaskcopy = false
                    onCreated()
                    onNavigate(Screen.TASKS)
                    version++
                }
            )

        }
    }

    if (showEditTask && taskToEdit != null) {
        Dialog(
            onDismissRequest = { showEditTask = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            CreateTask(
                task = taskToEdit,
                onClose = {
                    showEditTask = false
                    onEditDone()
                    onCreated()
                    version++
                }
            )
        }
    }

    if (showCreateTopic) {
        CreateTopicDialog(
            onClose = {
                showCreateTopic = false
                onCreated()
                version++
            }
        )
    }

    if (showCreateTag) {
        CreateTagDialog(
            onClose = {
                showCreateTag = false
                onCreated()
                version++
            },
            null
        )
    }

    if (showCreateGroup) {
        CreateGroup(
            onClose = {
                showCreateGroup = false
                onCreated()
                version++
            },
            onSubmit = { version++ }
        )
    }
}