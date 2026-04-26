package software.ulpgc.code.application.ui.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import software.ulpgc.code.application.ui.DateTextField
import software.ulpgc.code.application.ui.Headers
import software.ulpgc.code.architecture.control.CommandBuilder
import software.ulpgc.code.architecture.control.CommandLauncher
import software.ulpgc.code.architecture.control.CommandType
import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.tasks.Task
import software.ulpgc.code.architecture.model.tasks.TaskInterval
import software.ulpgc.code.architecture.model.times.Time
import software.ulpgc.code.architecture.model.times.TimeFactory
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

data class FormState(
    var taskName: String = "",
    var taskDescription: String = "",
    var taskTopic: Uuid? = null,
    var taskTags: MutableList<Uuid> = mutableListOf(),
    var taskStartDateString: String = "",
    var taskStartDate: Instant? = null,
    var taskFinalDateString: String = "",
    var taskFinalDate: Instant? = null,
    var taskInterval: TaskInterval = TaskInterval.NONE,
    var taskDuration: String = "",
    var taskPriority: String = "",
    var taskStartHour: String="",
    var taskFinalHour: String=""
)


@Composable
fun CreateTask(store: Storage, onClose: () -> Unit, task: Task? = null) {

    var form by remember { mutableStateOf(FormState()) }
    var createTask by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf(false) }
    var messageError: String? by remember { mutableStateOf("") }
    val checkedState = remember { mutableStateOf(false) }
    var expand by remember { mutableStateOf(false) }
    var selectedPeriod by remember { mutableStateOf("Periodo") }
    var cabecera by remember { mutableStateOf(if (task != null) "Editar tarea" else "Crear tarea") }

    val scrollState = rememberScrollState()
    LaunchedEffect(task) {
        if (task != null) {
            form = FormState(
                taskName = task.name,
                taskDescription = task.description,
                taskTopic = task.topicId,
                taskPriority = task.priority.toString(),
                taskStartDate = task.time.start,
                taskStartDateString = task.time.start.toFormattedDate(TimeZone.currentSystemDefault()),
                taskFinalDate = task.time.end,
                taskFinalDateString = task.time.end.toFormattedDate(TimeZone.currentSystemDefault()),
                taskStartHour = task.time.start.toFormattedHour(TimeZone.currentSystemDefault()),
                taskFinalHour = task.time.end.toFormattedHour(TimeZone.currentSystemDefault()),
                taskTags = task.tags.toMutableList(),
            )
        } else {
            form = FormState()
        }
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Headers(cabecera, onClose = onClose)
    }
    Column(
        modifier = Modifier.fillMaxSize().fillMaxWidth(0.5f).padding(16.dp).verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextFieldCustom(
            value = form.taskName,
            label = "* Nombre tarea",
            onValueChange = { form = form.copy(taskName = it) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )

        TextFieldCustom(
            value = form.taskDescription,
            label = "Descripción",
            onValueChange = { form = form.copy(taskDescription = it) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )

        TextFieldCustom(
            value = form.taskPriority,
            label = "Prioridad",
            onValueChange = { newValue ->
                if (newValue.isEmpty()) {
                    form = form.copy(taskPriority = newValue)
                } else if (newValue.all { it.isDigit() }) {
                    val number = newValue.toIntOrNull()
                    if (number != null && number in 1..10) {
                        form = form.copy(taskPriority = newValue)
                    }
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = "Seleccione del 1 al 10"
        )

        val read = task == null && form.taskStartDateString.isNotEmpty() && form.taskFinalDateString.isNotEmpty()

        DateTextField(
            value = form.taskStartDateString,
            onValueChange = { form = form.copy(taskStartDateString = it) },
            label = "Fecha de inicio",
            modifier = Modifier.padding(bottom = 16.dp),
            read = (task == null && form.taskFinalDateString.isNotEmpty() && form.taskDuration.isNotEmpty()),
        )

        TimeTextField(
            value = form.taskStartHour,
            onValueChange = { form = form.copy(taskStartHour = it) },
            modifier = Modifier.padding(bottom = 16.dp),
            type = "inicio",
            read = (task == null && form.taskFinalDateString.isNotEmpty() && form.taskDuration.isNotEmpty())
        )

        DateTextField(
            value = form.taskFinalDateString,
            onValueChange = { form = form.copy(taskFinalDateString = it) },
            label = "Fecha de fin",
            modifier = Modifier.padding(bottom = 16.dp),
            read = (task == null && form.taskStartDateString.isNotEmpty() && form.taskDuration.isNotEmpty())
        )

        TimeTextField(
            value = form.taskFinalHour,
            onValueChange = { form = form.copy(taskFinalHour = it) },
            modifier = Modifier.padding(bottom = 16.dp),
            type = "finalización",
            read = (task == null && form.taskStartDateString.isNotEmpty() && form.taskDuration.isNotEmpty())
        )

        TextField(
            value = form.taskDuration,
            onValueChange = { newValue ->
                if (newValue.isEmpty()) {
                    form = form.copy(taskDuration = newValue)
                } else if (newValue.all { it.isDigit() }) {
                    val number = newValue.toIntOrNull()
                    if (number != null && number in 0..100) {
                        form = form.copy(taskDuration = newValue)
                    }
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.padding(bottom = 16.dp, top = 16.dp),
            label = { Text("Duración de la tarea (en horas)") },
            readOnly = read,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = if (read) Color.Gray else Color.Unspecified,
                unfocusedContainerColor = if (read) Color.Gray else Color.Unspecified,
                focusedTextColor = if (read) Color.DarkGray else Color.Black,
                unfocusedTextColor = if (read) Color.DarkGray else Color.Black,
                focusedIndicatorColor = if (read) Color.Transparent else Color.Blue
            ),
        )

        DropdownCustom(
            section = "* Selecciona el tópico:",
            items = store.topics().toList(),
            selection = DropdownSelection.Single(form.taskTopic),
            onItemSelected = {
                form = form.copy(taskTopic = it, taskTags = mutableListOf())
            },
            itemId = { it.id },
            itemName = { it.name }
        )

        DropdownCustom(
            section = "Selecciona el/los tag asociado al tópico asociado:",
            items = store.tags().filter { it.topicId == form.taskTopic }.toList(),
            selection = DropdownSelection.Multiple(form.taskTags),
            onItemSelected = { id ->
                val updatedTags = form.taskTags.toMutableList()
                if (id in updatedTags) updatedTags.remove(id)
                else updatedTags.add(id)
                form = form.copy(taskTags = updatedTags)
            },
            itemId = { it.id },
            itemName = { it.name }
        )

        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.padding(bottom = 8.dp)) {
            Checkbox(
                checked = checkedState.value,
                onCheckedChange = { isChecked -> checkedState.value = isChecked }
            )
            Text("Tarea periódica")
        }

        if (checkedState.value) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box {
                    Button(onClick = { expand = true }) { Text(selectedPeriod) }
                    DropdownMenu(
                        expanded = expand,
                        onDismissRequest = { expand = false },
                        modifier = Modifier.fillMaxWidth(0.15f)
                    ) {
                        val periods = listOf("Ninguno", "Diario", "Semanal", "Mensual", "Anual")
                        for (i in 0..4) {
                            DropdownMenuItem(
                                text = { Text(periods[i]) },
                                onClick = {
                                    selectedPeriod = "Periodo seleccionado: ${periods[i]}"
                                    expand = false
                                    form.taskInterval = TaskInterval.entries[i]
                                }
                            )
                        }
                    }
                }
            }
        }

        Button(
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Blue,
                contentColor = Color.White,
            ),
            modifier = Modifier.padding(top = 32.dp),
            onClick = {
                // Resetear estado antes de validar
                createTask = true
                formError = false
                messageError = ""

                try {
                    var m = ""
                    var time: Time? = null

                    if (form.taskName.isEmpty()) {
                        messageError = "La tarea debe tener algún nombre"
                        formError = true
                    } else if (form.taskStartDateString.isEmpty() && form.taskFinalDateString.isEmpty()) {
                        messageError = "Debes rellenar al menos un campo de fecha"
                        formError = true
                    } else if (form.taskStartDateString.length == 8 && form.taskDuration.isNotEmpty()) {
                        try {
                            if (form.taskStartHour.isEmpty()) {
                                messageError = "La hora de inicio no puede estar vacío"
                                formError = true
                            } else {
                                form.taskStartDate = createInstant(form.taskStartDateString, form.taskStartHour)
                                m = isValidDate(form.taskStartDate, "inicial")
                                if (m.isNotEmpty()) throw IllegalArgumentException(m)
                                time = TimeFactory().createTime(form.taskStartDate!!, form.taskDuration.toLong())
                            }
                        } catch (e: Exception) {
                            messageError = validateDateErrorMessage(e, m)
                            formError = true
                        }
                    } else if (form.taskFinalDateString.length == 8 && form.taskDuration.isNotEmpty()) {
                        try {
                            if (form.taskFinalHour.isEmpty()) {
                                messageError = "La hora de finalización no puede estar vacío"
                                formError = true
                            } else {
                                form.taskFinalDate = createInstant(form.taskFinalDateString, form.taskFinalHour)
                                m = isValidDate(form.taskFinalDate, "final")
                                if (m.isNotEmpty()) throw IllegalArgumentException(m)
                                time = TimeFactory().createTime(form.taskDuration.toLong(), form.taskFinalDate!!)
                            }
                        } catch (e: Exception) {
                            messageError = validateDateErrorMessage(e, m)
                            formError = true
                        }
                    } else if (form.taskStartDateString.length == 8 && form.taskFinalDateString.length == 8 && form.taskDuration.isEmpty()) {
                        try {
                            if (form.taskStartHour.isEmpty() || form.taskFinalHour.isEmpty()) {
                                messageError = "La hora final e inicial no puede estar vacío"
                                formError = true
                            } else {
                                form.taskStartDate = createInstant(form.taskStartDateString, form.taskStartHour)
                                form.taskFinalDate = createInstant(form.taskFinalDateString, form.taskFinalHour)
                                time = TimeFactory().createTime(form.taskStartDate!!, form.taskFinalDate!!)
                                if (isValidDate(form.taskStartDate, "inicial").isNotEmpty() ||
                                    isValidDate(form.taskFinalDate, "final").isNotEmpty()) {
                                    throw IllegalArgumentException("Fecha final o inicial incorrecta")
                                }
                            }
                        } catch (e: Exception) {
                            messageError = validateDateErrorMessage(e, m)
                            formError = true
                        }
                    }

                    // ✅ Solo lanzar el comando y cerrar si NO hay errores
                    if (!formError) {
                        var builder = CommandBuilder(store)
                            .set("priority", form.taskPriority)
                            .set("name", form.taskName)
                            .set("userId", "00000000-0000-0000-0000-000026033100")
                            .set("description", form.taskDescription)
                            .set("topicId", form.taskTopic.toString())
                            .set("interval", form.taskInterval.toString())
                            .set("time", time.toString())

                        if (form.taskTags.isNotEmpty()) {
                            builder = builder.set("tags", form.taskTags.joinToString(", "))
                        }

                        if (task != null) {
                            CommandLauncher.launch(builder.set("id", task.id.toString()).build(CommandType.UPDATE_TASK))
                        } else {
                            CommandLauncher.launch(builder.build(CommandType.CREATE_TASK))
                        }
                        onClose() // ✅ Solo se cierra si todo fue bien
                    }

                } catch (e: Throwable) {
                    messageError = e.message ?: "Error inesperado"
                    formError = true
                }
            }
        ) {
            if (task != null) Text("Editar tarea") else Text("Crear tarea")
        }

        if (formError) {
            AlertDialog(
                onDismissRequest = { formError = false },
                title = { Text("Error") },
                text = { messageError?.let { Text(it) } },
                confirmButton = {
                    Button(onClick = {
                        formError = false
                        createTask = false
                    }) {
                        Text("Aceptar")
                    }
                }
            )
        }
    }
}


fun formatDate(digits: String): String {
    return digits.filter { it.isDigit() }.take(8)
}

fun isValidDate(date: Instant?, type: String):String {
    if (date != null) {
        if(date < Clock.System.now().toDatetime()){
            return "La fecha $type no puede ser anterior a la fecha actual"
        }
    }
    return ""
}

private fun Instant.toDatetime(): Instant =
    Instant.fromEpochMilliseconds(this.toEpochMilliseconds())

fun createInstant(fecha: String, hora: String): Instant {
    val parts = hora.split(':')
    val hour = if (parts.size >= 2) parts[0].toIntOrNull() ?: 0 else 0
    val minute = if (parts.size >= 2) parts[1].toIntOrNull() ?: 0 else 0

    return LocalDateTime(
        year = fecha.substring(4, 8).toInt(),
        month = fecha.substring(2, 4).toInt(),
        day = fecha.take(2).toInt(),
        hour = hour,
        minute = minute,
        second = 0,
        nanosecond = 0
    ).toInstant(TimeZone.UTC)
}

fun validateDateErrorMessage(e: Exception, m:String): String{
    if(e.toString().contains("Argument") && e.message.toString() == m) {
        return m
    }
    return "Los valores de día y mes deben ser correctos (0-31/1-12)"
}

@Composable
fun TimeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    type: String,
    read: Boolean=false
) {
    TextField(
        value = value,
        readOnly = read,
        onValueChange = { newValue ->
            val digits = newValue.filter { it.isDigit() }

            if (digits.length > 4) return@TextField

            if (digits.length >= 2) {
                val hours = digits.take(2).toInt()
                if (hours > 23) return@TextField
            }
            if (digits.length >= 4) {
                val minutes = digits.takeLast(2).toInt()
                if (minutes > 59) return@TextField
            }

            val formatted = when {
                digits.length <= 2 -> digits
                else -> "${digits.take(2)}:${digits.drop(2)}"
            }

            onValueChange(formatted)
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        placeholder = { Text("hh:mm") },
        label = { Text("Hora de $type") },
        modifier = modifier,
        colors = TextFieldDefaults.colors(
            // Si es readOnly, usamos Gris; si no, el color normal
            focusedContainerColor = if (read) Color.Gray else Color.Unspecified,
            unfocusedContainerColor = if (read) Color.Gray else Color.Unspecified,
            // También puedes cambiar el color del texto para que se vea "desactivado"
            focusedTextColor = if (read) Color.DarkGray else Color.Black,
            unfocusedTextColor = if (read) Color.DarkGray else Color.Black,

            // Ocultar la línea indicadora si es de solo lectura
            focusedIndicatorColor = if (read) Color.Transparent else Color.Blue
        ),
    )
}


fun Instant.toFormattedHour(
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): String {
    val localDateTime = this.toLocalDateTime(timeZone)
    val hour = localDateTime.hour.toString().padStart(2, '0')
    val minute = localDateTime.minute.toString().padStart(2, '0')
    return "$hour:$minute"
}



fun Instant.toFormattedDate(
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): String {
    val localDate = this.toLocalDateTime(timeZone).date
    val day = localDate.day.toString().padStart(2, '0')
    val month = localDate.month.number.toString().padStart(2, '0')
    val year = localDate.year
    return "$day$month$year"
}



@Composable
fun TextFieldCustom(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    placeholder: String? = null
) {
    TextField(
        value = value,
        onValueChange = { onValueChange(it) }, // Sin condición
        label = { Text(label) },
        isError = label.contains("tarea") && value.isBlank(),
        modifier = Modifier.padding(bottom = 16.dp),
        keyboardOptions = keyboardOptions,
        placeholder = placeholder?.let { { Text(it) } }
    )
}


@Composable
fun <T> DropdownCustom(
    section: String,
    items: List<T>,                    // Lista genérica (topics, priorities, etc.)
    selection: DropdownSelection,              // ID del item seleccionado (nullable)
    onItemSelected: (Uuid) -> Unit,     // Callback cuando se selecciona un item
    itemId: (T) -> Uuid,                // Cómo obtener el ID de un item
    itemName: (T) -> String            // Cómo obtener el nombre de un item
) {
    var expanded by remember { mutableStateOf(false) }


    Text(section)
    Box {
        Button(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(0.15f).padding(bottom = 8.dp)
        ) {
            val displayText = when (selection) {
                is DropdownSelection.Single -> {
                    if (selection.id != null) {
                        itemName(items.first { itemId(it) == selection.id })
                    } else {
                        onItemSelected(itemId(items.first()))
                        itemName(items.first())
                    }
                }

                is DropdownSelection.Multiple -> {
                    if (selection.ids.isEmpty()) "Seleccionar..."
                    else items.filter { itemId(it) in selection.ids }
                        .joinToString(", ") { itemName(it) }
                }
            }
            Text(displayText)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                val isSelected = when (selection) {
                    is DropdownSelection.Single -> itemId(item) == selection.id
                    is DropdownSelection.Multiple -> itemId(item) in selection.ids
                }
                DropdownMenuItem(
                    text = { Text(itemName(item)) },
                    onClick = {
                        onItemSelected(itemId(item))
                        if (selection is DropdownSelection.Single) expanded = false
                        // En Multiple el menú se mantiene abierto para seguir seleccionando
                    },
                    trailingIcon = {
                        if (isSelected) Icon(Icons.Default.Check, contentDescription = null)
                    }
                )
            }
        }
    }
}


sealed class DropdownSelection {
    data class Single(val id: Uuid?) : DropdownSelection()
    data class Multiple(val ids: MutableList<Uuid>) : DropdownSelection()
}