package software.ulpgc.code.application.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import software.ulpgc.code.application.ui.DateTextField
import software.ulpgc.code.application.ui.Screen
import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.Tag

data class FormState(
    val taskName: String = "",
    val taskDescription: String = "",
    val taskTopic: String = "",
    val taskTag: String = "",
    var taskStartDateString: String = "",
    var taskStartDate: LocalDate? = null,
    var taskFinalDateString: String = "",
    var taskFinalDate: LocalDate? = null,
    val taskDuration: String = "",
)


@Composable
fun CreateTaskScreen(onNavigate: (Screen) -> Unit, store: Storage) {

    var expandedTopics by remember { mutableStateOf(false) }
    var expandedTag by remember { mutableStateOf(false) }
    var form by remember { mutableStateOf(FormState()) }
    var selectedTopic by remember { mutableStateOf(store.topics().first()) }
    var selectedTag by remember { mutableStateOf<Tag?>(null) }

    var createTask by remember { mutableStateOf(false) }
    var dateError by remember { mutableStateOf(false) }
    var messageError: String? by remember { mutableStateOf("") }

    val checkedState = remember { mutableStateOf(false) }
    var expand by remember { mutableStateOf(false) }
    var selectedPeriod by remember { mutableStateOf("Periodo") }



    val scrollState = rememberScrollState()


    Row (modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Text("Creación de tarea",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp).align(Alignment.Center)
            )
            Button(
                modifier = Modifier.align(Alignment.CenterEnd),
                onClick = { onNavigate(Screen.HOME) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            ) {
                Text("✖\uFE0E")
            }
        }
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
            placeholder = { Text("") },
            label = { Text("Descripción") },
            modifier = Modifier.padding(bottom = 16.dp)
        )

        DateTextField(
            value = form.taskStartDateString,
            onValueChange = { form = form.copy(taskStartDateString = it) }, label = "Fecha de inicio",
            modifier = Modifier.padding(bottom = 16.dp)
        )


        DateTextField(
            value = form.taskFinalDateString,
            onValueChange = { form = form.copy(taskFinalDateString = it) }, label = "Fecha de fin",
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.padding(bottom = 32.dp)) {

            Checkbox(
                checked = checkedState.value,
                onCheckedChange = { isChecked ->
                    checkedState.value = isChecked  //Activó las tareas periódicas
                }
            )
            Text("Tarea periódica")
        }
        if(checkedState.value) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box() {
                    Button(onClick = { expand = true }) {
                        Text(selectedPeriod)
                    }

                    DropdownMenu(
                        expanded = expand,
                        onDismissRequest = { expand = false }
                    ) {
                        val periods = listOf("Diario", "Semanal", "Quincenal","Mensual","Semestral", "Anual")
                        for (x in periods) {
                            DropdownMenuItem(
                                text = { Text(x) },
                                onClick = {
                                    selectedPeriod = "Periodo seleccionado: $x"
                                    expand = false
                                }
                            )
                        }
                    }
                }
            }
        }


        TextField(
            value = form.taskDuration,
            onValueChange = { form = form.copy(taskDuration = it) },
            placeholder = {Text("")},
            modifier = Modifier.padding(bottom = 16.dp,top = 16.dp),
            label = {Text("Duración de la tarea (en horas)")},
        )

        Text("* Selecciona el tópico:")

        Box() {
            Button(onClick = { expandedTopics = true }, modifier = Modifier.fillMaxWidth(0.15f).padding(bottom = 8.dp)) {
                Text(selectedTopic.name)
            }


            DropdownMenu(
                expanded = expandedTopics,
                onDismissRequest = { expandedTopics = false }
            ) {
                store.topics().forEach { topic ->
                    DropdownMenuItem(
                        text = { Text(topic.name) },
                        onClick = {
                            selectedTopic = topic
                            expandedTopics = false
                        }
                    )
                }
            }
        }
        Text("Selecciona el tag asociado al tópico asociado:")


        Box(){
            Button(onClick = { expandedTag = true }, modifier = Modifier.fillMaxWidth(0.15f).padding(bottom = 8.dp)) {
                if (selectedTag != null) {
                    Text(selectedTag!!.name)
                } else {
                    Text("Ninguno")
                }
            }

            DropdownMenu(
                expanded = expandedTag,
                onDismissRequest = { expandedTag = false },
                offset = DpOffset(0.dp, 0.dp)
            ) {
                store.tags().filter { tag -> tag.topicId == selectedTopic.id} .forEach { tag ->
                    DropdownMenuItem(
                        text = { Text(tag.name) },
                        onClick = {
                            selectedTag = tag
                            expandedTag = false
                        }
                    )
                }
            }
        }





        Button(colors = ButtonDefaults.buttonColors(
            containerColor = Color.Blue,
            contentColor = Color.White,

        ),
            modifier= Modifier.padding(top = 32.dp),
            onClick = {
                createTask = true
                var m=""
                if(form.taskName.isEmpty() && createTask){
                    messageError="La tarea debe tener algún nombre"
                    dateError=true
                }

                else if (form.taskStartDateString.isEmpty() && form.taskFinalDateString.isEmpty() && createTask) {
                    messageError = "Debes rellenar al menos un campo de fecha"
                    dateError=true
                }
                else if(form.taskStartDateString.length == 8 && createTask){

                    try {

                        form.taskStartDate = LocalDate(
                            form.taskStartDateString.substring(4, 8).toInt(),
                            form.taskStartDateString.substring(2, 4).toInt(),
                            form.taskStartDateString.take(2).toInt()
                        )
                        m=isValidDate(form.taskStartDate,"inicial")
                        println(m)
                        if(!m.isEmpty()){
                            throw IllegalArgumentException(m)
                        }
                    }
                    catch (e: Exception) {
                        println(e)
                        if(e.toString().contains("Argument") && e.message.toString() == m) {
                            messageError = m
                        }
                        else{
                            messageError = "Los valores de día y mes deben ser correctos (0-31/1-12)"
                        }
                        dateError=true
                    }
                }
                else if(form.taskFinalDateString.length == 8 && createTask) {
                    try{
                        form.taskFinalDate = LocalDate(
                            form.taskFinalDateString.substring(4, 8).toInt(),
                            form.taskFinalDateString.substring(2, 4).toInt(),
                            form.taskFinalDateString.take(2).toInt())
                        m=isValidDate(form.taskFinalDate,"final")
                        if(!m.isEmpty()){
                            throw IllegalArgumentException(m)
                        }


                    } catch (e: Exception) {
                        if(e.toString().contains("Argument") && e.message.toString() == m) {
                            messageError = m
                        }
                        else{
                            messageError = "Los valores de día y mes deben ser correctos (0-31/1-12)"
                        }
                        dateError=true
                    }

                }
            }){
            Text("Crear tarea")
            }

        if (dateError) {
            AlertDialog(
                onDismissRequest = { dateError = false },
                title = { Text("Error") },
                text = {
                    messageError?.let { Text(it) }
                },
                confirmButton = {
                    Button(onClick = {
                        dateError = false
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


fun isValidDate(date: LocalDate?, type: String):String {
    if (date != null) {
        if(date< Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date){
            return "La fecha $type no puede ser anterior a la fecha actual"
        }
    }
    return ""
}