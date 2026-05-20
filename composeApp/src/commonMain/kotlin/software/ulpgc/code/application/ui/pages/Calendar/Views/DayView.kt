package software.ulpgc.code.application.ui.pages.Calendar.Views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import software.ulpgc.code.application.ui.CreateTask
import software.ulpgc.code.application.ui.pages.Calendar.CalendarConstants
import software.ulpgc.code.application.ui.pages.Calendar.CalendarHeader
import software.ulpgc.code.application.ui.pages.Calendar.CalendarViewMode
import software.ulpgc.code.application.ui.pages.Calendar.END_HOUR
import software.ulpgc.code.application.ui.pages.Calendar.HOUR_HEIGHT
import software.ulpgc.code.application.ui.pages.Calendar.START_HOUR
import software.ulpgc.code.application.ui.pages.Calendar.SampleEntry
import software.ulpgc.code.application.ui.pages.Calendar.TIME_COL_W
import software.ulpgc.code.application.ui.pages.Calendar.WeekDayColumn
import software.ulpgc.code.application.ui.pages.urgencyColorFromEntries // 🔹 NUEVO: para barra de prioridad
import software.ulpgc.code.application.ui.pages.DayDetailDialog
import software.ulpgc.code.architecture.control.commands.CommandBuilder
import software.ulpgc.code.architecture.control.commands.CommandLauncher
import software.ulpgc.code.architecture.control.commands.CommandType
import software.ulpgc.code.architecture.io.Store
import software.ulpgc.code.architecture.model.tasks.Task
import kotlin.collections.map

import kotlin.time.Clock

@Composable
fun DayView(
    sampleEntries: Map<LocalDate, List<SampleEntry>>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    viewMode: CalendarViewMode,
    onViewModeChange: (CalendarViewMode) -> Unit,
    onTaskCreated: () -> Unit,
    onDeleted: () -> Unit,
    onEdit: () -> Unit,
    onFilterClick: () -> Unit
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val scrollState = rememberScrollState()
    val totalHours = END_HOUR - START_HOUR
    val totalHeightDp = HOUR_HEIGHT * totalHours
    var selectedEntry by remember { mutableStateOf<SampleEntry?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    var dayOffset by remember { mutableStateOf(0) }
    val currentDay = remember(dayOffset) {
        today.plus(DatePeriod(days = dayOffset))
    }

    val urgencyColor = remember(
        currentDay,
        sampleEntries[currentDay]?.map {it.title to it.task?.priority}
    ) {
        urgencyColorFromEntries(sampleEntries[currentDay] ?: emptyList())
    }

    LaunchedEffect(Unit) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val nowFraction = (now.hour - START_HOUR).coerceAtLeast(0)
        scrollState.animateScrollTo((nowFraction * HOUR_HEIGHT.value - 100).toInt().coerceAtLeast(0))
    }

    Column(modifier = Modifier.fillMaxSize()) {

        CalendarHeader(
            title = "${CalendarConstants.DAY_NAMES_ES[currentDay.dayOfWeek.ordinal]} ${currentDay.dayOfMonth} de ${CalendarConstants.MONTH_NAMES_ES[currentDay.month.ordinal]} ${currentDay.year}",
            onPreviousClick = { dayOffset-- },
            onNextClick = { dayOffset++ },
            viewMode = viewMode,
            onViewModeChange = onViewModeChange,
            scrollState = scrollState,
            onFilterClick = onFilterClick,
            modifier = Modifier
        )

        val allDayEntries = (sampleEntries[currentDay] ?: emptyList())
            .filter { it.time == "Vence hoy" || it.time == "Sin hora" }

        if (allDayEntries.isNotEmpty()) {
            AllDayStrip(entries = allDayEntries)
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Box(
                modifier = Modifier
                    .width(TIME_COL_W)
                    .height(totalHeightDp)
            ) {
                for (h in START_HOUR..END_HOUR) {
                    val topDp = ((h - START_HOUR) * HOUR_HEIGHT.value).dp
                    Text(
                        text = if (h < 10) "0$h:00" else "$h:00",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .offset(y = topDp - 7.dp)
                            .fillMaxWidth()
                            .padding(end = 6.dp),
                        textAlign = TextAlign.End
                    )
                }
            }

            val timedEntries = (sampleEntries[currentDay] ?: emptyList())
                .filter { it.time != "Vence hoy" && it.time != "Sin hora" }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(totalHeightDp)
                    .border(0.5.dp, Color.Gray.copy(alpha = 0.15f))
                    .clickable { showCreateDialog = true }
            ) {
                WeekDayColumn(
                    date = currentDay,
                    entries = timedEntries,
                    isToday = currentDay == today,
                    isSelected = true,
                    hourHeight = HOUR_HEIGHT,
                    onEntryClick = { entry -> selectedEntry = entry },
                    urgencyColor = urgencyColor
                )
            }
        }

        var showCreateTask by remember { mutableStateOf(false) }
        var taskToEdit by remember { mutableStateOf<Task?>(null) }

        selectedEntry?.let { entry ->
            val task = entry.task
            if (task != null) {
                val topicName = Store.topics().find { it.id == task.topicId }?.name ?: "Sin tópico"
                val tagNames = task.tags.mapNotNull { id ->
                    Store.tags().associateBy { it.id }[id]?.name
                }
                AlertDialog(
                    onDismissRequest = { selectedEntry = null },
                    title = { Text(task.name, fontWeight = FontWeight.Bold) },
                    text = {
                        Text(
                            "Descripción: ${task.description}\n" +
                                    "Tema: $topicName\n" +
                                    "Tags: ${tagNames.joinToString(", ")}\n" +
                                    "Fecha de comienzo: ${task.time.start}\n" +
                                    "Fecha de final: ${task.time.end}\n" +
                                    "Prioridad: ${task.priority}"
                        )
                    },
                    confirmButton = {
                        Button(onClick = { selectedEntry = null }) { Text("Cerrar") }
                        Button(onClick = {
                            val command = CommandBuilder().set("id", task.id.toString()).build(CommandType.DELETE_TASK)
                            command
                                .onSuccess { CommandLauncher.launch(it) }
                                .onFailure { println("error: ${it.message}") }
                            selectedEntry = null
                            onDeleted()
                        }) { Text("Eliminar tarea") }
                        Button(onClick = {
                            taskToEdit = task
                            showCreateTask = true
                            selectedEntry = null
                        }) { Text("Editar tarea") }
                    },
                    shape = RoundedCornerShape(16.dp)
                )
            }
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

        if (showCreateDialog) {
            DayDetailDialog(
                date = currentDay,
                entries = sampleEntries[currentDay] ?: emptyList(),
                onTaskCreated = onTaskCreated,
                onDismiss = { showCreateDialog = false },
                onDeleted = onDeleted,
                onEdit = onEdit
            )
        }
    }
}

@Composable
fun AllDayStrip(entries: List<SampleEntry>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F7FB))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Todo el día",
            fontSize = 10.sp,
            color = Color.Gray,
            modifier = Modifier.width(TIME_COL_W - 8.dp),
            textAlign = TextAlign.End
        )
        entries.forEach { entry ->
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(entry.color.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(entry.color, CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = entry.title, fontSize = 11.sp, color = entry.color)
            }
        }
    }
}
