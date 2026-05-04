package software.ulpgc.code.application.ui.graph

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import software.ulpgc.code.architecture.model.tasks.Task
import kotlin.time.Clock

// Estructura intermedia para la gráfica
data class DayStats(
    val date: LocalDate,
    val proposed: Int,
    val completed: Int
)

// Función de agregación

/*
fun aggregateByDay(tasks: List<Task>): List<DayStats> {
    return tasks
        .groupBy { task ->
            task.time.start.toLocalDateTime(TimeZone.currentSystemDefault()).date
        }
        .map { (date, dayTasks) ->
            DayStats(
                date = date,
                proposed = dayTasks.size,
                completed = dayTasks.count { it.isCompleted }
            )
        }
        .sortedBy { it.date }
}

 */

fun aggregateByDay(tasks: List<Task>): List<DayStats> {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val windowStart = today.minus(4, DateTimeUnit.DAY)
    val windowEnd   = today.plus(4, DateTimeUnit.DAY)

    // Mapa de tareas agrupadas por su fecha de inicio
    val tasksByDate = tasks
        .groupBy { task ->
            task.time.start.toLocalDateTime(TimeZone.currentSystemDefault()).date
        }

    // Generar los 9 días consecutivos de la ventana
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

/*
fun aggregateByWeek(tasks: List<Task>): List<DayStats> {
    return tasks
        .groupBy { task ->
            val date = task.time.start.toLocalDateTime(TimeZone.currentSystemDefault()).date
            // Obtener el lunes de esa semana
            date.minus(DatePeriod(days = date.dayOfWeek.ordinal))
        }
        .map { (weekStart, weekTasks) ->
            DayStats(
                date = weekStart,
                proposed = weekTasks.size,
                completed = weekTasks.count { it.isCompleted }
            )
        }
        .sortedBy { it.date }
}

 */
fun aggregateByWeek(tasks: List<Task>): List<DayStats> {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    // Lunes de la semana actual
    val currentMonday = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)

    // Las 4 semanas: desde 3 lunes atrás hasta el lunes actual
    val weekStarts = (3 downTo 0).map { weeksBack ->
        currentMonday.minus(weeksBack * 7, DateTimeUnit.DAY)
    }

    // Mapa de cada tarea → lunes de su semana
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