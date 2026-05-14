package software.ulpgc.code.application.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import software.ulpgc.code.architecture.control.commands.CommandBuilder
import software.ulpgc.code.architecture.control.commands.CommandLauncher
import software.ulpgc.code.architecture.control.commands.CommandType
import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.tasks.Task
import software.ulpgc.code.architecture.model.tasks.TaskInterval
import kotlinx.datetime.Instant
import kotlin.uuid.Uuid


data class FormState(
    var taskName: String = "",
    var taskDescription: String = "",
    var taskTopic: Uuid? = null,
    var taskTags: List<Uuid> = emptyList(),
    var taskStartDateString: String = "",
    var taskStartDate: kotlin.time.Instant? = null,
    var taskFinalDateString: String = "",
    var taskFinalDate: kotlin.time.Instant? = null,
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
fun CreateTask(
    store: Storage,
    onClose: () -> Unit,
    task: Task? = null,
    initialDate: LocalDate? = null
) {
    var form by remember { mutableStateOf(FormState()) }
    var step by remember { mutableStateOf(0) }
    var formError by remember { mutableStateOf(false) }
    var messageError by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableStateOf<TaskInterval?>(null) }
    var periodicEnabled by remember { mutableStateOf(false) }
    var mode by remember { mutableStateOf(CreateMode.DATES) }

    val action = if (task != null) "Editar" else "Crear"
    val totalSteps = 4

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
        } else if (initialDate != null) {
            val instant = initialDate.atStartOfDayIn(TimeZone.currentSystemDefault())
            form = FormState(
                taskStartDate = instant,
                taskStartDateString = instant.toFormattedDate(TimeZone.currentSystemDefault()),
            )
        }
    }

    if (formError) {
        AlertDialog(
            onDismissRequest = { formError = false },
            title = { Text("Error") },
            text = { Text(messageError) },
            confirmButton = {
                Button(onClick = { formError = false }) { Text("Aceptar") }
            }
        )
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.55f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {

                WizardHeader(
                    title = "$action tarea",
                    step = step,
                    totalSteps = totalSteps,
                    onClose = onClose
                )

                Spacer(Modifier.height(20.dp))

                AnimatedContent(
                    targetState = step,
                    transitionSpec = {
                        val direction = if (targetState > initialState) 1 else -1
                        slideInHorizontally(
                            initialOffsetX = { it * direction },
                            animationSpec = tween(260)
                        ) togetherWith slideOutHorizontally(
                            targetOffsetX = { -it * direction },
                            animationSpec = tween(260)
                        )
                    },
                    label = "wizard_step"
                ) { currentStep ->
                    when (currentStep) {
                        0 -> StepBasicInfo(form = form, onFormChange = { form = it })
                        1 -> StepTopicsAndPeriod(
                            store = store,
                            form = form,
                            onFormChange = { form = it },
                            periodicEnabled = periodicEnabled,
                            onPeriodicToggle = { periodicEnabled = it },
                            selectedPeriod = selectedPeriod,
                            onPeriodSelected = { period ->
                                selectedPeriod = period
                                form = form.copy(taskInterval = period ?: TaskInterval.NONE)
                            }
                        )
                        2 -> StepDateTime(
                            form = form,
                            onFormChange = { form = it },
                            mode = mode,
                            onModeChange = { mode = it }
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                WizardNavigation(
                    step = step,
                    totalSteps = totalSteps,
                    actionLabel = action,
                    onBack = { step-- },
                    onNext = {
                        val error = when (step) {
                            0 -> validateStep0(form)
                            1 -> validateStep1(form)
                            else -> null
                        }
                        if (error != null) {
                            messageError = error
                            formError = true
                        } else {
                            step++
                        }
                    },
                    onSubmit = {
                        val error = validateBasicFields(form)
                        if (error != null) {
                            messageError = error
                            formError = true
                            return@WizardNavigation
                        }
                        val time = try {
                            buildTime(form)
                        } catch (e: Exception) {
                            messageError = validateDateErrorMessage(e, "")
                            formError = true
                            return@WizardNavigation
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
                    }
                )
            }
        }
    }
}


@Composable
private fun StepBasicInfo(form: FormState, onFormChange: (FormState) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        StepLabel("Información básica")

        TextFieldCustom(
            value = form.taskName,
            label = "* Nombre tarea",
            onValueChange = { onFormChange(form.copy(taskName = it)) },
            keyboardOptions = KeyboardOptions.Default
        )

        TextFieldCustom(
            value = form.taskDescription,
            label = "Descripción",
            onValueChange = { onFormChange(form.copy(taskDescription = it)) },
            keyboardOptions = KeyboardOptions.Default
        )

        TextFieldCustom(
            value = form.taskPriority,
            label = "* Prioridad (1–10)",
            placeholder = "1 - 10",
            onValueChange = { newValue ->
                val filtered = newValue.filter { it.isDigit() }.take(2)
                val number = filtered.toIntOrNull()
                if (filtered.isEmpty() || (number != null && number in 1..10)) {
                    onFormChange(form.copy(taskPriority = filtered))
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}

@Composable
private fun StepTopicsAndPeriod(
    store: Storage,
    form: FormState,
    onFormChange: (FormState) -> Unit,
    periodicEnabled: Boolean,
    onPeriodicToggle: (Boolean) -> Unit,
    selectedPeriod: TaskInterval?,
    onPeriodSelected: (TaskInterval?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        StepLabel("Clasificación y periodicidad")

        DropdownCustom(
            section = "* Selecciona el tópico",
            items = store.topics().toList(),
            selection = DropdownSelection.Single(form.taskTopic),
            onItemSelected = { onFormChange(form.copy(taskTopic = it)) },
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
                onFormChange(
                    form.copy(
                        taskTags = form.taskTags.toMutableList().apply {
                            if (contains(id)) remove(id) else add(id)
                        }
                    )
                )
            },
            itemId = { it.id },
            itemName = { it.name }
        )

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    if (periodicEnabled)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
                .border(
                    1.dp,
                    if (periodicEnabled)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.outlineVariant,
                    RoundedCornerShape(24.dp)
                )
                .clickable { onPeriodicToggle(!periodicEnabled) }
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "Tarea periódica",
                    style = MaterialTheme.typography.bodyMedium,
                    color =
                        if (periodicEnabled)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurface
                )

                Text(
                    "Se repetirá automáticamente",
                    style = MaterialTheme.typography.bodySmall,
                    color =
                        if (periodicEnabled)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = periodicEnabled,
                onCheckedChange = { onPeriodicToggle(it) }
            )
        }

        AnimatedVisibility(visible = periodicEnabled) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(8.dp))
                DropdownCustom(
                    section = "Período",
                    items = TaskInterval.entries.filter { it != TaskInterval.NONE },
                    selection = DropdownSelection.Single(selectedPeriod),
                    onItemSelected = { onPeriodSelected(it) },
                    itemId = { it },
                    itemName = { it.label }
                )
            }
        }
    }
}

@Composable
private fun StepDateTime(
    form: FormState,
    onFormChange: (FormState) -> Unit,
    mode: CreateMode,
    onModeChange: (CreateMode) -> Unit
) {

    @Composable
    fun ModeButton(
        onClick: () -> Unit,
        selected: Boolean,
        icon: ImageVector,
        label: String
    ) {

        val background =
            if (selected)
                MaterialTheme.colorScheme.primaryContainer
            else
                Color.Transparent

        val contentColor =
            if (selected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(background)
                .clickable { onClick() }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {

            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(32.dp),
                tint = contentColor
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = label,
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
                lineHeight = 11.sp,
                color = contentColor
            )
        }
    }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StepLabel("Fechas y duración")

        Row(
            modifier = Modifier
                .fillMaxWidth(0.55f)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(24.dp)
                )
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant,
                    RoundedCornerShape(24.dp)
                )
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ModeButton(
                onClick = { onModeChange(CreateMode.DATES) },
                selected = mode == CreateMode.DATES,
                icon = Icons.Default.CalendarToday,
                label = "Inicio y\nfin"
            )
            ModeButton(
                onClick = { onModeChange(CreateMode.START_DURATION) },
                selected = mode == CreateMode.START_DURATION,
                icon = Icons.Default.Schedule,
                label = "Inicio y\nduración"
            )
            ModeButton(
                onClick = { onModeChange(CreateMode.END_DURATION) },
                selected = mode == CreateMode.END_DURATION,
                icon = Icons.Default.HourglassEmpty,
                label = "Fin y\nduración"
            )
        }

        when (mode) {
            CreateMode.DATES -> {
                DateTimeRow(
                    date = form.taskStartDateString,
                    onDateChange = { str, instant ->
                        onFormChange(form.copy(taskStartDateString = str, taskStartDate = instant))
                    },
                    labelDate = "* Fecha de inicio",
                    hour = form.taskStartHour,
                    onHourChange = { onFormChange(form.copy(taskStartHour = it)) },
                    labelTime = "Inicio"
                )
                DateTimeRow(
                    date = form.taskFinalDateString,
                    onDateChange = { str, instant ->
                        onFormChange(form.copy(taskFinalDateString = str, taskFinalDate = instant))
                    },
                    labelDate = "* Fecha de fin",
                    hour = form.taskFinalHour,
                    onHourChange = { onFormChange(form.copy(taskFinalHour = it)) },
                    labelTime = "Final"
                )
            }

            CreateMode.START_DURATION -> {
                DateTimeRow(
                    date = form.taskStartDateString,
                    onDateChange = { str, instant ->
                        onFormChange(form.copy(taskStartDateString = str, taskStartDate = instant))
                    },
                    labelDate = "* Fecha inicio",
                    hour = form.taskStartHour,
                    onHourChange = { onFormChange(form.copy(taskStartHour = it)) },
                    labelTime = "Inicio"
                )
                DuracionField(
                    value = form.taskDuration,
                    onChange = { onFormChange(form.copy(taskDuration = it)) }
                )
            }

            CreateMode.END_DURATION -> {
                DateTimeRow(
                    date = form.taskStartDateString,
                    onDateChange = { str, instant ->
                        onFormChange(form.copy(taskStartDateString = str, taskStartDate = instant))
                    },
                    labelDate = "* Fecha final",
                    hour = form.taskStartHour,
                    onHourChange = { onFormChange(form.copy(taskStartHour = it)) },
                    labelTime = "Final"
                )
                DuracionField(
                    value = form.taskDuration,
                    onChange = { onFormChange(form.copy(taskDuration = it)) }
                )
            }
        }
    }
}

@Composable
private fun StepLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun DuracionField(value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.all { c -> c.isDigit() }) onChange(it) },
        label = { Text("Duración (horas)") },
        modifier = Modifier.fillMaxWidth(0.55f),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(32.dp)
    )
}

@Composable
private fun WizardHeader(title: String, step: Int, totalSteps: Int, onClose: () -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onClose) {
                Text("✖\uFE0E", color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val stepLabels = listOf("Básico", "Clasificación", "Fechas")
            stepLabels.forEachIndexed { index, label ->
                StepIndicator(
                    index = index,
                    current = step,
                    label = label
                )
                if (index < stepLabels.lastIndex) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(1.dp)
                            .background(
                                if (index < step) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(index: Int, current: Int, label: String) {
    val isDone = index < current
    val isActive = index == current
    val circleColor = when {
        isDone || isActive -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    val textColor = when {
        isActive -> MaterialTheme.colorScheme.primary
        isDone -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(if (isDone || isActive) circleColor else Color.Transparent)
                .border(1.5.dp, circleColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isDone) "✓" else "${index + 1}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDone || isActive) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.outlineVariant
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun WizardNavigation(
    step: Int,
    totalSteps: Int,
    actionLabel: String,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (step > 0) {
            OutlinedButton(onClick = onBack) {
                Text("← Anterior")
            }
        } else {
            Spacer(Modifier.width(1.dp))
        }

        if (step < totalSteps - 1) {
            Button(onClick = onNext) {
                Text("Siguiente →")
            }
        } else {
            Button(
                onClick = onSubmit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text("$actionLabel tarea")
            }
        }
    }
}

private fun validateStep0(form: FormState): String? {
    if (form.taskName.isBlank()) return "El nombre de la tarea es obligatorio."
    if (form.taskPriority.isBlank()) return "La prioridad es obligatoria (1–10)."
    return null
}

private fun validateStep1(form: FormState): String? {
    if (form.taskTopic == null) return "Debes seleccionar un tópico."
    return null
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
        modifier = Modifier.fillMaxWidth(0.55f),
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