package software.ulpgc.code.application.ui

import androidx.compose.runtime.Composable
import androidx.compose.material3.Text
import com.kizitonwose.calendar.compose.VerticalCalendar
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.core.yearMonth
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@Composable
fun SimpleCalendar() {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val currentMonth = yearMonth(today) // helper de la librería

    VerticalCalendar(
        startMonth = currentMonth,
        endMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeekFromLocale(),
        dayContent = { day: CalendarDay ->
            Text(text = day.date.dayOfMonth.toString())
        }
    )
}