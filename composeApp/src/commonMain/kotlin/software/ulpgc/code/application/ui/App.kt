package software.ulpgc.code.application.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import software.ulpgc.code.application.control.TaskNotifier
import software.ulpgc.code.application.io.DatabaseDriverFactory
import software.ulpgc.code.application.io.JSONParser
import software.ulpgc.code.application.io.SQLiteDBManager
import software.ulpgc.code.application.ui.filters.TaskFilters
import software.ulpgc.code.application.ui.pages.CreateTaskScreen
import software.ulpgc.code.application.ui.pages.DeleteTaskScreen
import software.ulpgc.code.application.ui.pages.HomeScreen
import software.ulpgc.code.application.ui.pages.SearchTaskScreen
import software.ulpgc.code.architecture.control.exceptions.AppException
import software.ulpgc.code.architecture.io.Store
import software.ulpgc.code.architecture.model.tasks.Task
import software.ulpgc.code.architecture.model.tasks.TaskMonitor

@Composable
fun App(
    databaseDriverFactory: DatabaseDriverFactory
) {
    var screen by remember { mutableStateOf(Screen.HOME) }
    var searchText by remember { mutableStateOf("") }
    var filters by remember { mutableStateOf(TaskFilters()) }
    var refreshKey by remember { mutableStateOf(0) }
    var store by remember { mutableStateOf<Store?>(null) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var storeError by remember { mutableStateOf<AppException?>(null) }

    LaunchedEffect(Unit) {
        val seedData = JSONParser().loadDBData("composeResources/dbDefaults.json")
        store = Store(SQLiteDBManager(databaseDriverFactory, seedData), { error -> storeError = error }, { store ->
            TaskNotifier.setUpWith(store)
            TaskMonitor(store)
        })
    }

    val storeReady = store?.ready?.collectAsState()?.value ?: false

    MaterialTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            storeError?.let { error -> StoreErrorDisplay(error) }
            if (storeReady) {
                key(refreshKey){
                    when (screen) {
                        Screen.HOME -> {
                            HomeScreen(
                                onNavigate = { screen = it },
                                store!!,
                                searchText,
                                onSearchTextChange = { searchText = it },
                                filters,
                                onEdit = { task ->
                                    taskToEdit = task
                                    screen = Screen.CREATE_TASK
                                },
                                onDeleted = { refreshKey++}
                            )
                        }

                        Screen.CREATE_TASK -> CreateTaskScreen(
                            onNavigate = { newScreen ->
                                screen = newScreen
                                if (newScreen == Screen.HOME) {
                                    taskToEdit = null
                                }
                            },
                            store!!,
                            task = taskToEdit
                        )

                        Screen.DELETE_TASK -> DeleteTaskScreen(
                            onNavigate = { screen = it }, store!!, onDeleted = { refreshKey++}
                        )

                        Screen.RESULTS -> SearchTaskScreen(
                            onNavigate = { screen = it },
                            store!!,
                            searchText,
                            onSearchTextChange = { searchText = it },
                            filters
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StoreErrorDisplay(exception: AppException) {

}