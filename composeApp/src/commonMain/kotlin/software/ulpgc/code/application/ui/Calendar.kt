package software.ulpgc.code.application.ui

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
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
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

enum class CalendarViewMode { DIA, SEMANA, MES, AÑO }
@Composable
fun CalendarScreen() {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    val sampleEntries = remember(today) {
        mapOf(
            today to listOf(
                SampleEntry("Reunión de equipo", "10:00 · 11:00", Color(0xFF4F6EF7)),
                SampleEntry("Enviar informe Q2", "Vence hoy", Color(0xFFF59E0B))
            ),
            today.plus(2, DateTimeUnit.DAY) to listOf(
                SampleEntry("Revisión de diseño", "15:00 · 16:30", Color(0xFF4F6EF7)),
                SampleEntry("Actualizar dependencias", "Sin hora", Color(0xFFF59E0B))
            ),
            today.plus(5, DateTimeUnit.DAY) to listOf(
                SampleEntry("Sprint planning", "11:00 · 12:00", Color(0xFF4F6EF7))
            )
        )
    }

    var selectedDate by remember { mutableStateOf(today) }
    var viewMode by remember { mutableStateOf(CalendarViewMode.MES) }
    // MonthView está al final
    Box(modifier = Modifier.fillMaxSize()) {
        when (viewMode) {
            CalendarViewMode.MES -> MonthView(
                sampleEntries = sampleEntries,
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it },
                viewMode = viewMode,
                onViewModeChange = { viewMode = it }
            )
            CalendarViewMode.DIA -> DayView(/* próximamente */)
            CalendarViewMode.SEMANA -> WeekView(
                sampleEntries = sampleEntries,
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it },
                viewMode = viewMode,
                onViewModeChange = { viewMode = it }
            )
            CalendarViewMode.AÑO -> YearView(
                sampleEntries = sampleEntries,
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it },
                viewMode = viewMode,
                onViewModeChange = { viewMode = it }
            )
        }
    }
}
data class SampleEntry(val title: String, val time: String, val color: Color)

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
    modifier: Modifier = Modifier
) {
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
}
// Función encargada del popup de la celda de día
@Composable
fun DayDetailDialog(
    date: LocalDate,
    entries: List<SampleEntry>,
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
            DayEntriesPanel(date = date, entries = entries)
        },
        confirmButton = {
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
    onViewModeChange: (CalendarViewMode) -> Unit
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
                    onDismiss = { showDialog = false }
                )
            }
        }
    }
}

@Composable
fun DayView(){}

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

    // ── Fila principal ──────────────────────────────────────────────────────
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // 🔹 Leyenda
        var expandLegend by remember { mutableStateOf(false) }

        Box(modifier = Modifier.weight(1f)) {
            Button(onClick = { expandLegend = true }) {
                Text("Leyenda", maxLines = 1)
            }

            LaunchedEffect(scrollState.value) {
                if (expandLegend) expandLegend = false
            }

            if (expandLegend) {
                Popup(
                    alignment = Alignment.TopStart,
                    offset = IntOffset(0, 60),
                    onDismissRequest = { expandLegend = false }
                ) {
                    val legendItems = listOf(
                        "Importante" to Color.Red,
                        "Info" to Color.Blue,
                        "OK" to Color.Green
                    )
                    Column(
                        modifier = Modifier
                            .width(150.dp)
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                            .padding(4.dp)
                    ) {
                        legendItems.forEach { (text, color) ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(color)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(text)
                            }
                        }
                    }
                }
            }
        }

        // 🔹 Flecha izquierda — fuera del bloque central
        IconButton(onClick = onPreviousClick) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Semana anterior")
        }

        // 🔹 Título solo, sin iconos compitiendo por espacio
        Text(
            text = title,
            modifier = Modifier.weight(1.5f),
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // 🔹 Flecha derecha — fuera del bloque central
        IconButton(onClick = onNextClick) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Semana siguiente")
        }

        // 🔹 Selector de modo
        var expanded by remember { mutableStateOf(false) }

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

    // ── Cabecera de días ────────────────────────────────────────────────────
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp)
    ) {
        listOf("L", "M", "X", "J", "V", "S", "D").forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun YearView(
    sampleEntries: Map<LocalDate, List<SampleEntry>>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    viewMode: CalendarViewMode,
    onViewModeChange: (CalendarViewMode) -> Unit
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
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
        Text(
            text = monthNames[month.yearMonth.month.ordinal],
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
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
                fontSize = 9.sp,
                lineHeight = 10.sp
            )

            // Puntos de color si hay entradas (máx 3)
            if (entries.isNotEmpty() && day.position == DayPosition.MonthDate) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(top = 1.dp)
                ) {
                    entries.take(3).forEach { entry ->
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(
                                    color = if (isSelected) Color.White.copy(alpha = 0.8f)
                                    else entry.color,
                                    shape = CircleShape
                                )
                        )
                    }
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
fun WeekEventChip(entry: SampleEntry, startHour: Float, endHour: Float, hourHeight: Dp) {
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
    onDayClick: () -> Unit
) {
    val totalHours = END_HOUR - START_HOUR

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
    ) {
        // Líneas de hora y media hora
        for (h in 0 until totalHours) {
            // Línea de hora
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .offset(y = (h * hourHeight.value).dp)
                    .background(Color.Gray.copy(alpha = 0.2f))
            )
            // Línea de media hora (discontinua simulada con alpha)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .offset(y = (h * hourHeight.value + hourHeight.value / 2).dp)
                    .background(Color.Gray.copy(alpha = 0.1f))
            )
        }

        // Eventos posicionados absolutamente
        entries.forEach { entry ->
            // Parsea "HH:mm · HH:mm" o "HH:mm" del campo time
            val (startH, endH) = parseEntryTime(entry.time)
            WeekEventChip(
                entry = entry,
                startHour = startH,
                endHour = endH,
                hourHeight = hourHeight
            )
        }

        // Línea de "ahora" si es hoy
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

// ── Helper: parsea el campo `time` de SampleEntry ──────────────────────────
// Acepta "10:00 · 11:00", "Vence hoy", "Sin hora", etc.
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
    sampleEntries: Map<LocalDate, List<SampleEntry>>
) {
    val currentDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val scrollState = rememberScrollState()
    val totalHours  = END_HOUR - START_HOUR
    val totalHeightDp = HOUR_HEIGHT * totalHours

    // ── Navegación: solo un entero que representa el offset de semanas ──
    var weekOffset by remember { mutableStateOf(0) }

    // Lunes de la semana visible
    val weekStart = remember(weekOffset) {
        val daysSinceMonday = currentDate.dayOfWeek.ordinal // 0=Lun, 6=Dom
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
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Columna de horas
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

            // 7 columnas de días
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
                            .border(0.5.dp, Color.Gray.copy(alpha = 0.15f))
                    ) {
                        WeekDayColumn(
                            date       = date,
                            entries    = entries,
                            isToday    = date == currentDate,
                            isSelected = date == selectedDate,
                            hourHeight = HOUR_HEIGHT,
                            onDayClick = { onDateSelected(date) }
                        )
                    }
                }
            }
        }
    }
}