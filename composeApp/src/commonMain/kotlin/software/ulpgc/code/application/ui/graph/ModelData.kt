package software.ulpgc.code.application.ui.graph

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import software.ulpgc.code.architecture.model.tasks.Task

// Estructura intermedia para la gráfica
data class DayStats(
    val date: LocalDate,
    val proposed: Int,
    val completed: Int
)

// Función de agregación
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