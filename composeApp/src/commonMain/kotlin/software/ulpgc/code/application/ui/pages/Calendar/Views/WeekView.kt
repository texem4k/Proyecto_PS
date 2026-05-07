package software.ulpgc.code.application.ui.pages.Calendar.Views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import kotlinx.datetime.minus
import kotlinx.datetime.plus
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
import software.ulpgc.code.application.ui.pages.DayDetailDialog
import software.ulpgc.code.application.ui.pages.Calendar.WeekDayColumn
import software.ulpgc.code.application.ui.pages.urgencyColorFromEntries
import software.ulpgc.code.architecture.control.commands.CommandBuilder
import software.ulpgc.code.architecture.control.commands.CommandLauncher
import software.ulpgc.code.architecture.control.commands.CommandType
import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.tasks.Task
import kotlin.time.Clock



@Composable
fun WeekView(
    viewMode: CalendarViewMode,
    onViewModeChange: (CalendarViewMode) -> Unit,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    sampleEntries: Map<LocalDate, List<SampleEntry>>,
    store: Storage,
    onTaskCreated: () -> Unit,
    onDeleted: () -> Unit,
    onEdit: () -> Unit,
    onFilterClick: () -> Unit
) {
    val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val scrollState = rememberScrollState()
    val totalHours = END_HOUR - START_HOUR
    val totalHeightDp = HOUR_HEIGHT * totalHours
    var weekOffset by remember { mutableStateOf(0) }
    var selectedEntry by remember { mutableStateOf<SampleEntry?>(null) }
    var clickedDate by remember { mutableStateOf<LocalDate?>(null) }

    val weekStart = remember(weekOffset) {
        val daysSinceMonday = currentDate.dayOfWeek.ordinal
        currentDate
            .minus(DatePeriod(days = daysSinceMonday))
            .plus(DatePeriod(days = weekOffset * 7))
    }
    val weekDates = remember(weekStart) {
        (0..6).map { weekStart.plus(DatePeriod(days = it)) }
    }
    val startDate = weekDates.first()
    val endDate = weekDates.last()

    Column(modifier = Modifier.fillMaxSize()) {
        CalendarHeader(
            title = if (startDate.month == endDate.month) {
                "${startDate.dayOfMonth} - ${endDate.dayOfMonth} ${CalendarConstants.MONTH_NAMES_ES[startDate.month.ordinal]} ${startDate.year}"
            } else {
                "${startDate.dayOfMonth} ${CalendarConstants.MONTH_NAMES_ES[startDate.month.ordinal]} - " +
                        "${endDate.dayOfMonth} ${CalendarConstants.MONTH_NAMES_ES[endDate.month.ordinal]} ${endDate.year}"
            },
            onPreviousClick = { weekOffset-- },
            onNextClick = { weekOffset++ },
            viewMode = viewMode,
            onViewModeChange = onViewModeChange,
            scrollState = scrollState,
            onFilterClick = onFilterClick,
            modifier = Modifier
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(start = TIME_COL_W)
        ) {

            val dayUrgencyColor = remember(weekOffset, sampleEntries.values.flatten().map { entry -> entry.title to entry.task?.priority }) {
                weekDates.associateWith { date ->
                    urgencyColorFromEntries(sampleEntries[date] ?: emptyList())
                }
            }

            val dayLetters = listOf("L", "M", "X", "J", "V", "S", "D")
            var urgencyColor: Color?;

            weekDates.forEachIndexed { index, date ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = dayLetters[index],
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (date == currentDate) Color(0xFF4F6EF7) else Color.Gray
                    )

                    Box(
                        modifier = Modifier.size(28.dp),  // tamaño fijo igual para todos los días
                        contentAlignment = Alignment.Center
                    ) {
                        // Círculo azul de fondo solo si es hoy
                        if (date == currentDate) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(Color(0xFF4F6EF7), CircleShape)
                            )
                        }

                        Text(
                            text = date.dayOfMonth.toString(),
                            fontSize = 13.sp,
                            fontWeight = when {
                                date == currentDate -> FontWeight.Bold
                                date == selectedDate -> FontWeight.Bold
                                else -> FontWeight.Normal
                            },
                            color = when {
                                date == currentDate -> Color.White
                                date == selectedDate -> Color(0xFF4F6EF7)
                                else -> Color.Black
                            }
                        )
                    }

                    urgencyColor = dayUrgencyColor[date]
                    if (urgencyColor != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .background(dayUrgencyColor[date]!!)
                        )
                    }
                }
            }
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
                    .background(Color.White)
            ) {
                for (h in START_HOUR..END_HOUR) {
                    val topDp = ((h - START_HOUR) * HOUR_HEIGHT.value).dp
                    Text(
                        text = if (h < 10) "0$h:00" else "$h:00",
                        fontSize = 9.sp,
                        color = Color.Black,
                        modifier = Modifier
                            .offset(y = topDp - 7.dp)
                            .fillMaxWidth()
                            .padding(end = 6.dp),
                        textAlign = TextAlign.End
                    )
                }
            }

            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(totalHeightDp)
            ) {
                weekDates.forEach { date ->
                    val entries = sampleEntries[date] ?: emptyList()
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .border(0.5.dp, Color.Black.copy(alpha = 0.15f))
                            .clickable { clickedDate = date }
                    ) {
                        WeekDayColumn(
                            date = date,
                            entries = entries,
                            isToday = date == currentDate,
                            isSelected = date == selectedDate,
                            hourHeight = HOUR_HEIGHT,
                            onEntryClick = { entry ->
                                onDateSelected(date)
                                selectedEntry = entry
                            }
                        )
                    }
                }
            }
        }

        var showCreateTask by remember { mutableStateOf(false) }
        var taskToEdit by remember { mutableStateOf<Task?>(null) }

        selectedEntry?.let { entry ->
            val task = entry.task
            if (task != null) {
                val topicName = store.topics().find { it.id == task.topicId }?.name ?: "Sin tópico"
                val tagNames = task.tags.mapNotNull { id ->
                    store.tags().associateBy { it.id }[id]?.name
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
                            val command = CommandBuilder(store).set("id", task.id.toString()).build(CommandType.DELETE_TASK)
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
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .fillMaxHeight(0.7f)
                ) {
                    CreateTask(
                        store = store,
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

        clickedDate?.let { date ->
            val entriesForDay = sampleEntries[date] ?: emptyList()
            DayDetailDialog(
                date = date,
                entries = entriesForDay,
                store = store,
                onTaskCreated = onTaskCreated,
                onDismiss = { clickedDate = null },
                onDeleted = onDeleted,
                onEdit = onEdit
            )
        }
    }
}
