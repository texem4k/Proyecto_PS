package software.ulpgc.code.application.ui.pages.Calendar.Views

import Screen
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarMonth
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
import software.ulpgc.code.application.ui.pages.Calendar.SampleEntry
import software.ulpgc.code.application.ui.pages.DayDetailDialog
import software.ulpgc.code.application.ui.pages.urgencyColorFromEntries
import software.ulpgc.code.architecture.io.Storage
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.time.Clock

@Composable
fun HomeCalendar(
    sampleEntries: Map<LocalDate, List<SampleEntry>>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onNavigate: (Screen) -> Unit,
    store: Storage,
    onTaskCreated: () -> Unit,
    onDeleted: () -> Unit,
    onEdit: () -> Unit
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

    val dayUrgencyColor by remember {
        derivedStateOf {
            sampleEntries.mapValues { (_, entries) ->
                urgencyColorFromEntries(entries)
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp)
    ) {
        val headerHeight = 80.dp
        val cellHeight = (maxHeight - headerHeight) / weeks
        Column {
            HorizontalCalendar(
                modifier = Modifier.fillMaxSize(),
                state = calendarState,
                monthHeader = { month ->
                    miniCalendarHeader(
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
                        }
                    )
                },
                dayContent = { day ->
                    val isSelected = day.date == selectedDate
                    val entries = sampleEntries[day.date] ?: emptyList()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(cellHeight)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when {
                                    day.date == selectedDate -> MaterialTheme.colorScheme.primary
                                    day.date == today -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else -> Color.Transparent
                                }
                            )
                            .clickable(enabled = day.position == DayPosition.MonthDate) {
                                onDateSelected(day.date)
                                showDialog = true
                            },
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = day.date.dayOfMonth.toString(),
                                fontSize = 10.sp,
                                color = when {
                                    day.date == selectedDate -> Color.White
                                    day.date == today -> Color(0xFF4F6EF7)
                                    day.position != DayPosition.MonthDate -> Color.Gray.copy(alpha = 0.3f)
                                    else -> MaterialTheme.colorScheme.onSurface
                                },
                                fontWeight = if (day.date == today || day.date == selectedDate) FontWeight.Bold else FontWeight.Normal
                            )
                            if (entries.isNotEmpty() && day.position == DayPosition.MonthDate) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(
                                            color = dayUrgencyColor[day.date]!!,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = entries.size.toString(),
                                        modifier = Modifier.offset(y = -2.dp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary                                    )
                                }
                            }
                        }
                    }
                }
            )
        }

        if (showDialog) {
            val entriesForDay = sampleEntries[selectedDate] ?: emptyList()
            DayDetailDialog(
                date = selectedDate,
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

// ── miniCalendarHeader ────────────────────────────────────────────────────────

@Composable
fun miniCalendarHeader(
    month: CalendarMonth,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${CalendarConstants.MONTH_NAMES_ES[month.yearMonth.month.ordinal]} ${month.yearMonth.year}" ,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onPreviousClick) {
                Icon(Icons.Default.ChevronLeft, contentDescription = null,     tint = MaterialTheme.colorScheme.onSurface)

            }
            IconButton(onClick = onNextClick) {
                Icon(Icons.Default.ChevronRight, contentDescription = null,     tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
    Row(modifier = Modifier.fillMaxWidth()) {
        CalendarConstants.DAY_LETTERS.forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface            )
        }
    }
}
