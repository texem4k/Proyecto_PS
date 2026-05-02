package software.ulpgc.code.application.ui.pages

import Screen
import androidx.compose.foundation.ScrollState
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.compose.rememberCalendarState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.VerticalYearCalendar
import com.kizitonwose.calendar.compose.yearcalendar.YearCalendarState
import com.kizitonwose.calendar.compose.yearcalendar.rememberYearCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.CalendarYear
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.Year
import com.kizitonwose.calendar.core.minusMonths
import com.kizitonwose.calendar.core.minusYears
import com.kizitonwose.calendar.core.plusMonths
import com.kizitonwose.calendar.core.plusYears
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.yearMonth
import kotlin.time.Clock
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import software.ulpgc.code.application.ui.SideBar
import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.tasks.Task

enum class CalendarViewMode { DIA, SEMANA, MES, AÑO }
@Composable
fun CalendarScreen(onNavigate: (Screen) -> Unit, store: Storage) {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    val sampleEntries = remember(store.tasks()) {
        store.tasks().groupBy { task ->
            task.time.start.toLocalDateTime(TimeZone.UTC).date
        }.mapValues { (_, tasks) ->
            tasks.map { task ->
                val startTime = task.time.start.toLocalDateTime(TimeZone.UTC)
                val endTime = task.time.end.toLocalDateTime(TimeZone.UTC)
                SampleEntry(
                    title = task.name,
                    time = "${startTime.hour.toString().padStart(2,'0')}:${startTime.minute.toString().padStart(2,'0')} · ${endTime.hour.toString().padStart(2,'0')}:${endTime.minute.toString().padStart(2,'0')}",
                    color = Color(0xFF4F6EF7),
                    task = task
                )
            }
        }
    }

    var selectedDate by remember { mutableStateOf(today) }
    var viewMode by remember { mutableStateOf(CalendarViewMode.MES) }
    // MonthView está al final
    Row(modifier = Modifier.fillMaxSize()) {
        SideBar(
            selectedScreen = Screen.CALENDAR,
            onNavigate = onNavigate
        )

        Box(modifier = Modifier.fillMaxSize()) {
            when (viewMode) {
                CalendarViewMode.MES -> MonthView(
                    sampleEntries = sampleEntries,
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it },
                    viewMode = viewMode,
                    onViewModeChange = { viewMode = it },
                    store = store
                )
                CalendarViewMode.DIA -> DayView(
                    sampleEntries = sampleEntries,
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it },
                    viewMode = viewMode,
                    onViewModeChange = { viewMode = it },
                    store = store
                )
                CalendarViewMode.SEMANA -> WeekView(
                    sampleEntries = sampleEntries,
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it },
                    viewMode = viewMode,
                    onViewModeChange = { viewMode = it },
                    store = store
                )
                CalendarViewMode.AÑO -> YearView(
                    sampleEntries = sampleEntries,
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it },
                    viewMode = viewMode,
                    onViewModeChange = { viewMode = it },
                    store = store
                )
            }
        }
    }
}
data class SampleEntry(val title: String, val time: String, val color: Color, val task: Task?=null)

@Composable
fun DayCell(
    day: CalendarDay,
    entries: List<SampleEntry>,
    isSelected: Boolean,
    onClick: () -> Unit,
    cellHeight: Dp
) {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(cellHeight)
            .padding(2.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) Color(0xFF4F6EF7) else Color.Transparent)
            .clickable(enabled = day.position == DayPosition.MonthDate, onClick = onClick),
        contentAlignment = Alignment.TopStart
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth().height(24.dp)) {
                Box (modifier = Modifier.size(25.dp).fillMaxHeight().padding(bottom = 2.dp, start = 4.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = day.date.dayOfMonth.toString(),
                        color = when {
                            isSelected -> Color.White
                            day.date == today -> Color(0xFF4F6EF7)
                            day.position != DayPosition.MonthDate -> Color.Gray.copy(alpha = 0.3f)
                            else -> Color.Unspecified
                        },
                        fontWeight = if (isSelected || day.date == today) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 15.sp,
                        lineHeight = 15.sp
                    )
                }
                Box(Modifier.padding(start=4.dp).weight(1f).height(20.dp).background(Color.Black).fillMaxHeight())
            }
            // Zona encargada de la previsualización de tareas
            if (entries.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    entries.take(2).forEach { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(entry.color.copy(alpha = if (isSelected) 0.3f else 0.15f))
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(entry.color, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = entry.title,
                                fontSize = 9.sp,
                                maxLines = 1,
                                color = if (isSelected) Color.White else Color.Black.copy(alpha = 0.7f),
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthHeader(month: CalendarMonth,
                onPreviousClick: () -> Unit,
                onNextClick: () -> Unit,
                viewMode: CalendarViewMode,
                onViewModeChange: (CalendarViewMode) -> Unit,
                scrollState: ScrollState
) {
    val monthNames = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        //boton leyenda y popup
        var expandLegend by remember { mutableStateOf(false) }

        Box(modifier = Modifier.weight(1f)) {
            Button(
                onClick = { expandLegend = true }
            ) {
                Text(text = "Leyenda")
            }

            DropdownMenu(
                expanded = expandLegend,
                onDismissRequest = { expandLegend = false },
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                val legendItems = listOf(
                    "Importante" to Color.Red,
                    "Info" to Color.Blue,
                    "OK" to Color.Green
                )

                legendItems.forEach { (text, color) ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(color)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(text)
                            }
                        },
                        onClick = { } // opcional
                    )
                }
            }
        }

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${monthNames[month.yearMonth.month.ordinal]} ${month.yearMonth.year}",
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onPreviousClick) {
                Icon(Icons.Default.ChevronLeft, contentDescription = null)
            }
            IconButton(onClick = onNextClick) {
                Icon(Icons.Default.ChevronRight, contentDescription = null)
            }
        }

        var expanded by remember { mutableStateOf(false) }

        // Boton (día, mes. semana. año) y popup
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.TopEnd) {

            LaunchedEffect(scrollState.value) {
                if (expanded) expanded = false
            }

            // Box interior que envuelve SOLO el botón — el dropdown se ancla a este
            Box {
                Button(
                    onClick = { expanded = true }
                ) {
                    Text(text = viewMode.name)
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                ) {
                    CalendarViewMode.entries.forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode.name) },
                            onClick = {
                                onViewModeChange(mode)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
    Row(modifier = Modifier.fillMaxWidth()) {
        listOf("L", "M", "X", "J", "V", "S", "D").forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = Color.Black
            )
        }
    }
}


@Composable
fun DayEntriesPanel(
    date: LocalDate,
    entries: List<SampleEntry>,
    store: Storage,
    modifier: Modifier = Modifier
) {
    var selectedEntry by remember { mutableStateOf<SampleEntry?>(null) }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = "Entradas para $date")

        if (entries.isEmpty()) {
            Text(text = "No hay eventos para este día")
        } else {
            entries.forEach { entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedEntry = entry }
                        .background(
                            color = Color(0xFFF5F5F5),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(7.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = entry.title)
                        Text(text = entry.time, color = Color.Gray)
                    }
                }
            }
        }
    }

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
                    Button(onClick = { }) { Text("Eliminar tarea") }
                    Button(onClick = { }) { Text("Editar tarea") }
                },
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}
// Función encargada del popup de la celda de día
@Composable
fun DayDetailDialog(
    date: LocalDate,
    entries: List<SampleEntry>,
    store: Storage,        // ← añadir
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "${date.dayOfMonth}/${date.monthNumber}/${date.year}",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            DayEntriesPanel(date = date, entries = entries, store = store)
        },
        confirmButton = {
            Button(onClick = {}) { Text("Crear Tarea")}
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = Color(0xFF4F6EF7))
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun MonthView(
    sampleEntries: Map<LocalDate, List<SampleEntry>>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    viewMode: CalendarViewMode,
    onViewModeChange: (CalendarViewMode) -> Unit,
    store: Storage
) {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val currentMonth = today.yearMonth
    val coroutineScope = rememberCoroutineScope()

    val startMonth = remember { currentMonth.minusMonths(12) }
    val endMonth = remember { currentMonth.plusMonths(12) }

    val calendarState = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = DayOfWeek.MONDAY,
        outDateStyle = OutDateStyle.EndOfRow
    )

    var weeks by remember { mutableStateOf(calendarState.firstVisibleMonth.weekDays.size) }
    LaunchedEffect(calendarState) {
        snapshotFlow { calendarState.firstVisibleMonth }
            .collect { month -> weeks = month.weekDays.size }
    }

    var showDialog by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = Modifier.fillMaxHeight().fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd
    ) {
        val headerHeight = 100.dp
        val cellSize = (maxHeight - headerHeight) / weeks
        val calendarHeight = (cellSize * weeks) + headerHeight

        val scrollState = rememberScrollState()

        Column(modifier = Modifier.fillMaxWidth().padding(15.dp)) {
            Box(modifier = Modifier.fillMaxWidth().height(calendarHeight)) {
                HorizontalCalendar(
                    modifier = Modifier.fillMaxSize(),
                    state = calendarState,
                    monthHeader = { month ->
                        MonthHeader(
                            month = month,
                            onPreviousClick = {
                                coroutineScope.launch {
                                    calendarState.animateScrollToMonth(month.yearMonth.minusMonths(1))
                                }
                            },
                            onNextClick = {
                                coroutineScope.launch {
                                    calendarState.animateScrollToMonth(month.yearMonth.plusMonths(1))
                                }
                            },
                            viewMode = viewMode,
                            onViewModeChange = onViewModeChange,
                            scrollState = scrollState
                        )
                    },
                    monthBody = { _, content ->
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFF5F7FB))
                                .padding(horizontal = 2.dp)
                        ) { content() }
                    },
                    dayContent = { day ->
                        val entries = sampleEntries[day.date] ?: emptyList()
                        DayCell(
                            day = day,
                            entries = entries,
                            isSelected = day.date == selectedDate,
                            onClick = {
                                onDateSelected(day.date)
                                showDialog = true
                            },
                            cellHeight = cellSize,
                        )
                    }
                )
            }
            if (showDialog) {
                val entriesForDay = sampleEntries[selectedDate] ?: emptyList()
                DayDetailDialog(
                    date = selectedDate,
                    entries = entriesForDay,
                    store = store,
                    onDismiss = { showDialog = false }
                )
            }
        }
    }
}
@Composable
fun WeekHeader(
    startDate: LocalDate,
    endDate: LocalDate,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    viewMode: CalendarViewMode,
    onViewModeChange: (CalendarViewMode) -> Unit,
    scrollState: ScrollState
) {
    val monthNames = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    val title = if (startDate.month == endDate.month) {
        "${startDate.dayOfMonth} - ${endDate.dayOfMonth} ${monthNames[startDate.month.ordinal]} ${startDate.year}"
    } else {
        "${startDate.dayOfMonth} ${monthNames[startDate.month.ordinal]} - " +
                "${endDate.dayOfMonth} ${monthNames[endDate.month.ordinal]} ${endDate.year}"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ── Izquierda: Leyenda ───────────────────────────────────────────
        var expandLegend by remember { mutableStateOf(false) }

        Box(modifier = Modifier.weight(1f)) {
            Button(onClick = { expandLegend = true }) {
                Text(text = "Leyenda")
            }

            LaunchedEffect(scrollState.value) {
                if (expandLegend) expandLegend = false
            }

            DropdownMenu(
                expanded = expandLegend,
                onDismissRequest = { expandLegend = false },
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                val legendItems = listOf(
                    "Importante" to Color.Red,
                    "Info" to Color.Blue,
                    "OK" to Color.Green
                )
                legendItems.forEach { (text, color) ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(16.dp).background(color))
                                Spacer(Modifier.width(8.dp))
                                Text(text)
                            }
                        },
                        onClick = {}
                    )
                }
            }
        }

        // ── Centro: título + flechas ─────────────────────────────────────
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onPreviousClick) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Semana anterior")
            }
            IconButton(onClick = onNextClick) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Semana siguiente")
            }
        }

        // ── Derecha: selector de vista ───────────────────────────────────
        var expanded by remember { mutableStateOf(false) }

        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.TopEnd) {

            LaunchedEffect(scrollState.value) {
                if (expanded) expanded = false
            }

            Box {
                Button(onClick = { expanded = true }) {
                    Text(text = viewMode.name)
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    CalendarViewMode.entries.forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode.name) },
                            onClick = {
                                onViewModeChange(mode)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun YearView(
    sampleEntries: Map<LocalDate, List<SampleEntry>>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    viewMode: CalendarViewMode,
    onViewModeChange: (CalendarViewMode) -> Unit,
    store: Storage
) {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val currentYear = remember { Year.now() }
    var visibleYear by remember { mutableStateOf(currentYear) }
    val startYear = remember(visibleYear) { visibleYear }
    val endYear = remember(visibleYear) { visibleYear }

    val yearState = rememberYearCalendarState(
        startYear = startYear,
        endYear = endYear,
        firstVisibleYear = visibleYear,
        firstDayOfWeek = DayOfWeek.MONDAY,
        outDateStyle = OutDateStyle.EndOfGrid
    )

    var showDialog by remember { mutableStateOf(false) }
    var dialogDate by remember { mutableStateOf(selectedDate) }

    // Columnas adaptativas según ancho de pantalla
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val monthColumns = when {
            maxWidth >= 900.dp -> 4
            maxWidth >= 600.dp -> 3
            else -> 2
        }

        VerticalYearCalendar(
            modifier = Modifier.fillMaxSize(),
            state = yearState,
            monthColumns = monthColumns,
            yearHeader = { year ->
                YearHeader(
                    year = year,
                    yearState = yearState,
                    viewMode = viewMode,
                    onViewModeChange = onViewModeChange,
                    onPreviousYear = { visibleYear = visibleYear.minusYears(1) }, // aquí sí puede mutar
                    onNextYear = { visibleYear = visibleYear.plusYears(1) }
                )
            },
            monthHeader = { month ->
                YearMonthHeader(month = month)
            },
            monthBody = { _, content ->
                Box(
                    modifier = Modifier
                        .border(1.dp, Color.Black)
                        .background(Color(0xFFF5F7FB))
                        .padding(horizontal = 1.dp)
                ) { content() }
            },
            dayContent = { day ->
                val entries = sampleEntries[day.date] ?: emptyList()
                YearDayCell(
                    day = day,
                    entries = entries,
                    isSelected = day.date == selectedDate,
                    today = today,
                    onClick = {
                        onDateSelected(day.date)
                        dialogDate = day.date
                        showDialog = true
                    }
                )
            }
        )

        if (showDialog) {
            val entriesForDay = sampleEntries[dialogDate] ?: emptyList()
            DayDetailDialog(
                date = dialogDate,
                entries = entriesForDay,
                store = store,
                onDismiss = { showDialog = false }
            )
        }
    }
}

// ── Cabecera de año con botones de navegación y selector de vista ──────────

@Composable
fun YearHeader(
    year: CalendarYear,
    yearState: YearCalendarState,
    viewMode: CalendarViewMode,
    onViewModeChange: (CalendarViewMode) -> Unit,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }

    // Cierra el popup si el usuario scrollea
    LaunchedEffect(yearState.isScrollInProgress) {
        if (yearState.isScrollInProgress) expanded = false
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Año + navegación
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousYear) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Año anterior")
            }

            Text(
                text = year.year.value.toString(),
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            IconButton(onClick = onNextYear) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Año siguiente")
            }
        }

        // Selector de vista (mismo estilo que MonthHeader)
        Box(contentAlignment = Alignment.CenterEnd) {
            Button(onClick = { expanded = true }) {
                Text(text = viewMode.name)
            }
            if (expanded) {
                Popup(
                    alignment = Alignment.TopEnd,
                    offset = IntOffset(10, 60),
                    onDismissRequest = { expanded = false }
                ) {
                    Column(
                        modifier = Modifier
                            .width(150.dp)
                            .wrapContentHeight()
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                            .padding(4.dp)
                    ) {
                        CalendarViewMode.entries.forEach { mode ->
                            Text(
                                text = mode.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onViewModeChange(mode)
                                        expanded = false
                                    }
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Cabecera de mes dentro de la vista anual ───────────────────────────────

@Composable
fun YearMonthHeader(month: CalendarMonth) {
    val monthNames = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Text(
            text = monthNames[month.yearMonth.month.ordinal],
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,           // ← centrar texto
            modifier = Modifier
                .fillMaxWidth()                     // ← necesario para que el center funcione
                .padding(vertical = 6.dp)
        )
        // Días de la semana
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("L", "M", "X", "J", "V", "S", "D").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    color = Color.Black
                )
            }
        }
    }
}

// ── Celda de día reducida para la vista anual ──────────────────────────────

@Composable
fun YearDayCell(
    day: CalendarDay,
    entries: List<SampleEntry>,
    isSelected: Boolean,
    today: LocalDate,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(1.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) Color(0xFF4F6EF7) else Color.Transparent)
            .clickable(enabled = day.position == DayPosition.MonthDate, onClick = onClick),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            // Número del día
            Text(
                text = day.date.dayOfMonth.toString(),
                color = when {
                    isSelected -> Color.White
                    day.date == today -> Color(0xFF4F6EF7)
                    day.position != DayPosition.MonthDate -> Color.Gray.copy(alpha = 0.3f)
                    else -> Color.Unspecified
                },
                fontWeight = if (isSelected || day.date == today) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 10.sp
            )

            if (entries.isNotEmpty() && day.position == DayPosition.MonthDate) {
                Box(
                    modifier = Modifier
                        .size(25.dp)
                        .background(
                            color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color(0xFF4F6EF7),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = entries.size.toString(),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color(0xFF4F6EF7) else Color.White
                    )
                }
            }
        }
    }
}



// ── Constantes de layout ────────────────────────────────────────────────────
private val HOUR_HEIGHT = 64.dp   // altura de cada hora en dp
private val TIME_COL_W = 52.dp    // ancho de la columna de horas
private val START_HOUR = 0        // primera hora visible
private val END_HOUR   = 24       // última hora visible

// ── Celda de evento en la columna de día ────────────────────────────────────
@Composable
fun WeekEventChip(entry: SampleEntry, startHour: Float, endHour: Float, hourHeight: Dp, onClick: () -> Unit = {}) {
    val topDp    = ((startHour - START_HOUR) * hourHeight.value).dp
    val heightDp = ((endHour - startHour) * hourHeight.value).dp.coerceAtLeast(20.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp)
            .offset(y = topDp)
            .height(heightDp)
            .clip(RoundedCornerShape(5.dp))
            .background(entry.color.copy(alpha = 0.13f))
            .border(
                width = 2.5.dp,
                color = entry.color,
                shape = RoundedCornerShape(topStart = 5.dp, bottomStart = 5.dp)
            )
            .clickable {onClick()}
            .padding(horizontal = 5.dp, vertical = 3.dp)
    ) {
        Column {
            Text(
                text = entry.title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = entry.color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = entry.time,
                fontSize = 9.sp,
                color = entry.color.copy(alpha = 0.8f),
                maxLines = 1
            )
        }
    }
}

// ── Columna de un día (7 columnas en la cuadrícula) ─────────────────────────
@Composable
fun WeekDayColumn(
    date: LocalDate,
    entries: List<SampleEntry>,
    isToday: Boolean,
    isSelected: Boolean,
    hourHeight: Dp,
    onEntryClick: (SampleEntry) -> Unit
) {
    val totalHours = END_HOUR - START_HOUR

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(Color.White)
    ) {
        for (h in 0 until totalHours) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.65.dp)
                    .offset(y = (h * hourHeight.value).dp)
                    .background(Color.Black.copy(alpha = 0.2f))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .offset(y = (h * hourHeight.value + hourHeight.value / 2).dp)
                    .background(Color.Black.copy(alpha = 0.1f))
            )
        }

        entries.forEach { entry ->
            val (startH, endH) = parseEntryTime(entry.time)
            WeekEventChip(
                entry = entry,
                startHour = startH,
                endHour = endH,
                hourHeight = hourHeight,
                onClick = { onEntryClick(entry) }
            )
        }

        if (isToday) {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val nowFraction = now.hour + now.minute / 60f
            if (nowFraction in START_HOUR.toFloat()..END_HOUR.toFloat()) {
                val topDp = ((nowFraction - START_HOUR) * hourHeight.value).dp
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = topDp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .offset(x = (-4).dp, y = (-4).dp)
                            .background(Color(0xFF4F6EF7), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(Color(0xFF4F6EF7))
                    )
                }
            }
        }
    }
}

private fun parseEntryTime(time: String): Pair<Float, Float> {
    return try {
        val parts = time.split("·").map { it.trim() }
        val start = parts[0].split(":").let { it[0].toFloat() + it[1].toFloat() / 60f }
        val end   = if (parts.size > 1) parts[1].split(":").let { it[0].toFloat() + it[1].toFloat() / 60f }
        else start + 0.5f
        start to end
    } catch (_: Exception) {
        9f to 9.5f   // fallback: 09:00-09:30
    }
}

// ── WeekView principal ──────────────────────────────────────────────────────
@Composable
fun WeekView(
    viewMode: CalendarViewMode,
    onViewModeChange: (CalendarViewMode) -> Unit,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    sampleEntries: Map<LocalDate, List<SampleEntry>>,
    store: Storage
) {
    val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val scrollState = rememberScrollState()
    val totalHours  = END_HOUR - START_HOUR
    val totalHeightDp = HOUR_HEIGHT * totalHours
    var weekOffset by remember { mutableStateOf(0) }
    var selectedEntry by remember { mutableStateOf<SampleEntry?>(null) }

    val weekStart = remember(weekOffset) {
        val daysSinceMonday = currentDate.dayOfWeek.ordinal
        currentDate
            .minus(DatePeriod(days = daysSinceMonday))
            .plus(DatePeriod(days = weekOffset * 7))
    }
    val weekDates = remember(weekStart) {
        (0..6).map { weekStart.plus(DatePeriod(days = it)) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        WeekHeader(
            startDate        = weekDates.first(),
            endDate          = weekDates.last(),
            onPreviousClick  = { weekOffset-- },
            onNextClick      = { weekOffset++ },
            viewMode         = viewMode,
            onViewModeChange = onViewModeChange,
            scrollState      = scrollState
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(start = TIME_COL_W)
        ) {
            val dayLetters = listOf("L", "M", "X", "J", "V", "S", "D")
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
                    if (date == currentDate) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color(0xFF4F6EF7), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = date.dayOfMonth.toString(),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    } else {
                        Text(
                            text = date.dayOfMonth.toString(),
                            fontSize = 13.sp,
                            fontWeight = if (date == selectedDate) FontWeight.Bold else FontWeight.Normal,
                            color = if (date == selectedDate) Color(0xFF4F6EF7) else Color.Black
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
                    ) {
                        WeekDayColumn(
                            date       = date,
                            entries    = entries,
                            isToday    = date == currentDate,
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
                        Button(onClick = { }) { Text("Eliminar tarea") }
                        Button(onClick = { }) { Text("Editar tarea") }
                    },
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}
@Composable
fun DayView(
    sampleEntries: Map<LocalDate, List<SampleEntry>>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    viewMode: CalendarViewMode,
    onViewModeChange: (CalendarViewMode) -> Unit,
    store: Storage
) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val scrollState = rememberScrollState()
    val totalHours = END_HOUR - START_HOUR
    val totalHeightDp = HOUR_HEIGHT * totalHours
    var selectedEntry by remember { mutableStateOf<SampleEntry?>(null) }

    var dayOffset by remember { mutableStateOf(0) }
    val currentDay = remember(dayOffset) {
        today.plus(DatePeriod(days = dayOffset))
    }

    LaunchedEffect(Unit) {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val nowFraction = (now.hour - START_HOUR).coerceAtLeast(0)
        scrollState.animateScrollTo((nowFraction * HOUR_HEIGHT.value - 100).toInt().coerceAtLeast(0))
    }

    Column(modifier = Modifier.fillMaxSize()) {
        DayHeader(
            date = currentDay,
            onPreviousClick = { dayOffset-- },
            onNextClick = { dayOffset++ },
            viewMode = viewMode,
            onViewModeChange = onViewModeChange,
            scrollState = scrollState
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
                        color = Color.Gray,
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
            ) {
                WeekDayColumn(
                    date = currentDay,
                    entries = timedEntries,
                    isToday = currentDay == today,
                    isSelected = true,
                    hourHeight = HOUR_HEIGHT,
                    onEntryClick = { entry -> selectedEntry = entry }
                )
            }
        }

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
                        Button(onClick = { }) { Text("Eliminar tarea") }
                        Button(onClick = { }) { Text("Editar tarea") }
                    },
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }
    }
}
// ── Franja de eventos sin hora ───────────────────────────────────────────
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

// ── Cabecera de la vista de día ──────────────────────────────────────────
@Composable
fun DayHeader(
    date: LocalDate,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    viewMode: CalendarViewMode,
    onViewModeChange: (CalendarViewMode) -> Unit,
    scrollState: ScrollState
) {
    val monthNames = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )
    val dayNames = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
    val title = "${dayNames[date.dayOfWeek.ordinal]} ${date.dayOfMonth} de ${monthNames[date.month.ordinal]} ${date.year}"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ── Izquierda: Leyenda ───────────────────────────────────────────
        var expandLegend by remember { mutableStateOf(false) }

        Box(modifier = Modifier.weight(1f)) {
            Button(onClick = { expandLegend = true }) {
                Text(text = "Leyenda")
            }

            LaunchedEffect(scrollState.value) {
                if (expandLegend) expandLegend = false
            }

            DropdownMenu(
                expanded = expandLegend,
                onDismissRequest = { expandLegend = false },
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                val legendItems = listOf(
                    "Importante" to Color.Red,
                    "Info" to Color.Blue,
                    "OK" to Color.Green
                )
                legendItems.forEach { (text, color) ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(16.dp).background(color))
                                Spacer(Modifier.width(8.dp))
                                Text(text)
                            }
                        },
                        onClick = {}
                    )
                }
            }
        }

        // ── Centro: título + flechas ─────────────────────────────────────
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onPreviousClick) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Día anterior")
            }
            IconButton(onClick = onNextClick) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Día siguiente")
            }
        }

        // ── Derecha: selector de vista ───────────────────────────────────
        var expanded by remember { mutableStateOf(false) }

        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.TopEnd) {

            LaunchedEffect(scrollState.value) {
                if (expanded) expanded = false
            }

            Box {
                Button(onClick = { expanded = true }) {
                    Text(text = viewMode.name)
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    CalendarViewMode.entries.forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode.name) },
                            onClick = {
                                onViewModeChange(mode)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}