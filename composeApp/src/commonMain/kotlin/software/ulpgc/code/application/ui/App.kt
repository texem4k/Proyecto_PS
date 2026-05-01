package software.ulpgc.code.application.ui

import TasksScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import software.ulpgc.code.application.io.DatabaseDriverFactory
import software.ulpgc.code.application.io.JSONParser
import software.ulpgc.code.application.io.SQLiteDBManager
import software.ulpgc.code.application.ui.filters.TaskFilters
import software.ulpgc.code.application.ui.pages.DashboardScreen
import software.ulpgc.code.application.ui.pages.HomeScreen
import software.ulpgc.code.application.ui.pages.SearchResultsDialog
import software.ulpgc.code.architecture.io.Store
import software.ulpgc.code.architecture.model.tasks.Task

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

    LaunchedEffect(Unit) {
        val seedData = JSONParser().loadDBData("composeResources/dbDefaults.json")
        store = Store(SQLiteDBManager(databaseDriverFactory, seedData))
    }

    val storeReady = store?.ready?.collectAsState()?.value ?: false

    MaterialTheme {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (storeReady) {
                key(refreshKey){
                    when (screen) {
                        Screen.HOME -> HomeScreen(
                            onNavigate = { screen = it },
                            store!!,
                            searchText,
                            onSearchTextChange = { searchText = it },
                            onDeleted = { refreshKey++ }
                        )

                        Screen.RESULTS -> SearchResultsDialog(
                            onNavigate = { screen = it },
                            store=store!!,
                            value=searchText,
                            onSearchTextChange = { searchText = it },
                            filters=filters,
                        )

                        Screen.TASKS -> TasksScreen(
                            onNavigate = { screen = it },
                            store!!,
                            searchText,
                            onSearchTextChange = { searchText = it },
                            filters,
                            onEdit = { task ->
                                taskToEdit = task
                            },
                            onCreated = { refreshKey++ },
                            onDeleted = { refreshKey++ }
                        )

                        Screen.TASKS_CREATE -> TasksScreen(
                            onNavigate = { screen = it },
                            store!!,
                            searchText,
                            onSearchTextChange = { searchText = it },
                            filters,
                            onEdit = { task ->
                                taskToEdit = task
                            },
                            onDeleted = { refreshKey++ },
                            autoOpen = AutoOpen.TASK
                        )

                        Screen.TOPIC_CREATE -> TasksScreen(
                            onNavigate = { screen = it },
                            store!!,
                            searchText,
                            onSearchTextChange = { searchText = it },
                            filters,
                            onEdit = { task ->
                                taskToEdit = task
                            },
                            onDeleted = { refreshKey++ },
                            autoOpen = AutoOpen.TOPIC
                        )

                        Screen.TAG_CREATE -> TasksScreen(
                            onNavigate = { screen = it },
                            store!!,
                            searchText,
                            onSearchTextChange = { searchText = it },
                            filters,
                            onEdit = { task ->
                                taskToEdit = task
                            },
                            onDeleted = { refreshKey++ },
                            autoOpen = AutoOpen.TAG
                        )

                        Screen.DASHBOARD -> DashboardScreen(
                            onNavigate = { screen = it },
                            store!!,
                            searchText,
                            onSearchTextChange = { searchText = it },
                        )

                        else -> {}
                    }
                }
            }
        }
    }
}