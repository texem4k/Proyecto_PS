package software.ulpgc.code.application.ui

import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.compose.rememberCalendarState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
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

    val currentMonth = today.yearMonth

    val calendarState = rememberCalendarState(
        startMonth = currentMonth,
        endMonth = currentMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = DayOfWeek.MONDAY,
        outDateStyle = OutDateStyle.EndOfGrid
    )

    Column {
        HorizontalCalendar(
            state = calendarState,
            dayContent = { day ->
                Text(
                    text = day.date.dayOfMonth.toString(),
                    color = if (day.position == DayPosition.MonthDate) {
                        Color.Black
                    } else {
                        Color.LightGray
                    }
                )
            }
        )

        val entriesForDay = sampleEntries[selectedDate] ?: emptyList()
        DayEntriesPanel(date = selectedDate, entries = entriesForDay)
    }
}

data class SampleEntry(val title: String, val time: String, val color: Color)

@Composable
fun DayCell(
    day: CalendarDay,
    entries: List<SampleEntry>,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    Box(
        modifier = Modifier
            .aspectRatio(1f)
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
                fontSize = 13.sp,
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
    Text(text = month.yearMonth.toString())
}


@Composable
fun DayEntriesPanel(
    date: LocalDate,
    entries: List<SampleEntry>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
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