package software.ulpgc.code.application.ui

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.minusMonths
import com.kizitonwose.calendar.core.plusMonths
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.yearMonth
import kotlin.time.Clock
import kotlinx.coroutines.launch

@Composable
fun CalendarScreen() {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val currentMonth = today.yearMonth
    val coroutineScope = rememberCoroutineScope()

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
    var showDialog by remember { mutableStateOf(false) }

    val startMonth = remember { currentMonth.minusMonths(12) }
    val endMonth = remember { currentMonth.plusMonths(12) }

    val calendarState = rememberCalendarState(
        startMonth = startMonth,   // ← antes ponías currentMonth aquí
        endMonth = endMonth,       // ← y aquí también, sin rango
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = DayOfWeek.MONDAY,
        outDateStyle = OutDateStyle.EndOfRow
    )
    var weeks by remember { mutableStateOf(calendarState.firstVisibleMonth.weekDays.size) }
    LaunchedEffect(calendarState) {
        snapshotFlow { calendarState.firstVisibleMonth }
            .collect { month ->
                weeks = month.weekDays.size
            }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        BoxWithConstraints(modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .background(color = Color.Transparent)
            .align(Alignment.CenterEnd),
            contentAlignment = Alignment.CenterEnd
        ) {
            val headerHeight = 100.dp
            val cellSize = (maxHeight - headerHeight) / weeks
            val calendarHeight = (cellSize * weeks) + headerHeight

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(calendarHeight),
                ) {
                    HorizontalCalendar(
                        modifier = Modifier.fillMaxSize(),
                        state = calendarState,
                        monthHeader = { month -> MonthHeader(month = month,
                            onPreviousClick = {
                                coroutineScope.launch {
                                    calendarState.animateScrollToMonth(
                                        month.yearMonth.minusMonths(1)
                                    )
                                }
                            },
                            onNextClick = {
                                coroutineScope.launch {
                                    calendarState.animateScrollToMonth(
                                        month.yearMonth.plusMonths(1)
                                    )
                                }
                            }) },
                        monthBody = { _, content ->
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFF5F7FB))
                                    .padding(horizontal = 2.dp)
                            ) {
                                content()
                            }
                        },
                        dayContent = { day ->
                            val entries = sampleEntries[day.date] ?: emptyList()
                            DayCell(
                                day = day,
                                entries = entries,
                                isSelected = day.date == selectedDate,
                                onClick = { selectedDate = day.date
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
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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
                onNextClick: () -> Unit
) {
    val monthNames = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            // month.yearMonth.month es el enum DayOfWeek, .ordinal da 0-11
            text = "${monthNames[month.yearMonth.month.ordinal]} ${month.yearMonth.year}",
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp
        )

        Row (modifier = Modifier.padding(start = 10.dp)) {
            IconButton(onClick = onPreviousClick,  modifier = Modifier.wrapContentSize(Alignment.Center)) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Next"
                )
            }
            IconButton(onClick = onNextClick,  modifier = Modifier.wrapContentSize(Alignment.Center)) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Next"
                )
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