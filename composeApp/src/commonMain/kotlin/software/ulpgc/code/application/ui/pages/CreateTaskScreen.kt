package software.ulpgc.code.application.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.NavigationDrawerItemDefaults.colors
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDateTime
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import software.ulpgc.code.application.ui.DateTextField
import software.ulpgc.code.application.ui.Headers
import software.ulpgc.code.application.ui.Screen
import software.ulpgc.code.architecture.control.CommandBuilder
import software.ulpgc.code.architecture.control.CommandLauncher
import software.ulpgc.code.architecture.control.CommandType
import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.tasks.Task
import software.ulpgc.code.architecture.model.tasks.TaskInterval
import software.ulpgc.code.architecture.model.times.Time
import software.ulpgc.code.architecture.model.times.TimeFactory
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
fun CreateTaskScreen(onNavigate: (Screen) -> Unit, store: Storage, task: Task? = null) {

    var expandedTopics by remember { mutableStateOf(false) }
    var expandedTag by remember { mutableStateOf(false) }
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
                taskDuration = task.time.end.minus(task.time.start).inWholeHours.toString()
            )
        } else {
            form = FormState()
        }
    }

    Row (modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Headers(onNavigate, cabecera)
    }
    Column(
        modifier = Modifier.fillMaxSize().fillMaxWidth(0.5f).padding(16.dp).verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = form.taskName,
            onValueChange = { form = form.copy(taskName = it) },
            placeholder = {Text("")},
            modifier = Modifier.padding(bottom = 16.dp),
            label = {Text("* Nombre de la tarea")},
            isError = form.taskName.isBlank(),
        )

        TextField(
            value = form.taskDescription,
            onValueChange = { form = form.copy(taskDescription = it) },
            placeholder = {Text("")},
            label = { Text("Descripción") },
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TextField(
            value = form.taskPriority,
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
            placeholder = { Text("Seleccione del 1 al 10") },
            label = { Text("Prioridad") },
            modifier = Modifier.padding(bottom = 16.dp)
        )

        DateTextField(
            value = form.taskStartDateString,
            onValueChange = { form = form.copy(taskStartDateString = it) }, label = "Fecha de inicio",
            modifier = Modifier.padding(bottom = 16.dp),
            read = (task == null && form.taskFinalDateString.isNotEmpty() &&form.taskDuration.isNotEmpty()),
        )

        TimeTextField(
            value = form.taskStartHour,
            onValueChange = {form = form.copy(taskStartHour = it) },
            modifier = Modifier.padding(bottom = 16.dp),
            type="inicio",
            read=(task == null && form.taskFinalDateString.isNotEmpty() &&form.taskDuration.isNotEmpty())
        )


        DateTextField(
            value = form.taskFinalDateString,
            onValueChange = { form = form.copy(taskFinalDateString = it) }, label = "Fecha de fin",
            modifier = Modifier.padding(bottom = 16.dp),
            read = (task == null && form.taskStartDateString.isNotEmpty() &&form.taskDuration.isNotEmpty())
        )

        TimeTextField(
            value = form.taskFinalHour,
            onValueChange = {form = form.copy(taskFinalHour = it) },
            modifier = Modifier.padding(bottom = 16.dp),
            type="finalización",
            read = (task == null && form.taskStartDateString.isNotEmpty() &&form.taskDuration.isNotEmpty())
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
            readOnly = (task == null && form.taskStartDateString.isNotEmpty() &&form.taskFinalDateString.isNotEmpty())
        )
        Text("* Selecciona el tópico:")

        Box {
            Button(onClick = { expandedTopics = true }, modifier = Modifier.fillMaxWidth(0.15f).padding(bottom = 8.dp)) {
                if (form.taskTopic != null) {
                    Text(store.topics().filter { topic -> topic.id==form.taskTopic }.first().name)
                } else {
                    form.taskTopic = store.topics().first().id
                    Text(store.topics().first().name)
                }
            }

            DropdownMenu(
                expanded = expandedTopics,
                onDismissRequest = { expandedTopics = false }
            ) {
                store.topics().forEach { topic ->
                    DropdownMenuItem(
                        text = { Text(topic.name) },
                        onClick = {
                            form.taskTopic = topic.id
                            expandedTopics = false
                        }
                    )
                }
            }
        }
        Text("Selecciona el tag asociado al tópico asociado:")

        Box{
            Button(onClick = { expandedTag = true }, modifier = Modifier.fillMaxWidth(0.15f).padding(bottom = 8.dp)) {
                if (form.taskTags.isNotEmpty()) {
                    Text(store.tags().filter { tag -> form.taskTags.any { tag.id == it }}.first().name)
                } else {
                    Text("Ninguno")
                }
            }

            DropdownMenu(
                expanded = expandedTag,
                onDismissRequest = { expandedTag = false },
                offset = DpOffset(0.dp, 0.dp)
            ) {
                store.tags().filter { tag -> tag.topicId == form.taskTopic} .forEach { tag ->
                    DropdownMenuItem(
                        text = { Text(tag.name) },
                        onClick = {
                            form.taskTags.removeFirst()
                            form.taskTags.add(tag.id)
                            expandedTag = false
                        }
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.padding(bottom = 8.dp)) {

            Checkbox(
                checked = checkedState.value,
                onCheckedChange = { isChecked ->
                    checkedState.value = isChecked
                }
            )
            Text("Tarea periódica")
        }
        if(checkedState.value) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box {
                    Button(onClick = { expand = true }) {
                        Text(selectedPeriod)
                    }

                    DropdownMenu(
                        expanded = expand,
                        onDismissRequest = { expand = false },
                        modifier = Modifier.fillMaxWidth(0.15f)
                    ) {

                        val periods = listOf("Ninguno","Diario", "Semanal","Mensual", "Anual")
                        for (i in 0..4) {
                            DropdownMenuItem(
                                text = { Text(periods[i]) },
                                onClick = {
                                    selectedPeriod = "Periodo seleccionado: ${periods[i]}"
                                    expand = false
                                    form.taskInterval= TaskInterval.entries[i]
                                }
                            )
                        }
                    }
                }
            }
        }
        Button(colors = ButtonDefaults.buttonColors(
            containerColor = Color.Blue,
            contentColor = Color.White,),
            modifier= Modifier.padding(top = 32.dp),
            onClick = {
                try {
                createTask = true
                var m=""
                var time: Time? = null

                if(form.taskName.isEmpty() && createTask){
                    messageError="La tarea debe tener algún nombre"
                    formError=true
                }

                else if (form.taskStartDateString.isEmpty() && form.taskFinalDateString.isEmpty() && createTask) {
                    messageError = "Debes rellenar al menos un campo de fecha"
                    formError=true
                }

                else if(form.taskStartDateString.length == 8 && form.taskDuration.isNotEmpty() && createTask){
                    try {
                        if(form.taskStartHour.isEmpty()){
                            messageError = "La hora de inicio no puede estar vacío"
                            formError=true
                        }

                        form.taskStartDate = createInstant(form.taskStartDateString, form.taskStartHour)
                        m=isValidDate(form.taskStartDate,"inicial")
                        if(!m.isEmpty()){
                            throw IllegalArgumentException(m)
                        }
                        time = TimeFactory().createTime(form.taskStartDate!!,form.taskDuration.toLong())
                    }
                    catch (e: Exception) {
                        messageError = validateDateErrorMessage(e, m)
                        formError=true
                    }
                }

                else if(form.taskFinalDateString.length == 8  && form.taskDuration.isNotEmpty() && createTask) {
                    try{
                        if(form.taskFinalHour.isEmpty()){
                            messageError = "La hora de finalización no puede estar vacío"
                            formError=true
                        }

                        form.taskFinalDate = createInstant(form.taskFinalDateString, form.taskFinalHour)

                        m=isValidDate(form.taskFinalDate,"final")
                        if(!m.isEmpty()){
                            throw IllegalArgumentException(m)
                        }

                        time = TimeFactory().createTime(form.taskDuration.toLong(), form.taskFinalDate!!)

                    } catch (e: Exception) {
                        messageError = validateDateErrorMessage(e, m)
                        formError=true
                    }
                }

                else if(form.taskStartDateString.length==8 && form.taskFinalDateString.length==8 && form.taskDuration.isEmpty() && createTask){
                    try{
                        if(form.taskStartHour.isEmpty() || form.taskFinalHour.isEmpty()){
                            messageError = "La hora final e inicial no puede estar vacío"
                            formError=true
                        }

                        form.taskStartDate = createInstant(form.taskStartDateString, form.taskStartHour)
                        form.taskFinalDate = createInstant(form.taskFinalDateString, form.taskFinalHour)

                        time = TimeFactory().createTime(form.taskStartDate!!, form.taskFinalDate!!)
                        if (!isValidDate(form.taskStartDate, "inicial").isEmpty() || !isValidDate(form.taskFinalDate, "final").isEmpty()) {
                            throw IllegalArgumentException("Fecha final o inicial incorrecta")
                        }
                    } catch (e: Exception) {
                        messageError = validateDateErrorMessage(e, m)
                        formError=true
                    }
                }
                val builder = CommandBuilder(store)
                    .set("priority", form.taskPriority)
                    .set("name", form.taskName)
                    .set("userId","00000000-0000-0000-0000-000026033100")
                    .set("description", form.taskDescription)
                    .set("topicId", form.taskTopic.toString())
                    .set("interval", form.taskInterval.toString())
                    .set("tags", form.taskTags.toString())
                    .set("time", time.toString())
                if (task != null){
                    CommandLauncher.launch(builder.set("id", task.id.toString()).build(CommandType.UPDATE_TASK))
                } else {
                    CommandLauncher.launch(builder.build(CommandType.CREATE_TASK))
                }
                onNavigate(Screen.HOME)
                } catch (e: Throwable) {
                    messageError = e.message ?: "Error inesperado"
                    formError = true
                }
            }) {
            if (task != null) {
                Text("Editar tarea")
            } else {
                Text("Crear tarea")
            }
        }

        if (formError) {
            AlertDialog(
                onDismissRequest = { formError = false },
                title = { Text("Error") },
                text = {
                    messageError?.let { Text(it) }
                },
                confirmButton = {
                    Button(onClick = {
                        formError = false
                        createTask=false
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
            focusedContainerColor = if (read) Color.DarkGray else Color.Unspecified,
            unfocusedContainerColor = if (read) Color.DarkGray else Color.Unspecified,

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
    val day = localDate.dayOfMonth.toString().padStart(2, '0')
    val month = localDate.monthNumber.toString().padStart(2, '0')
    val year = localDate.year
    return "$day$month$year"
}