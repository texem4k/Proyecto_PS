package software.ulpgc.code.application.ui

import TasksScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import software.ulpgc.code.application.control.TaskNotifier
import software.ulpgc.code.application.io.DatabaseDriverFactory
import software.ulpgc.code.application.io.JSONParser
import software.ulpgc.code.application.io.SQLiteDBManager
import software.ulpgc.code.application.ui.filters.TaskFilters
import software.ulpgc.code.application.ui.pages.CalendarScreen
import software.ulpgc.code.application.ui.pages.DashboardScreen
import software.ulpgc.code.application.ui.pages.HomeScreen
import software.ulpgc.code.application.ui.pages.SearchResultsDialog
import software.ulpgc.code.architecture.control.exceptions.AppException
import software.ulpgc.code.architecture.control.optimizer.TaskOptimizer
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
    var startEditMode by remember { mutableStateOf(false) }
    var showResults by remember { mutableStateOf(false) }
    var selectedTheme by remember { mutableStateOf(AppThemeType.GREEN) }
    var showThemeDialog by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        val seedData = JSONParser().loadDBData("composeResources/dbDefaults.json")
        store = Store(SQLiteDBManager(databaseDriverFactory, seedData), { error -> storeError = error }, { store ->
            TaskNotifier.setUpWith(store)
            TaskMonitor(store)
            TaskOptimizer.setUpWith(store)
        })
    }

    val storeReady = store?.ready?.collectAsState()?.value ?: false

    AppTheme(theme = selectedTheme) {
        if (showThemeDialog) {
            ThemeDialog(
                current = selectedTheme,
                onThemeSelected = {
                    selectedTheme = it
                },
                onDismiss = {
                    showThemeDialog = false
                }
            )
        }
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
                        Screen.HOME -> HomeScreen(
                            onNavigate = { screen = it },
                            store!!,
                            searchText,
                            onSearchTextChange = { searchText = it },
                            onEdit = { task ->
                                taskToEdit = task
                                startEditMode = true
                                screen = Screen.TASKS
                            },
                            onDeleted = { refreshKey++ },
                            onSearch = {
                                filters.hasFilter = false
                                showResults = true
                            },
                            onSettingsClick = { showThemeDialog = true }
                        )

                        Screen.TASKS -> TasksScreen(
                            onNavigate = { screen = it },
                            store!!,
                            searchText,
                            onSearchTextChange = { searchText = it },
                            filters,
                            onEdit = { task ->
                                taskToEdit = task
                                startEditMode = true
                                screen = Screen.TASKS
                            } ,
                            onDeleted = { refreshKey++ },
                            onCreated = { refreshKey++ },
                            taskToEdit = if (startEditMode) taskToEdit else null,
                            onEditDone = {
                                startEditMode = false
                                taskToEdit = null
                            },
                            onShowResults = { showResults = it },
                            onSettingsClick = { showThemeDialog = true }
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
                            autoOpen = AutoOpen.TASK,
                            onShowResults = { showResults = it }
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
                            autoOpen = AutoOpen.TOPIC,
                            onShowResults = { showResults = it }
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
                            autoOpen = AutoOpen.TAG,
                            onShowResults = { showResults = it }
                        )

                        Screen.DASHBOARD -> DashboardScreen(
                            onNavigate = { screen = it },
                            store!!,
                            onSettingsClick = { showThemeDialog = true }
                        )

                        Screen.CALENDAR -> CalendarScreen(
                            onNavigate = { screen = it },
                            store!!,
                            onSettingsClick = { showThemeDialog = true }
                        )

                        else -> {}
                    }
                }
            }
        }

        if (storeReady && showResults) {
            SearchResultsDialog(
                onDismiss = {
                    showResults = false
                    filters.hasFilter = false
                },
                store = store!!,
                value = searchText,
                onSearchTextChange = { searchText = it },
                filters = filters
            )
        }
    }
}

@Composable
fun StoreErrorDisplay(exception: AppException) {
    AlertDialog(
        onDismissRequest = {},
        title = {
            Text("Error")
        },
        text = {
            Text(
                exception.message ?: "Ha ocurrido un error inesperado"
            )
        },
        confirmButton = {
            Button(onClick = {}) {
                Text("Aceptar")
            }
        }
    )
}