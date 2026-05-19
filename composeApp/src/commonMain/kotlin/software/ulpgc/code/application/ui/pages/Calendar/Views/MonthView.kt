package software.ulpgc.code.application.ui.pages.Calendar.Views

import Screen
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.OutDateStyle
import com.kizitonwose.calendar.core.minusMonths
import com.kizitonwose.calendar.core.plusMonths
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.yearMonth
import software.ulpgc.code.application.ui.pages.Calendar.CalendarConstants
import software.ulpgc.code.application.ui.pages.Calendar.CalendarHeader
import software.ulpgc.code.application.ui.pages.Calendar.CalendarViewMode
import software.ulpgc.code.application.ui.pages.Calendar.SampleEntry
import software.ulpgc.code.application.ui.pages.DayDetailDialog
import software.ulpgc.code.application.ui.pages.urgencyColorFromEntries
import kotlin.time.Clock

@Composable
fun MonthView(
    sampleEntries: Map<LocalDate, List<SampleEntry>>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    viewMode: CalendarViewMode,
    onNavigate: (Screen) -> Unit,
    onViewModeChange: (CalendarViewMode) -> Unit,
    onTaskCreated: () -> Unit,
    onDeleted: () -> Unit,
    onEdit: () -> Unit,
    onFilterClick: () -> Unit
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
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(),
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
                        CalendarHeader(
                            title = "${CalendarConstants.MONTH_NAMES_ES[month.yearMonth.month.ordinal]} ${month.yearMonth.year}",
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
                            scrollState = scrollState,
                            onFilterClick = onFilterClick,
                            modifier = Modifier
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
                    onTaskCreated = onTaskCreated,
                    onDismiss = { showDialog = false },
                    onDeleted = onDeleted,
                    onEdit = onEdit
                )
            }
        }
    }
}

@Composable
fun DayCell(
    day: CalendarDay,
    entries: List<SampleEntry>,
    isSelected: Boolean,
    onClick: () -> Unit,
    cellHeight: Dp
) {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val tasks = entries.mapNotNull { it.task }
    val priorities = tasks.map { it.priority }

    val urgencyColor: Color = urgencyColorFromEntries(entries)

    BoxWithConstraints (
        modifier = Modifier
            .fillMaxWidth()
            .height(cellHeight)
            .padding(2.dp)
            .border(1.dp, Color.Black)
            .background(if (isSelected) Color(0xFF4F6EF7) else Color.Transparent)
            .clickable(enabled = day.position == DayPosition.MonthDate, onClick = onClick),
        contentAlignment = Alignment.TopStart
    ) {
        val maxTask = if (maxHeight < 110.dp) 2 else 3
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth().height(24.dp)) {
                Box(
                    modifier = Modifier
                        .size(25.dp)
                        .fillMaxHeight()
                        .padding(bottom = 2.dp, start = 4.dp),
                    contentAlignment = Alignment.Center
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
                        fontSize = 15.sp,
                        lineHeight = 15.sp
                    )
                }
                Box(
                    Modifier
                        .padding(start = 4.dp)
                        .weight(1f)
                        .height(20.dp)
                        .background(urgencyColor)
                        .fillMaxHeight()
                )
            }
            if (entries.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    entries.take(maxTask).forEach { entry ->
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
                    if (entries.size > 3) {
                        Text(
                            text = "...",
                            fontSize = 30.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}