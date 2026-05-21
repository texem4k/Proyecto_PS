package software.ulpgc.code.application.ui.pages

import Screen
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import software.ulpgc.code.application.ui.DialMenu
import software.ulpgc.code.application.ui.SideBar
import software.ulpgc.code.application.ui.widgets.MenuTareas
import software.ulpgc.code.architecture.control.commands.CommandLauncher
import software.ulpgc.code.architecture.model.tasks.Task
import kotlin.time.Clock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import software.ulpgc.code.architecture.control.optimizer.TaskOptimizer
import software.ulpgc.code.application.ui.MarkTaskIcon
import software.ulpgc.code.application.ui.TaskInformationDialog
import software.ulpgc.code.application.ui.filters.TaskFilters
import software.ulpgc.code.application.ui.pages.Calendar.Views.HomeCalendar
import software.ulpgc.code.application.ui.pages.Calendar.getFilteredEntries
import software.ulpgc.code.application.ui.toFormattedDateDisplay
import software.ulpgc.code.application.ui.toFormattedHour
import software.ulpgc.code.architecture.io.Store
import software.ulpgc.code.isDesktop

data class DialMenuItem(
    val icon: ImageVector,
    val label: String,
    val color: Color,
    val onClick: () -> Unit
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigate: (Screen) -> Unit,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onEdit: (Task) -> Unit = {},
    onDeleted: () -> Unit = {},
    onSearch: () -> Unit,
    onSettingsClick: () -> Unit = {}
) {
    val focusRequester = remember { FocusRequester() }
    var selectedTask by remember { mutableStateOf<Task?>(null) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    var selectedDate by remember { mutableStateOf(today) }
    var version by remember { mutableStateOf(0) }



    val sampleEntries = remember(version) {
        getFilteredEntries(TaskFilters(true, setOf("No completadas")))
    }

    val priorityTask = remember (version) { TaskOptimizer.sortedTasks.toList() }

    Box(
        modifier = setUndoRedo({ version++; onDeleted() } , focusRequester)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {

            if(isDesktop()){
                SideBar(
                    selectedScreen = Screen.HOME,
                    onNavigate = onNavigate,
                    onSettingsClick = onSettingsClick,
                    onRefresh={version++},
                    version = version
                )
            }

            Column(
                modifier = Modifier
                    .weight(2.7f)
                    .fillMaxHeight()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.17f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Top
                ) {
                    SearchBar(
                        text = searchText,
                        onTextChange = onSearchTextChange,
                        onSearch = onSearch
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Top,

                    ) {
                    Text(
                        "Tareas Prioritarias",
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onBackground)
                    Divider(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .padding(top = 4.dp, bottom = 16.dp),
                        thickness = 5.dp
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.5f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (priorityTask.isEmpty()) {
                        item {
                            Text(
                                "No hay tareas prioritarias asignadas, ¡Disfruta del día \uD83D\uDE09!",
                                fontSize = 18.sp
                            )
                        }
                    } else {
                        items(priorityTask) { task ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedTask = task },
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    MarkTaskIcon(task, onDeleted = {
                                        onDeleted()
                                        version++
                                        focusRequester.requestFocus()
                                    })
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = task.name,
                                            style = MaterialTheme.typography.titleSmall,
                                        )

                                        Spacer(Modifier.height(4.dp))
                                        val tz = TimeZone.currentSystemDefault()
                                        val endDate = task.time.end.toFormattedDateDisplay(tz)
                                        val endHour = task.time.end.toFormattedHour(tz)

                                        Text(
                                            text = "${
                                                Store.topics().find { it.id == task.topicId }?.name ?: "Sin tópico"
                                            } $endDate $endHour",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                ShowDialMenu(
                    { onNavigate(Screen.TASKS_CREATE) },
                    { onNavigate(Screen.TOPIC_CREATE) },
                    { onNavigate(Screen.TAG_CREATE) },
                    {onNavigate(Screen.GROUP_CREATE) },
                    modifier=Modifier.fillMaxWidth().weight(0.1f)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        HomeCalendar(
                            sampleEntries = sampleEntries,
                            selectedDate = selectedDate,
                            onDateSelected = { selectedDate = it },
                            onNavigate = onNavigate,
                            onTaskCreated = { version++ },
                            onDeleted = { version-- },
                            onEdit = { version++ }
                        )
                    }
                }

                ShowNearAndCompleteTasks(
                    modifier = Modifier.weight(1f),
                    onDeleted = {
                        version++
                        onDeleted()
                        focusRequester.requestFocus()
                    },
                    refreshFlag = version
                )
            }
        }
    }
    if (selectedTask != null) {
        TaskInformationDialog(
            selectedTask = selectedTask!!,
            onDismiss = { selectedTask = null },
            onDeleted = {
                version++
                selectedTask = null
            },
            onEdit = {
                onEdit(selectedTask!!)
            }
        )
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
            modifier = Modifier.fillMaxWidth(0.7f).height(56.dp),
            shape = RoundedCornerShape(32.dp),
            onValueChange = { onTextChange(it) },
            placeholder = { Text("Buscar...") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() })
        )
    }
}


fun setUndoRedo(onDeleted: () -> Unit, focusRequest: FocusRequester): Modifier{
    return Modifier
        .fillMaxSize()
        .focusRequester(focusRequest)
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
}



@Composable
fun ShowDialMenu(
    onCreateTask: () -> Unit,
    onCreateTopic: () -> Unit,
    onCreateTag: () -> Unit,
    onCreateGroup: () -> Unit,
    modifier: Modifier
) {
    Row(
        modifier = modifier.padding(bottom = 16.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(300.dp)
        ) {
            DialMenu(
                onCreateTask = onCreateTask,
                onCreateTopic = onCreateTopic,
                onCreateTag = onCreateTag,
                onCreateGroup = onCreateGroup
            )
        }
    }
}



@Composable
fun ShowNearAndCompleteTasks(modifier: Modifier, onDeleted: () -> Unit, refreshFlag: Int = 0){
    Row(
        modifier = modifier
            .fillMaxWidth()
    ) {
        MenuTareas(onDeleted = onDeleted, refreshFlag)
    }
}
