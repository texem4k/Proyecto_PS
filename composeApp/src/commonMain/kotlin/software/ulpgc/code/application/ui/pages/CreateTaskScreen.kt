package software.ulpgc.code.application.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import software.ulpgc.code.architecture.control.commands.CommandBuilder
import software.ulpgc.code.architecture.control.commands.CommandLauncher
import software.ulpgc.code.architecture.control.commands.CommandType
import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.tasks.Task
import software.ulpgc.code.architecture.model.tasks.TaskInterval
import software.ulpgc.code.architecture.model.times.Time
import software.ulpgc.code.architecture.model.times.TimeFactory
import kotlin.onFailure
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid


data class FormState(
    var taskName: String = "",
    var taskDescription: String = "",
    var taskTopic: Uuid? = null,
    var taskTags: List<Uuid> = emptyList(),
    var taskStartDateString: String = "",
    var taskStartDate: Instant? = null,
    var taskFinalDateString: String = "",
    var taskFinalDate: Instant? = null,
    var taskInterval: TaskInterval = TaskInterval.NONE,
    var taskDuration: String = "",
    var taskPriority: String = "",
    var taskStartHour: String = "",
    var taskFinalHour: String = ""
)

enum class CreateMode {
    SIMPLE,
    WITH_TIME,
    WITH_DURATION
}


@Composable
fun CreateTask(store: Storage, onClose: () -> Unit, task: Task? = null) {

    var form by remember { mutableStateOf(FormState()) }
    var formError by remember { mutableStateOf(false) }
    var messageError: String? by remember { mutableStateOf("") }
    val checkedState = remember { mutableStateOf(false) }
    var expand by remember { mutableStateOf(false) }
    var selectedPeriod by remember { mutableStateOf("Periodo") }
    var mode by remember { mutableStateOf(CreateMode.SIMPLE) }
    var createTask by remember { mutableStateOf(false) }

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
                taskTags = task.tags.toList(),
            )
        } else {
            form = FormState()
        }
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = "Crear tarea",
                modifier = Modifier.align(Alignment.Center),
                fontSize = 24.sp
            )
            Button(
                modifier = Modifier.align(Alignment.CenterEnd),
                onClick = { formError = false; onClose() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            ) {
                Text("✖\uFE0E")
            }
        }
    }

    if (formError) {
        AlertDialog(
            onDismissRequest = { formError = false },
            title = { Text("Error") },
            text = { messageError?.let { Text(it) } },
            confirmButton = {
                Button(onClick = { formError = false; createTask = false }) {
                    Text("Aceptar")
                }
            }
        )
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
            label = " Prioridad",
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

        DropdownCustom(
            section = "* Selecciona el tópico:",
            items = store.topics().toList(),
            selection = DropdownSelection.Single(form.taskTopic),
            onItemSelected = { form = form.copy(taskTopic = it) },
            itemId = { it.id },
            itemName = { it.name }
        )

        DropdownCustom(
            section = "Selecciona los tags:",
            items = store.tags().filter { it.topicId == form.taskTopic }.toList(),
            selection = DropdownSelection.Multiple(form.taskTags),
            onItemSelected = { id ->
                val updatedTags = form.taskTags.toMutableList()
                if (id in updatedTags) updatedTags.remove(id) else updatedTags.add(id)
                form = form.copy(taskTags = updatedTags.toList())
            },
            itemId = { it.id },
            itemName = { it.name }
        )

        Row(
            modifier = Modifier
                .padding(bottom = 16.dp)
                .border(1.dp, Color.Gray, shape = RoundedCornerShape(16.dp))
                .padding(8.dp)
                .fillMaxWidth(0.50f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            fun Modifier.selected(selected: Boolean) =
                this.background(
                    if (selected) Color.LightGray else Color.Transparent,
                    shape = RoundedCornerShape(50)
                )

            @Composable
            fun ModeButton(onClick: () -> Unit, selected: Boolean, icon: ImageVector, label: String) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .selected(selected)
                        .clickable { onClick() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(icon, contentDescription = label, modifier = Modifier.size(32.dp))
                    Text(text = label, fontSize = 9.sp, textAlign = TextAlign.Center, lineHeight = 11.sp)
                }
            }

            ModeButton(
                onClick = { mode = CreateMode.SIMPLE },
                selected = mode == CreateMode.SIMPLE,
                icon = Icons.Default.CalendarToday,
                label = "Inicio y\nfin"
            )
            ModeButton(
                onClick = { mode = CreateMode.WITH_TIME },
                selected = mode == CreateMode.WITH_TIME,
                icon = Icons.Default.Schedule,
                label = "Inicio y\nduración"
            )
            ModeButton(
                onClick = { mode = CreateMode.WITH_DURATION },
                selected = mode == CreateMode.WITH_DURATION,
                icon = Icons.Default.HourglassEmpty,
                label = "Fin y\nduración"
            )
        }

        when (mode) {
            CreateMode.SIMPLE -> {
                Row(
                    modifier = Modifier.fillMaxWidth(0.50f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DatePickerField(
                        value = form.taskStartDateString,
                        onValueChange = { str, instant ->
                            form = form.copy(taskStartDateString = str, taskStartDate = instant)
                        },
                        label = "Fecha de inicio",
                        modifier = Modifier.weight(1f)
                    )
                    TimePickerField(
                        value = form.taskStartHour,
                        onValueChange = { form = form.copy(taskStartHour = it) },
                        type = "inicio",
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(0.50f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DatePickerField(
                        value = form.taskFinalDateString,
                        onValueChange = { str, instant ->
                            form = form.copy(taskFinalDateString = str, taskFinalDate = instant)
                        },
                        label = "Fecha de fin",
                        modifier = Modifier.weight(1f)
                    )
                    TimePickerField(
                        value = form.taskFinalHour,
                        onValueChange = { form = form.copy(taskFinalHour = it) },
                        type = "final",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            CreateMode.WITH_TIME -> {
                Row(
                    modifier = Modifier.fillMaxWidth(0.50f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DatePickerField(
                        value = form.taskStartDateString,
                        onValueChange = { str, instant ->
                            form = form.copy(taskStartDateString = str, taskStartDate = instant)
                        },
                        label = "Fecha inicio",
                        modifier = Modifier.weight(1f)
                    )
                    TimePickerField(
                        value = form.taskStartHour,
                        onValueChange = { form = form.copy(taskStartHour = it) },
                        type = "inicio",
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = form.taskDuration,
                    onValueChange = {
                        if (it.all { c -> c.isDigit() }) form = form.copy(taskDuration = it)
                    },
                    label = { Text("Duración (horas)") },
                    modifier = Modifier.fillMaxWidth(0.50f).padding(bottom = 16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(32.dp)
                )
            }

            CreateMode.WITH_DURATION -> {
                Row(
                    modifier = Modifier.fillMaxWidth(0.50f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DatePickerField(
                        value = form.taskFinalDateString,
                        onValueChange = { str, instant ->
                            form = form.copy(taskFinalDateString = str, taskFinalDate = instant)
                        },
                        label = "Fecha final",
                        modifier = Modifier.weight(1f)
                    )
                    TimePickerField(
                        value = form.taskFinalHour,
                        onValueChange = { form = form.copy(taskFinalHour = it) },
                        type = "final",
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = form.taskDuration,
                    onValueChange = {
                        if (it.all { c -> c.isDigit() }) form = form.copy(taskDuration = it)
                    },
                    label = { Text("Duración (horas)") },
                    modifier = Modifier.fillMaxWidth(0.50f).padding(bottom = 16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(32.dp)
                )
            }
        }

        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.padding(bottom = 8.dp)) {
            Checkbox(
                checked = checkedState.value,
                onCheckedChange = { checkedState.value = it }
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
                                form.taskStartDate = createInstantFromDateAndHour(form.taskStartDate!!, form.taskStartHour)
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
                                form.taskFinalDate = createInstantFromDateAndHour(form.taskFinalDate!!, form.taskFinalHour)
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
                                form.taskStartDate = createInstantFromDateAndHour(form.taskStartDate!!, form.taskStartHour)
                                form.taskFinalDate = createInstantFromDateAndHour(form.taskFinalDate!!, form.taskFinalHour)
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
                            val command =CommandBuilder(store).set("id", task.id.toString()).build(CommandType.UPDATE_TASK)
                            command
                                .onSuccess { CommandLauncher.launch(it) }
                                .onFailure { println("error: ${it.message}") }
                        } else {
                            val command = CommandBuilder(store).build(CommandType.CREATE_TASK)

                            command
                                .onSuccess { CommandLauncher.launch(it) }
                                .onFailure { println("error: ${it.message}") }
                        }
                        onClose()
                    }

                } catch (e: Throwable) {
                    messageError = e.message ?: "Error inesperado"
                    formError = true
                }
            }
        ) {
            if (task != null) Text("Editar tarea") else Text("Crear tarea")
        }
    }
}


fun isValidDate(date: Instant?, type: String): String {
    if (date != null && date < Instant.fromEpochMilliseconds(Clock.System.now().toEpochMilliseconds())) {
        return "La fecha $type no puede ser anterior a la fecha actual"
    }
    return ""
}

fun createInstantFromDateAndHour(dateInstant: Instant, hora: String): Instant {
    val tz = TimeZone.currentSystemDefault()
    val localDate = dateInstant.toLocalDateTime(tz).date
    val parts = hora.split(':')
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
    return LocalDateTime(
        year = localDate.year,
        month = localDate.month,
        dayOfMonth = localDate.dayOfMonth,
        hour = hour,
        minute = minute,
        second = 0,
        nanosecond = 0
    ).toInstant(tz)
}

fun validateDateErrorMessage(e: Exception, m: String): String {
    if (e.toString().contains("Argument") && e.message.toString() == m) return m
    return "Los valores de día y mes deben ser correctos (0-31/1-12)"
}

fun Instant.toFormattedHour(timeZone: TimeZone = TimeZone.currentSystemDefault()): String {
    val localDateTime = this.toLocalDateTime(timeZone)
    return "${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}"
}

fun Instant.toFormattedDate(timeZone: TimeZone = TimeZone.currentSystemDefault()): String {
    val localDate = this.toLocalDateTime(timeZone).date
    return "${localDate.day.toString().padStart(2, '0')}${localDate.month.number.toString().padStart(2, '0')}${localDate.year}"
}

@Composable
fun TextFieldCustom(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    placeholder: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it) },
        label = { Text(label) },
        isError = label.contains("tarea") || label.contains("Prioridad") && value.isBlank(),
        modifier = Modifier.fillMaxWidth(0.50f).padding(bottom = 16.dp),
        keyboardOptions = keyboardOptions,
        placeholder = placeholder?.let { { Text(it) } },
        shape = RoundedCornerShape(32.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropdownCustom(
    section: String,
    items: List<T>,
    selection: DropdownSelection,
    onItemSelected: (Uuid) -> Unit,
    itemId: (T) -> Uuid,
    itemName: (T) -> String
) {
    var expanded by remember { mutableStateOf(false) }

    val displayText = when (selection) {
        is DropdownSelection.Single ->
            items.find { itemId(it) == selection.id }?.let { itemName(it) } ?: "Seleccionar..."
        is DropdownSelection.Multiple ->
            if (selection.ids.isEmpty()) "Seleccionar..."
            else items.filter { itemId(it) in selection.ids }.joinToString(", ") { itemName(it) }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.padding(bottom = 16.dp)
    ) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            label = { Text(section) },
            modifier = Modifier.menuAnchor().fillMaxWidth(0.50f),
            shape = RoundedCornerShape(32.dp),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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
                    },
                    trailingIcon = {
                        if (isSelected) Icon(Icons.Default.Check, contentDescription = null)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    value: String,
    onValueChange: (String, Instant) -> Unit,  // ← string + instant
    label: String,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }

    val displayText = if (value.length == 8) {
        "${value.take(2)}/${value.substring(2, 4)}/${value.drop(4)}"
    } else ""

    Box(modifier = modifier.padding(bottom = 16.dp)) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text("Seleccionar fecha") },
            trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        )
        Box(modifier = Modifier.matchParentSize().clickable { showPicker = true })
    }

    if (showPicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val instant = Instant.fromEpochMilliseconds(millis)
                        val tz = TimeZone.currentSystemDefault()
                        onValueChange(instant.toFormattedDate(tz), instant)
                    }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    type: String
) {
    var showPicker by remember { mutableStateOf(false) }

    val initialHour = value.split(':').getOrNull(0)?.toIntOrNull() ?: 0
    val initialMinute = value.split(':').getOrNull(1)?.toIntOrNull() ?: 0

    Box(modifier = modifier.padding(bottom = 16.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text("Hora de $type") },
            placeholder = { Text("hh:mm") },
            trailingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        )
        Box(modifier = Modifier.matchParentSize().clickable { showPicker = true })
    }

    if (showPicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = initialHour,
            initialMinute = initialMinute,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val h = timePickerState.hour.toString().padStart(2, '0')
                    val m = timePickerState.minute.toString().padStart(2, '0')
                    onValueChange("$h:$m")
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancelar") }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }
}

sealed class DropdownSelection {
    data class Single(val id: Uuid?) : DropdownSelection()
    data class Multiple(val ids: List<Uuid>) : DropdownSelection()
}