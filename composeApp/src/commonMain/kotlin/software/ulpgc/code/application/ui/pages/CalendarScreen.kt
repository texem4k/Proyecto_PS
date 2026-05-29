package software.ulpgc.code.application.ui.pages

import Screen
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import software.ulpgc.code.application.ui.widgets.SideBar
import software.ulpgc.code.application.ui.filters.FilterContent
import software.ulpgc.code.application.ui.filters.TaskFilters
import software.ulpgc.code.architecture.model.tasks.Task
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.focus.FocusRequester
import software.ulpgc.code.application.ui.pages.Calendar.CalendarViewMode
import software.ulpgc.code.application.ui.pages.Calendar.SampleEntry
import software.ulpgc.code.application.ui.dialogs.CreateTask
import software.ulpgc.code.application.ui.dialogs.TaskInformationDialog
import software.ulpgc.code.application.ui.pages.Calendar.Views.DayView
import software.ulpgc.code.application.ui.pages.Calendar.Views.MonthView
import software.ulpgc.code.application.ui.pages.Calendar.Views.WeekView
import software.ulpgc.code.application.ui.pages.Calendar.Views.YearView
import software.ulpgc.code.application.ui.pages.Calendar.getFilteredEntries
import software.ulpgc.code.architecture.model.tasks.MAX

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onNavigate: (Screen) -> Unit,
    onSettingsClick: () -> Unit,

) {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    var version by remember { mutableStateOf(0) }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val onTaskCreated: () -> Unit = { version++ }
    val onDeleted: () -> Unit = { version++ }
    val onEdit: () -> Unit = { version++ }

    var selectedDate by remember { mutableStateOf(today) }
    var viewMode by remember { mutableStateOf(CalendarViewMode.MES) }
    var showFilters by remember { mutableStateOf(false) }
    var filters by remember { mutableStateOf(TaskFilters(true, setOf("No completadas"))) }

    val filteredEntries = remember(version, filters) {
        getFilteredEntries(filters)
    }

    Row(modifier = setUndoRedo(onDeleted, focusRequester)) {
        SideBar(
            onNavigate = onNavigate,
            selectedScreen = Screen.CALENDAR,
            onSettingsClick = onSettingsClick,
            onRefresh = {version++},
            version=version
        )

        Box(modifier = Modifier.fillMaxSize()) {
            when (viewMode) {
                CalendarViewMode.MES -> MonthView(
                    sampleEntries = filteredEntries,
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it },
                    viewMode = viewMode,
                    onViewModeChange = { viewMode = it },
                    onNavigate = onNavigate,
                    onTaskCreated = onTaskCreated,
                    onDeleted = onDeleted,
                    onEdit = onEdit,
                    onFilterClick = { showFilters = true }
                )

                CalendarViewMode.DIA -> DayView(
                    sampleEntries = filteredEntries,
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it },
                    viewMode = viewMode,
                    onViewModeChange = { viewMode = it },
                    onTaskCreated = onTaskCreated,
                    onDeleted = onDeleted,
                    onEdit = onEdit,
                    onFilterClick = { showFilters = true }
                )

                CalendarViewMode.SEMANA -> WeekView(
                    sampleEntries = filteredEntries,
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it },
                    viewMode = viewMode,
                    onViewModeChange = { viewMode = it },
                    onTaskCreated = onTaskCreated,
                    onDeleted = onDeleted,
                    onEdit = onEdit,
                    onFilterClick = { showFilters = true },
                )

                CalendarViewMode.AÑO -> YearView(
                    sampleEntries = filteredEntries,
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it },
                    viewMode = viewMode,
                    onViewModeChange = { viewMode = it },
                    onNavigate = onNavigate,
                    onTaskCreated = onTaskCreated,
                    onDeleted = onDeleted,
                    onEdit = onEdit,
                    onFilterClick = { showFilters = true }
                )
            }

            if (showFilters) {
                ModalBottomSheet(
                    onDismissRequest = { showFilters = false },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ) {
                    FilterContent(
                        onApply = { newFilters ->
                            filters = newFilters.copy(
                                hasFilter = newFilters.priority.isNotEmpty() ||
                                        newFilters.topics.isNotEmpty() ||
                                        newFilters.tags.isNotEmpty()
                            )
                            showFilters = false
                        },
                        onDismiss = { showFilters = false }
                    )
                }
            }
        }
    }
}
@Composable
fun DayEntriesPanel(
    date: LocalDate,
    entries: List<SampleEntry>,
    modifier: Modifier = Modifier,
    onDeleted: () -> Unit,
    onEdit: () -> Unit
) {
    var selectedEntry by remember { mutableStateOf<SampleEntry?>(null) }
    var showCreateTask by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }

    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "Entradas para $date", color = MaterialTheme.colorScheme.onPrimaryContainer)

        if (entries.isEmpty()) {
            Text(text = "No hay eventos para este día", color = MaterialTheme.colorScheme.onPrimaryContainer)
        } else {
            entries.forEach { entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedEntry = entry }
                        .background(
                            color = MaterialTheme.colorScheme.tertiary,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(7.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = entry.title, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(text = entry.time, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
    }

    selectedEntry?.task?.let { task ->
        TaskInformationDialog(
            selectedTask = task,
            onDismiss = { selectedEntry = null },
            onEdit = { editedTask ->
                taskToEdit = editedTask
                showCreateTask = true
                selectedEntry = null
            },
            onDeleted = {
                selectedEntry = null
                onDeleted()
            }
        )
    }

    if (showCreateTask) {
        Dialog(
            onDismissRequest = { showCreateTask = false },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            CreateTask(
                onClose = {
                    showCreateTask = false
                    taskToEdit = null
                    onEdit()
                },
                task = taskToEdit
            )
        }
    }
}

@Composable
fun DayDetailDialog(
    date: LocalDate,
    entries: List<SampleEntry>,
    onTaskCreated: () -> Unit,
    onDismiss: () -> Unit,
    onDeleted: () -> Unit,
    onEdit: () -> Unit
) {
    var showCreateTask by remember { mutableStateOf(false) }

    if (!showCreateTask) {
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.background,
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "${date.dayOfMonth}/${date.monthNumber}/${date.year}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            },
            text = {
                DayEntriesPanel(date = date, entries = entries, onDeleted = onDeleted, onEdit = onEdit)
            },
            confirmButton = {
                Row {
                    Button(onClick = { showCreateTask = true }) { Text("Crear Tarea") }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = onDismiss) { Text("Cerrar", color = Color(0xFF4F6EF7)) }
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showCreateTask) {
        Dialog(
            onDismissRequest = { showCreateTask = false },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            CreateTask(
                initialDate = date,
                onClose = {
                    showCreateTask = false
                    onTaskCreated()
                }
            )
        }
    }

}
// ── HomeCalendar ──────────────────────────────────────────────────────────────


fun urgencyColorFromEntries(entries: List<SampleEntry>): Color {
    val priorities = entries.mapNotNull { it.task?.priority }
    if (priorities.isEmpty()) return Color.Transparent
    val t = (priorities.map{it.value}.average() / MAX).coerceIn(0.0, 1.0).toFloat()
    return Color(red = t, green = 1f - t, blue = 0f, alpha = 0.6f)
}