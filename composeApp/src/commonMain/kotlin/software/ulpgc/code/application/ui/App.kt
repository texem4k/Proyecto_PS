package software.ulpgc.code.application.ui

import TasksScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.jordond.connectivity.Connectivity
import software.ulpgc.code.application.control.TaskNotifier
import software.ulpgc.code.application.io.cloudDB.SupabaseAuth
import software.ulpgc.code.application.io.cloudDB.SupabaseDBManager
import software.ulpgc.code.application.io.cloudDB.SupabaseProvider
import software.ulpgc.code.application.io.localDB.DatabaseDriverFactory
import software.ulpgc.code.application.io.localDB.JSONParser
import software.ulpgc.code.application.io.localDB.SQLiteDBManager
import software.ulpgc.code.application.io.network.NetworkMonitor
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

val LocalThemeState = compositionLocalOf<ThemeState> { error("No ThemeState provided") }

data class ThemeState(
    val current: AppThemeType,
    val onThemeSelected: (AppThemeType) -> Unit,
    val onThemeClick: () -> Unit
)

@Composable
fun App(
    databaseDriverFactory: DatabaseDriverFactory,
    titleBar: @Composable () -> Unit = {}
) {
    var screen by remember { mutableStateOf(Screen.HOME) }
    var searchText by remember { mutableStateOf("") }
    var filters by remember { mutableStateOf(TaskFilters()) }
    var refreshKey by remember { mutableStateOf(0) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var storeError by remember { mutableStateOf<AppException?>(null) }
    var startEditMode by remember { mutableStateOf(false) }
    var showResults by remember { mutableStateOf(false) }
    var selectedTheme by remember { mutableStateOf(AppThemeType.GREEN) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showCreateGroup by remember { mutableStateOf(false) }
    var isAuthenticated by remember { mutableStateOf(true) }


    val themeState = ThemeState(
        current = selectedTheme,
        onThemeSelected = { selectedTheme = it },
        onThemeClick = { showThemeDialog = true }
    )

    LaunchedEffect(Unit) {
        val seedData = JSONParser().loadDBData("composeResources/dbDefaults.json")
        SQLiteDBManager.initialize(databaseDriverFactory, seedData)
        Store.initialize(SQLiteDBManager, { error -> storeError = error }, {
            TaskNotifier.initialize()
            TaskMonitor.initialize()
            TaskOptimizer.initialize()
            SupabaseProvider.initialize()
        }, SupabaseDBManager,
            {
                NetworkMonitor.hasConnection.value &&
                        SupabaseAuth.ready.value && SupabaseAuth.isLoggedIn() &&
                        SupabaseDBManager.ready.value
            })
    }

    val storeReady = Store.ready.collectAsState().value

    CompositionLocalProvider(
        LocalThemeState provides themeState
    ) {
        AppTheme(theme = selectedTheme) {
            Column(modifier = Modifier.fillMaxSize()) {

                titleBar()

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .safeContentPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (showThemeDialog) {
                        ThemeDialog(
                            current = selectedTheme,
                            onThemeSelected = { selectedTheme = it },
                            onDismiss = { showThemeDialog = false }
                        )
                    }

                    storeError?.let { error -> StoreErrorDisplay(error) }

                    if (storeReady) {
                        when (screen) {
                            Screen.HOME -> HomeScreen(
                                onNavigate = { screen = it },
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
                                searchText,
                                onSearchTextChange = { searchText = it },
                                filters,
                                onEdit = { task ->
                                    taskToEdit = task
                                    startEditMode = true
                                    screen = Screen.TASKS
                                },
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
                                searchText,
                                onSearchTextChange = { searchText = it },
                                filters,
                                onEdit = { task -> taskToEdit = task },
                                onDeleted = { refreshKey++ },
                                autoOpen = AutoOpen.TASK,
                                onShowResults = { showResults = it }
                            )

                            Screen.TOPIC_CREATE -> TasksScreen(
                                onNavigate = { screen = it },
                                searchText,
                                onSearchTextChange = { searchText = it },
                                filters,
                                onEdit = { task -> taskToEdit = task },
                                onDeleted = { refreshKey++ },
                                autoOpen = AutoOpen.TOPIC,
                                onShowResults = { showResults = it }
                            )

                            Screen.GROUP_CREATE -> {
                                screen = Screen.HOME
                                showCreateGroup = true
                            }

                            Screen.TAG_CREATE -> TasksScreen(
                                onNavigate = { screen = it },
                                searchText,
                                onSearchTextChange = { searchText = it },
                                filters,
                                onEdit = { task -> taskToEdit = task },
                                onDeleted = { refreshKey++ },
                                autoOpen = AutoOpen.TAG,
                                onShowResults = { showResults = it }
                            )

                            Screen.DASHBOARD -> DashboardScreen(
                                onNavigate = { screen = it },
                                onSettingsClick = { showThemeDialog = true },
                            )

                            Screen.CALENDAR -> CalendarScreen(
                                onNavigate = { screen = it },
                                onSettingsClick = { showThemeDialog = true },
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
                    value = searchText,
                    onSearchTextChange = { searchText = it },
                    filters = filters
                )
            }

            if (storeReady && showCreateGroup) {
                CreateGroup(
                    onClose = { showCreateGroup = false },
                    onSubmit = { showCreateGroup = false }
                )
            }
        }
    }
}

@Composable
fun StoreErrorDisplay(exception: AppException) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Error") },
        text = { Text(exception.message ?: "Ha ocurrido un error inesperado") },
        confirmButton = {
            Button(onClick = {}) { Text("Aceptar") }
        }
    )
}