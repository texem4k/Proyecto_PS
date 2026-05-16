package software.ulpgc.code.application.ui.graph

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import software.ulpgc.code.architecture.io.Store
import software.ulpgc.code.architecture.model.tasks.CompletionStat
import kotlin.time.Clock

data class DayStats(
    val date: LocalDate,
    val proposed: Int,
    val completed: Int
)


fun aggregateByDay(taskStat :Sequence<CompletionStat>): List<DayStats> {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val monday      = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)
    val windowStart = monday
    val tasksByDate = taskStat
        .groupBy { task -> task.proposedDate.toLocalDateTime(TimeZone.currentSystemDefault()).date }

    return buildList {
        var current = windowStart
        while (current <= today) {
            val dayTasks = tasksByDate[current] ?: emptyList()
            add(
                DayStats(
                    date      = current,
                    proposed  = dayTasks.size,
                    completed = dayTasks.count { it.completed }
                )
            )
            current = current.plus(1, DateTimeUnit.DAY)
        }
    }
}

fun aggregateByWeek(taskStat :Sequence<CompletionStat>): List<DayStats> {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    val currentMonday = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)

    val weekStarts = (3 downTo 0).map { weeksBack ->
        currentMonday.minus(weeksBack * 7, DateTimeUnit.DAY)
    }

    val tasksByWeek = taskStat.groupBy { task ->
            val date = task.proposedDate.toLocalDateTime(TimeZone.currentSystemDefault()).date
            date.minus(date.dayOfWeek.ordinal, DateTimeUnit.DAY)
        }

    return weekStarts.map { monday ->
        val weekTasks = tasksByWeek[monday] ?: emptyList()
        DayStats(
            date      = monday,
            proposed  = weekTasks.size,
            completed = weekTasks.count { it.completed }
        )
    }
}

fun aggregateByMonth(taskStat :Sequence<CompletionStat>): List<DayStats> {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    val months = (0..4).map { today.minus(it, DateTimeUnit.MONTH) }

    val tasksByMonth = taskStat.groupBy { task ->

        val date = task.proposedDate
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date

        months.firstOrNull { date >= it } ?: months.last()
    }

    return months.map { start ->
        val monthTasks = tasksByMonth[start] ?: emptyList()
        DayStats(
            date      = start,
            proposed  = monthTasks.size,
            completed = monthTasks.count { it.completed }
        )
    }.reversed()
}

fun aggregateByQuarter(taskStat :Sequence<CompletionStat>): List<DayStats> {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    val quarters = (0..4).map { today.minus(it*3, DateTimeUnit.MONTH) }

    val tasksByQuarter = taskStat.groupBy { task ->

        val date = task.proposedDate
            .toLocalDateTime(TimeZone.currentSystemDefault()).date

        quarters.firstOrNull { date >= it } ?: quarters.last()
    }

    return quarters.map { start ->
        val quarterTasks = tasksByQuarter[start] ?: emptyList()
        DayStats(
            date      = start,
            proposed  = quarterTasks.size,
            completed = quarterTasks.count { it.completed }
        )
    }.reversed()
}

fun aggregateByYear(taskStat :Sequence<CompletionStat>): List<DayStats> {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val endOfYear = LocalDate(today.year, 1, 1)


    val years = (0..4).map { endOfYear.minus(it, DateTimeUnit.YEAR) }

    val tasksByYear = taskStat.groupBy { task ->

        val date = task.proposedDate
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date

        years.firstOrNull { date >= it } ?: years.last()
    }

    return years.map { start ->
        val yearTasks = tasksByYear[start] ?: emptyList()
        DayStats(
            date      = start,
            proposed  = yearTasks.size,
            completed = yearTasks.count { it.completed }
        )
    }.reversed()
}