package software.ulpgc.code.application.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import software.ulpgc.code.application.ui.FormState
import software.ulpgc.code.architecture.io.Store
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

fun validateGroupStep0(form: CreateGroupFormState): String? {
    if (form.groupName.isBlank()) return "El nombre del grupo es obligatorio."
    return null
}

fun isValidEmail(email: String): Boolean =
    email.isNotBlank() && Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$").matches(email)

fun validateStep0(form: FormState): String? {
    if (form.taskName.isBlank()) return "El nombre de la tarea es obligatorio."
    if (form.taskPriority.isBlank()) return "La prioridad es obligatoria (1–10)."
    return null
}

fun validateStep1(form: FormState): String? {
    if (form.taskTopic == null) return "Debes seleccionar un tópico."
    return null
}


@Composable
fun TopicsNotExists(onClose: () -> Unit) {
    Dialog(
        onDismissRequest = onClose, // Al cerrar el warning, cierra el flujo entero
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "No hay tópicos existentes",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    text = "En el grupo \"${Store.currentGroup().name}\" no existen tópicos, " +
                            "por lo que no podrás crear una tarea ni tags. Puedes crear un tópico pulsando " +
                            "el botón \"+\" en el Home o en la página de tareas.",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text("Entendido")
                }
            }
        }
    }
}
