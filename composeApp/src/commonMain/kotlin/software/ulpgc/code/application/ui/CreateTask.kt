package software.ulpgc.code.application.ui
import androidx.compose.foundation.BorderStroke
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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import software.ulpgc.code.architecture.control.commands.CommandBuilder
import software.ulpgc.code.architecture.control.commands.CommandLauncher
import software.ulpgc.code.architecture.control.commands.CommandType
import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.tasks.Task
import software.ulpgc.code.architecture.model.tasks.TaskInterval
import kotlin.onFailure
import kotlin.time.Instant
import kotlin.uuid.Uuid

fun Modifier.selected(selected: Boolean): Modifier {
    return this.background(
        if (selected) Color.LightGray else Color.Transparent,
        shape = RoundedCornerShape(50)
    )
}


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
    DATES,
    START_DURATION,
    END_DURATION
}


@Composable
fun CreateTask(store: Storage, onClose: () -> Unit, task: Task? = null, initialDate: LocalDate? = null) {

    var form by remember { mutableStateOf(FormState()) }
    var formError by remember { mutableStateOf(false) }
    var messageError: String? by remember { mutableStateOf("") }
    val checkedState = remember { mutableStateOf(false) }
    var expand by remember { mutableStateOf(false) }
    var selectedPeriod by remember { mutableStateOf("Periodo") }
    var mode by remember { mutableStateOf(CreateMode.DATES) }
    val action = if (task != null) "Editar" else "Crear"

    @Composable
    fun Duracion(){
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
        } else if (initialDate != null){
            val instant = initialDate.atStartOfDayIn(TimeZone.currentSystemDefault())
            form = FormState(
                taskStartDate = instant,
                taskStartDateString = instant.toFormattedDate(TimeZone.currentSystemDefault()),
            )
        }
        else {
            form = FormState()
        }
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = "$action tarea",
                modifier = Modifier.align(Alignment.Center),
                fontSize = 24.sp
            )
            Button(
                modifier = Modifier.align(Alignment.CenterEnd),
                onClick = { formError = false; onClose() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
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
                Button(onClick = { formError = false}) {
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
            keyboardOptions = KeyboardOptions.Default,
        )

        TextFieldCustom(
            value = form.taskDescription,
            label = "Descripción",
            onValueChange = { form = form.copy(taskDescription = it) },
            keyboardOptions = KeyboardOptions.Default
        )

        TextFieldCustom(
            value = form.taskPriority,
            label = "* Prioridad",
            onValueChange = { newValue ->
                val filtered = newValue.filter { it.isDigit() }.take(2)
                val number = filtered.toIntOrNull()

                if (filtered.isEmpty() || (number != null && number in 1..10)) {
                    form = form.copy(taskPriority = filtered)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = "1 - 10"
        )

        DropdownCustom(
            section = "* Selecciona el tópico",
            items = store.topics().toList(),
            selection = DropdownSelection.Single(form.taskTopic),
            onItemSelected = { form = form.copy(taskTopic = it) },
            itemId = { it.id },
            itemName = { it.name }
        )

        DropdownCustom(
            section = "Selecciona los tags",
            items = store.tags()
                .filter { form.taskTopic == null || it.topicId == form.taskTopic }
                .toList(),
            selection = DropdownSelection.Multiple(form.taskTags),
            onItemSelected = { id ->
                form = form.copy(
                    taskTags = form.taskTags.toMutableList().apply {
                        if (contains(id)) remove(id) else add(id)
                    }
                )
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

            ModeButton(
                onClick = { mode = CreateMode.DATES },
                selected = mode == CreateMode.DATES,
                icon = Icons.Default.CalendarToday,
                label = "Inicio y\nfin"
            )
            ModeButton(
                onClick = { mode = CreateMode.START_DURATION },
                selected = mode == CreateMode.START_DURATION,
                icon = Icons.Default.Schedule,
                label = "Inicio y\nduración"
            )
            ModeButton(
                onClick = { mode = CreateMode.END_DURATION },
                selected = mode == CreateMode.END_DURATION,
                icon = Icons.Default.HourglassEmpty,
                label = "Fin y\nduración"
            )
        }

        when (mode) {
            CreateMode.DATES -> {
                Row(
                    modifier = Modifier.fillMaxWidth(0.50f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DatePickerField(
                        value = form.taskStartDateString,
                        onValueChange = { str, instant ->
                            form = form.copy(taskStartDateString = str, taskStartDate = instant)
                        },
                        label = "* Fecha de inicio",
                        modifier = Modifier.weight(1f)
                    )
                    TimePickerField(
                        value = form.taskStartHour,
                        onValueChange = { form = form.copy(taskStartHour = it) },
                        type = "Inicio",
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
                        label = "* Fecha de fin",
                        modifier = Modifier.weight(1f)
                    )
                    TimePickerField(
                        value = form.taskFinalHour,
                        onValueChange = { form = form.copy(taskFinalHour = it) },
                        type = "Final",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            CreateMode.START_DURATION -> {
                DateTimeRow(
                    date = form.taskStartDateString,
                    onDateChange = { str, instant ->
                        form = form.copy(taskStartDateString = str, taskStartDate = instant)
                    },
                    hour = form.taskStartHour,
                    onHourChange = { form = form.copy(taskStartHour = it) },
                    labelDate = "* Fecha inicio",
                    labelTime = "Inicio"
                )
                Duracion()
            }

            CreateMode.END_DURATION -> {
                DateTimeRow(
                    date = form.taskStartDateString,
                    onDateChange = { str, instant ->
                        form = form.copy(taskStartDateString = str, taskStartDate = instant)
                    },
                    hour = form.taskStartHour,
                    onHourChange = { form = form.copy(taskStartHour = it) },
                    labelDate = "* Fecha final",
                    labelTime = "Final"
                )
                Duracion()
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
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary
                    ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp
            ),
            modifier = Modifier.padding(top = 32.dp),
            onClick = {
                formError = false
                messageError = ""

                try {
                    val error = validateBasicFields(form)

                    if (error != null) {
                        messageError = error
                        formError = true
                        return@Button
                    }

                    val time = try {
                        buildTime(form)
                    } catch (e: Exception) {
                        messageError = validateDateErrorMessage(e, "")
                        formError = true
                        return@Button
                    }

                    val builder = CommandBuilder(store)
                        .set("priority", form.taskPriority)
                        .set("name", form.taskName)
                        .set("userId", "00000000-0000-0000-0000-000026033100")
                        .set("description", form.taskDescription)
                        .set("topicId", form.taskTopic.toString())
                        .set("interval", form.taskInterval.toString())
                        .set("tags", form.taskTags.joinToString(", "))
                        .set("time", time.toString())

                    val command = if (task != null) {
                        builder.set("id", task.id.toString()).build(CommandType.UPDATE_TASK)
                    } else {
                        builder.build(CommandType.CREATE_TASK)
                    }

                    command
                        .onSuccess { CommandLauncher.launch(it) }
                        .onFailure { println("error: ${it.message}") }

                    onClose()

                } catch (e: Throwable) {
                    messageError = e.message ?: "Error inesperado"
                    formError = true
                }
            }
        ) {
            Text("$action tarea")
        }
    }
}

@Composable
fun DateTimeRow(
    date: String,
    onDateChange: (String, Instant?) -> Unit,
    hour: String,
    onHourChange: (String) -> Unit,
    labelDate: String,
    labelTime: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(0.50f),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DatePickerField(
            value = date,
            onValueChange = onDateChange,
            label = labelDate,
            modifier = Modifier.weight(1f)
        )
        TimePickerField(
            value = hour,
            onValueChange = onHourChange,
            type = labelTime,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun ModeButton(
    onClick: () -> Unit,
    selected: Boolean,
    icon: ImageVector,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .selected(selected)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            modifier = Modifier.size(32.dp)
        )

        Text(
            text = label,
            fontSize = 9.sp,
            textAlign = TextAlign.Center,
            lineHeight = 11.sp
        )
    }
}