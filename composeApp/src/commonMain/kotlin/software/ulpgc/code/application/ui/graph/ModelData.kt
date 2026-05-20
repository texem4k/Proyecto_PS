package software.ulpgc.code.application.ui.graph

import androidx.compose.runtime.remember
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import software.ulpgc.code.architecture.model.tasks.CompletionStat
import kotlin.collections.plus
import kotlin.time.Clock

data class DayStats(
    val date: LocalDate,
    val proposed: Int,
    val completed: Int
)


data class TimeWindow(val start: LocalDate, val end: LocalDate)

fun LocalDate.toWindowStart(windows: List<TimeWindow>): LocalDate? =
    windows.firstOrNull { this >= it.start && this < it.end }?.start

fun aggregateByDay(taskStat: Sequence<CompletionStat>): List<DayStats> {
    val tz      = TimeZone.currentSystemDefault()
    val today   = Clock.System.now().toLocalDateTime(tz).date
    val monday  = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)

    val statList = taskStat.toList()

    val tasksByProposedDate = statList.groupBy { it.proposedDate.toLocalDateTime(tz).date }
    val completedByEndDate  = statList
        .filter { it.completed }
        .groupBy { it.endDate.toLocalDateTime(tz).date }

    return buildList {
        var current = monday
        while (current <= today) {
            add(
                DayStats(
                    date      = current,
                    proposed  = tasksByProposedDate[current]?.size ?: 0,
                    completed = completedByEndDate[current]?.size ?: 0
                )
            )
            current = current.plus(1, DateTimeUnit.DAY)
        }
    }
}

fun aggregateByWeek(taskStat: Sequence<CompletionStat>): List<DayStats> {
    val tz           = TimeZone.currentSystemDefault()
    val today        = Clock.System.now().toLocalDateTime(tz).date
    val weekStarts = (3 downTo 0).map { weeksBack ->
        today.minus(weeksBack * 7, DateTimeUnit.DAY)
    }

    val windows = weekStarts.zipWithNext { a, b -> TimeWindow(a, b) } +
            TimeWindow(weekStarts.last(), weekStarts.last().plus(1, DateTimeUnit.WEEK))

    val tasksByWeek     = taskStat.groupBy { it.proposedDate.toLocalDateTime(tz).date.toWindowStart(windows) }
    val completedByWeek = taskStat
        .filter { it.completed }
        .groupBy { it.endDate.toLocalDateTime(tz).date.toWindowStart(windows) }

    return weekStarts.map { start ->
        DayStats(
            date      = start,
            proposed  = tasksByWeek[start]?.size ?: 0,
            completed = completedByWeek[start]?.size ?: 0
        )
    }
}

fun aggregateByMonth(taskStat :Sequence<CompletionStat>): List<DayStats> {
    val tz    = TimeZone.currentSystemDefault()
    val today = Clock.System.now().toLocalDateTime(tz).date

    val months = (4 downTo 0).map { today.minus(it, DateTimeUnit.MONTH) }
    val statList = taskStat.toList()
    val windows = months.zipWithNext { a, b -> TimeWindow(a, b) } +
            TimeWindow(months.last(), months.last().plus(1, DateTimeUnit.MONTH))

    val tasksByMonth = statList.groupBy { task ->
        task.proposedDate
            .toLocalDateTime(tz)
            .date.toWindowStart(windows)

    }
    val completedByMonth = statList.filter { it.completed }.groupBy { task ->
        task.endDate
            .toLocalDateTime(tz)
            .date.toWindowStart(windows)

    }

    return months.map { start ->
        DayStats(
            date      = start,
            proposed  = tasksByMonth[start]?.size ?: 0,
            completed = completedByMonth[start]?.size ?: 0
        )
    }
}

fun aggregateByQuarter(taskStat :Sequence<CompletionStat>): List<DayStats> {
    val tz    = TimeZone.currentSystemDefault()
    val today = Clock.System.now().toLocalDateTime(tz).date

    val quarters = (4 downTo 0).map { today.minus(it*3, DateTimeUnit.MONTH) }

    val windows = quarters.zipWithNext { a, b -> TimeWindow(a, b) } +
            TimeWindow(quarters.last(), quarters.last().plus(3, DateTimeUnit.MONTH))

    val tasksByQuarter = taskStat.groupBy { task ->
        task.proposedDate
            .toLocalDateTime(tz).date.toWindowStart(windows)
    }
    val completedByQuarter = taskStat.filter { it.completed }.groupBy {
        it.endDate.toLocalDateTime(tz).date.toWindowStart(windows)
    }

    return quarters.map { start ->
        DayStats(
            date      = start,
            proposed  = tasksByQuarter[start]?.size ?: 0,
            completed = completedByQuarter[start]?.size ?: 0
        )
    }
}

fun aggregateByYear(taskStat :Sequence<CompletionStat>): List<DayStats> {
    val tz    = TimeZone.currentSystemDefault()
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val startOfYear = LocalDate(today.year, 1, 1)

    val years = (4 downTo 0).map { startOfYear.minus(it, DateTimeUnit.YEAR) }

    val windows = years.zipWithNext { a, b -> TimeWindow(a, b) } +
            TimeWindow(years.last(), years.last().plus(1, DateTimeUnit.YEAR))

    val tasksByYear = taskStat.groupBy { task ->
            task.proposedDate.toLocalDateTime(tz).date.toWindowStart(windows)
    }
    val completedByYear = taskStat.filter { it.completed }.groupBy { task ->
        task.proposedDate.toLocalDateTime(tz).date.toWindowStart(windows)
    }

    return years.map { start ->
        DayStats(
            date      = start,
            proposed  = tasksByYear[start]?.size ?: 0,
            completed = completedByYear[start]?.size ?: 0
        )
    }
}