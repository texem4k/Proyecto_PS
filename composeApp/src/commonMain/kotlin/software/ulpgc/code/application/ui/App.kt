package software.ulpgc.code.application.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import software.ulpgc.code.application.ui.pages.CreateTaskScreen
import software.ulpgc.code.application.ui.pages.DeleteTaskScreen
import software.ulpgc.code.application.ui.pages.HomeScreen
import software.ulpgc.code.architecture.model.EndBasedInterval
import software.ulpgc.code.architecture.model.TaskFactory
import software.ulpgc.code.architecture.model.Topic
import kotlin.time.Clock
import kotlin.uuid.Uuid

val userId = Uuid.parse("00000000-0000-0000-0000-000000000001")
val now = Clock.System.now()

val topics = listOf(
    Topic(name = "Estudios",  color = 0, id = Uuid.random()),
    Topic(name = "Proyectos", color = 0, id = Uuid.random()),
    Topic(name = "Topico1",   color = 0, id = Uuid.random()),
    Topic(name = "Topico2",   color = 0, id = Uuid.random())
)

val factory = TaskFactory()
val tareas = listOf(
    factory.createTask(priority = 1,  name = "Estudiar PS",       userId = userId, description = "asasas", topicId = topics[0].id, time = EndBasedInterval(id = Uuid.random(), start = now, end = now, taskId = Uuid.random())),
    factory.createTask(priority = 1,  name = "Hacer proyec PS",   userId = userId, description = "asasas", topicId = topics[1].id, time = EndBasedInterval(id = Uuid.random(), start = now, end = now, taskId = Uuid.random())),
    factory.createTask(priority = 1,  name = "Hacer proyecto PS", userId = userId, description = "asasas", topicId = topics[1].id, time = EndBasedInterval(id = Uuid.random(), start = now, end = now, taskId = Uuid.random())),
    factory.createTask(priority = 1,  name = "Ejemplo1",          userId = userId, description = "asasas", topicId = topics[2].id, time = EndBasedInterval(id = Uuid.random(), start = now, end = now, taskId = Uuid.random())),
    factory.createTask(priority = 1,  name = "Ejemplo2",          userId = userId, description = "asasas", topicId = topics[2].id, time = EndBasedInterval(id = Uuid.random(), start = now, end = now, taskId = Uuid.random())),
    factory.createTask(priority = 1,  name = "Ejemplo3",          userId = userId, description = "asasas", topicId = topics[2].id, time = EndBasedInterval(id = Uuid.random(), start = now, end = now, taskId = Uuid.random())),
    factory.createTask(priority = 1,  name = "Ejemplo4",          userId = userId, description = "asasas", topicId = topics[2].id, time = EndBasedInterval(id = Uuid.random(), start = now, end = now, taskId = Uuid.random())),
    factory.createTask(priority = 1,  name = "Ejemplo5",          userId = userId, description = "asasas", topicId = topics[2].id, time = EndBasedInterval(id = Uuid.random(), start = now, end = now, taskId = Uuid.random())),
    factory.createTask(priority = 1,  name = "Ejemplo6",          userId = userId, description = "asasas", topicId = topics[3].id, time = EndBasedInterval(id = Uuid.random(), start = now, end = now, taskId = Uuid.random())),
    factory.createTask(priority = 10, name = "Ejemplo7",          userId = userId, description = "asas",   topicId = topics[3].id, time = EndBasedInterval(id = Uuid.random(), start = now, end = now, taskId = Uuid.random())),
    factory.createTask(priority = 10, name = "Ejemplo8",          userId = userId, description = "asas",   topicId = topics[3].id, time = EndBasedInterval(id = Uuid.random(), start = now, end = now, taskId = Uuid.random())),
    factory.createTask(priority = 10, name = "Ejemplo9",          userId = userId, description = "asas",   topicId = topics[3].id, time = EndBasedInterval(id = Uuid.random(), start = now, end = now, taskId = Uuid.random())),
    factory.createTask(priority = 10, name = "Ejemplo10",         userId = userId, description = "asas",   topicId = topics[3].id, time = EndBasedInterval(id = Uuid.random(), start = now, end = now, taskId = Uuid.random())),
    factory.createTask(priority = 10, name = "Ejemplo11",         userId = userId, description = "asas",   topicId = topics[3].id, time = EndBasedInterval(id = Uuid.random(), start = now, end = now, taskId = Uuid.random())),
)

@Composable
@Preview
fun App() {
    var screen by remember { mutableStateOf(Screen.HOME) }
    var searchText by remember { mutableStateOf("") }


    MaterialTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (screen) {
                Screen.HOME -> {
                    HomeScreen(
                        onNavigate = { screen = it },
                        tareas, topics,
                        searchText,
                        onSearchTextChange = { searchText = it },
                    )
                }
                Screen.CREATE_TASK -> CreateTaskScreen(
                    onNavigate = { screen = it },
                )
                Screen.DELETE_TASK -> DeleteTaskScreen(
                    onNavigate = { screen = it }, tareas
                )
                Screen.RESULTS -> SearchTaskScreen(
                    onNavigate = { screen = it }, tareas, searchText, onSearchTextChange = { searchText = it }, filters
                )

            }
        }
    }
}