package software.ulpgc.code.application.ui.pages.Calendar.Views

import Screen
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import com.kizitonwose.calendar.compose.VerticalYearCalendar
import com.kizitonwose.calendar.compose.yearcalendar.rememberYearCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.core.Year
import com.kizitonwose.calendar.core.minusYears
import com.kizitonwose.calendar.core.plusYears
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import software.ulpgc.code.application.ui.pages.Calendar.CalendarConstants
import software.ulpgc.code.application.ui.pages.Calendar.CalendarViewMode
import software.ulpgc.code.application.ui.pages.Calendar.SampleEntry
import software.ulpgc.code.application.ui.pages.Calendar.YearHeader
import software.ulpgc.code.application.ui.pages.DayDetailDialog
import software.ulpgc.code.application.ui.pages.urgencyColorFromEntries
import software.ulpgc.code.architecture.io.Storage
import kotlin.time.Clock

@Composable
fun YearView(
    sampleEntries: Map<LocalDate, List<SampleEntry>>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    viewMode: CalendarViewMode,
    onNavigate: (Screen) -> Unit,
    onViewModeChange: (CalendarViewMode) -> Unit,
    store: Storage,
    onTaskCreated: () -> Unit,
    onDeleted: () -> Unit,
    onEdit: () -> Unit,
    onFilterClick: () -> Unit
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
                    onPreviousYear = { visibleYear = visibleYear.minusYears(1) },
                    onNextYear = { visibleYear = visibleYear.plusYears(1) },
                    onFilterClick = onFilterClick
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
                onTaskCreated = onTaskCreated,
                onDismiss = { showDialog = false },
                onDeleted = onDeleted,
                onEdit = onEdit
            )
        }
    }
}

@Composable
fun YearMonthHeader(month: CalendarMonth) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Text(
            text = CalendarConstants.MONTH_NAMES_ES[month.yearMonth.month.ordinal],
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        )
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


@Composable
fun YearDayCell(
    day: CalendarDay,
    entries: List<SampleEntry>,
    isSelected: Boolean,
    today: LocalDate,
    onClick: () -> Unit
) {
    val dayUrgencyColor by remember {
        derivedStateOf {
            urgencyColorFromEntries(entries)
        }
    }
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
                            color = dayUrgencyColor,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = entries.size.toString(),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
