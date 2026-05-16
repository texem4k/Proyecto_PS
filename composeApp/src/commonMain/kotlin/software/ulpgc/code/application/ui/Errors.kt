package software.ulpgc.code.application.ui

import kotlin.time.Clock
import kotlin.time.Instant

import software.ulpgc.code.application.ui.FormState as Form
import software.ulpgc.code.architecture.model.times.Time
import software.ulpgc.code.architecture.model.times.TimeFactory

fun validateBasicFields(form: Form): String? {
    return when {
        form.taskName.isEmpty() -> "La tarea debe tener algún nombre"
        form.taskPriority.isEmpty() -> "Debes rellenar el campo de prioridad"
        form.taskTopic == null -> "La tarea debe tener un tópico"
        isAllDatesEmpty(form) -> "Debes rellenar todos los campos de fecha"
        isInvalidDateCombination(form) -> "Debes rellenar todos los campos de fecha"
        else -> null
    }
}

fun isAllDatesEmpty(form: Form) =
    form.taskStartDateString.isEmpty() &&
            form.taskFinalDateString.isEmpty() &&
            form.taskStartHour.isEmpty() &&
            form.taskFinalHour.isEmpty()

fun isInvalidDateCombination(form: Form) =
    (form.taskStartDateString.isEmpty() && form.taskStartHour.isEmpty() && form.taskDuration.isEmpty()) ||
            (form.taskFinalDateString.isEmpty() && form.taskFinalHour.isEmpty() && form.taskDuration.isEmpty())

fun buildTime(form: Form): Time {
    return when {
        form.taskStartDateString.length == 8 && form.taskDuration.isNotEmpty() -> {
            require(form.taskStartHour.isNotEmpty()) { "La hora de inicio no puede estar vacío" }

            val start = createInstantFromDateAndHour(form.taskStartDate!!, form.taskStartHour)
            val error = isValidDate(start, "inicial")
            require(error.isEmpty()) { error }

            TimeFactory.createTime(start, form.taskDuration.toLong())
        }

        form.taskFinalDateString.length == 8 && form.taskDuration.isNotEmpty() -> {
            require(form.taskFinalHour.isNotEmpty()) { "La hora de finalización no puede estar vacío" }

            val end = createInstantFromDateAndHour(form.taskFinalDate!!, form.taskFinalHour)
            val error = isValidDate(end, "final")
            require(error.isEmpty()) { error }

            TimeFactory.createTime(form.taskDuration.toLong(), end)
        }

        form.taskStartDateString.length == 8 && form.taskFinalDateString.length == 8 -> {
            require(form.taskStartHour.isNotEmpty() && form.taskFinalHour.isNotEmpty()) {
                "La hora final e inicial no puede estar vacío"
            }

            val start = createInstantFromDateAndHour(form.taskStartDate!!, form.taskStartHour)
            val end = createInstantFromDateAndHour(form.taskFinalDate!!, form.taskFinalHour)

            require(isValidDate(start, "inicial").isEmpty() &&
                    isValidDate(end, "final").isEmpty()) {
                "Fecha final o inicial incorrecta"
            }

            TimeFactory.createTime(start, end)
        }

        else -> throw IllegalArgumentException("Combinación de fechas inválida")
    }
}

fun isValidDate(date: Instant?, type: String): String {
    if (date != null && date < Instant.fromEpochMilliseconds(Clock.System.now().toEpochMilliseconds())) {
        return "La fecha $type no puede ser anterior a la fecha actual"
    }
    return ""
}

fun validateDateErrorMessage(e: Exception, m: String): String {
    if (e.toString().contains("Argument") && e.message.toString() == m) return m
    return "Los valores de día y mes deben ser correctos (0-31/1-12)"
}

