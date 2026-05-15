package software.ulpgc.code.application.ui.graph

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.yearMonth
import software.ulpgc.code.architecture.model.tasks.Task
import kotlin.time.Clock

data class DayStats(
    val date: LocalDate,
    val proposed: Int,
    val completed: Int
)


fun aggregateByDay(tasks: List<Task>): List<DayStats> {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val windowStart = today.minus(4, DateTimeUnit.DAY)
    val windowEnd   = today.plus(4, DateTimeUnit.DAY)
    val tasksByDate = tasks
        .groupBy { task ->
            task.time.start.toLocalDateTime(TimeZone.currentSystemDefault()).date
        }

    return buildList {
        var current = windowStart
        while (current <= windowEnd) {
            val dayTasks = tasksByDate[current] ?: emptyList()
            add(
                DayStats(
                    date      = current,
                    proposed  = dayTasks.size,
                    completed = dayTasks.count { it.isCompleted }
                )
            )
            current = current.plus(1, DateTimeUnit.DAY)
        }
    }
}

fun aggregateByWeek(tasks: List<Task>): List<DayStats> {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    val currentMonday = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)

    val weekStarts = (3 downTo 0).map { weeksBack ->
        currentMonday.minus(weeksBack * 7, DateTimeUnit.DAY)
    }

    val tasksByWeek = tasks
        .groupBy { task ->
            val date = task.time.start.toLocalDateTime(TimeZone.currentSystemDefault()).date
            date.minus(date.dayOfWeek.ordinal, DateTimeUnit.DAY)
        }

    return weekStarts.map { monday ->
        val weekTasks = tasksByWeek[monday] ?: emptyList()
        DayStats(
            date      = monday,
            proposed  = weekTasks.size,
            completed = weekTasks.count { it.isCompleted }
        )
    }
}

fun aggregateByMonth(tasks: List<Task>): List<DayStats> {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    val months = (1..4).map { today.minus(it, DateTimeUnit.MONTH) }

    val tasksByMonth = tasks.groupBy { task ->

        val date = task.time.start
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date

        months.firstOrNull { date >= it } ?: months.last()
    }

    return months.map { start ->
        val monthTasks = tasksByMonth[start] ?: emptyList()
        DayStats(
            date      = start,
            proposed  = monthTasks.size,
            completed = monthTasks.count { it.isCompleted }
        )
    }.reversed()
}

fun aggregateByQuarter(tasks: List<Task>): List<DayStats> {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    val years = (1..4).map { today.minus(it, DateTimeUnit.YEAR) }

    val tasksByMonth = tasks.groupBy { task ->

        val date = task.time.start
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date

        years.firstOrNull { date >= it } ?: years.last()
    }

    return years.map { start ->
        val monthTasks = tasksByMonth[start] ?: emptyList()
        DayStats(
            date      = start,
            proposed  = monthTasks.size,
            completed = monthTasks.count { it.isCompleted }
        )
    }.reversed()
}

fun aggregateByYear(tasks: List<Task>): List<DayStats> {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    val years = (1..4).map { today.minus(it, DateTimeUnit.YEAR) }

    val tasksByMonth = tasks.groupBy { task ->

        val date = task.time.start
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date

        years.firstOrNull { date >= it } ?: years.last()
    }

    return years.map { start ->
        val monthTasks = tasksByMonth[start] ?: emptyList()
        DayStats(
            date      = start,
            proposed  = monthTasks.size,
            completed = monthTasks.count { it.isCompleted }
        )
    }.reversed()
}