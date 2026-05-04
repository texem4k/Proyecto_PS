package software.ulpgc.code.application.ui.pages

import Screen
import software.ulpgc.code.application.ui.UpcomingTasksPanel
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import software.ulpgc.code.application.ui.menuTareas
import software.ulpgc.code.architecture.control.commands.CommandLauncher
import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.tasks.Task
import kotlin.time.Clock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import software.ulpgc.code.architecture.control.commands.CommandBuilder
import software.ulpgc.code.architecture.control.commands.CommandType
import software.ulpgc.code.architecture.control.optimizer.TaskOptimizer
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlin.sequences.forEach

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
    store: Storage,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onEdit: (Task) -> Unit = {},
    onDeleted: () -> Unit = {},
    onSearch: () -> Unit,
    onSettingsClick: () -> Unit = {}
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Dentro de HomeScreen, antes del Box:
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    var selectedDate by remember { mutableStateOf(today) }
    var version by remember { mutableStateOf(0) }

    val sampleEntries = remember(version) {
        val topicsById = store.topics().associateBy { it.id }

        val tasks = store.tasks()

        // Construimos el mapa manualmente: día → lista de entries
        val map = mutableMapOf<LocalDate, MutableList<SampleEntry>>()

        tasks.forEach { task ->
            val startDate = task.time.start.toLocalDateTime(TimeZone.currentSystemDefault()).date
            val endDate   = task.time.end.toLocalDateTime(TimeZone.currentSystemDefault()).date

            // Iteramos todos los días desde startDate hasta endDate (inclusive)
            var current = startDate
            while (current <= endDate) {
                val startTime = task.time.start.toLocalDateTime(TimeZone.currentSystemDefault())
                val endTime   = task.time.end.toLocalDateTime(TimeZone.currentSystemDefault())
                val topicColor = (topicsById[task.topicId]?.color ?: 0xFF9E9E9E.toInt()) or 0xFF000000.toInt()

                val entry = SampleEntry(
                    title = task.name,
                    time  = "${startTime.hour.toString().padStart(2, '0')}:${startTime.minute.toString().padStart(2, '0')} · " +
                            "${endTime.hour.toString().padStart(2, '0')}:${endTime.minute.toString().padStart(2, '0')}",
                    color = Color(topicColor),
                    task  = task
                )

                map.getOrPut(current) { mutableListOf() }.add(entry)
                current = current.plus(1, DateTimeUnit.DAY)
            }
        }

        map
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

            SideBar(
                selectedScreen = Screen.HOME,
                onNavigate = onNavigate,
                onSettingsClick = onSettingsClick
            )

            Column(
                modifier = Modifier
                    .weight(2.7f)
                    .fillMaxHeight()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.16f),
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
                        .fillMaxWidth()
                        .padding(start = 52.dp),
                    verticalArrangement = Arrangement.Top,

                ) {
                    Text("Tareas Prioritarias", fontSize = 24.sp)
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .padding(top = 4.dp, bottom = 16.dp),
                        thickness = 5.dp
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.65f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(TaskOptimizer.sortedTasks.toList().slice(0..minOf(3, TaskOptimizer.sortedTasks.toList().lastIndex))) { task ->

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { },
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                IconButton(
                                    onClick = {
                                        val command = CommandBuilder(store)
                                            .set("id", task.id.toString())
                                            .build(CommandType.MARK_COMPLETE)

                                        command
                                            .onSuccess { CommandLauncher.launch(it) }
                                            .onFailure { println("error: ${it.message}") }

                                        onDeleted()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CheckCircle,
                                        contentDescription = "Completar tarea",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(Modifier.width(8.dp))

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
                                        text = "${store.topics().find { it.id == task.topicId }?.name ?: "Sin tópico"} $endDate $endHour",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.1f)
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(300.dp)
                    ) {
                        DialMenu(
                            onCreateTask = { onNavigate(Screen.TASKS_CREATE) },
                            onCreateTopic = { onNavigate(Screen.TOPIC_CREATE) },
                            onCreateTag = { onNavigate(Screen.TAG_CREATE) }
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
                            store = store,
                            onNavigate = onNavigate,
                            onTaskCreated = { version++ },
                            onDeleted = { version-- },
                            onEdit = { version++ }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    menuTareas(store)
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
            modifier = Modifier.fillMaxWidth(0.7f).fillMaxHeight(),
            shape = RoundedCornerShape(32.dp),
            onValueChange = { onTextChange(it) },
            placeholder = { Text("Buscar...") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() })
        )
    }
}