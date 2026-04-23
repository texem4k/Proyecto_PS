package software.ulpgc.code.application.ui

import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.compose.rememberCalendarState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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

@Composable
fun CalendarScreen() {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val currentMonth = today.yearMonth  // ← faltaba esta línea

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

    val startMonth = remember { currentMonth.minusMonths(12) }
    val endMonth = remember { currentMonth.plusMonths(12) }

    val calendarState = rememberCalendarState(
        startMonth = startMonth,   // ← antes ponías currentMonth aquí
        endMonth = endMonth,       // ← y aquí también, sin rango
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = DayOfWeek.MONDAY,
        outDateStyle = OutDateStyle.EndOfRow
    )
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        BoxWithConstraints(modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.80f)
            .background(color = Color.Transparent)
            .align(Alignment.CenterEnd),
            contentAlignment = Alignment.CenterEnd
        ) {
            val cellSize = maxWidth.div(9)
            val headerHeight = 60.dp
            val calendarHeight = (cellSize * 6) + headerHeight  // ← 6 filas + header

            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(end = 20.dp, top = 20.dp)
            ) {
                Box(modifier = Modifier.height(calendarHeight).fillMaxWidth(),
                    ) {  // ← y se usa aquí
                    HorizontalCalendar(
                        modifier = Modifier.fillMaxSize(),
                        state = calendarState,
                        monthHeader = { month -> MonthHeader(month) },
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
                                onClick = { selectedDate = day.date },
                                cellHeight = cellSize,
                            )
                        }
                    )
                }

                //val entriesForDay = sampleEntries[selectedDate] ?: emptyList()
                //DayEntriesPanel(
                //    modifier = Modifier
                //        .fillMaxWidth()
                //        .weight(1f),
                //    date = selectedDate, entries = entriesForDay)
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
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.date.dayOfMonth.toString(),
                color = when {
                    isSelected -> Color.White
                    day.date == today -> Color(0xFF4F6EF7)
                    day.position != DayPosition.MonthDate -> Color.Gray.copy(alpha = 0.3f)
                    else -> Color.Unspecified
                },
                fontWeight = if (isSelected || day.date == today) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 10.sp
            )
            if (entries.isNotEmpty() && !isSelected) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    entries.take(3).forEach { entry ->
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(entry.color, CircleShape)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthHeader(month: CalendarMonth) {
    val monthNames = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            // month.yearMonth.month es el enum DayOfWeek, .ordinal da 0-11
            text = "${monthNames[month.yearMonth.month.ordinal]} ${month.yearMonth.year}",
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp
        )
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