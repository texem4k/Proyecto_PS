package software.ulpgc.code.application.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import software.ulpgc.code.application.io.DatabaseDriverFactory
import software.ulpgc.code.application.io.JSONParser
import software.ulpgc.code.application.io.SQLiteDBManager
import software.ulpgc.code.application.ui.filters.TaskFilters
import software.ulpgc.code.application.ui.pages.CreateTaskScreen
import software.ulpgc.code.application.ui.pages.DeleteTaskScreen
import software.ulpgc.code.application.ui.pages.HomeScreen
import software.ulpgc.code.application.ui.pages.SearchTaskScreen
import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.io.Store


@Composable
fun App(databaseDriverFactory: DatabaseDriverFactory) {
    var screen by remember { mutableStateOf(Screen.HOME) }
    var searchText by remember { mutableStateOf("") }
    var filters by remember { mutableStateOf(TaskFilters()) }

    var store by remember { mutableStateOf<Storage?>(null) }

    LaunchedEffect(Unit) {
        val seedData = JSONParser().loadDBData("dbDefaults.json")
        store = Store(SQLiteDBManager(databaseDriverFactory, seedData))
    }

    if (store == null) {
        TODO()
    } else {

    }

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
                        store,
                        searchText,
                        onSearchTextChange = { searchText = it },
                        filters
                    )
                }
                Screen.CREATE_TASK -> CreateTaskScreen(
                    onNavigate = { screen = it },
                )
                Screen.DELETE_TASK -> DeleteTaskScreen(
                    onNavigate = { screen = it }, store
                )
                Screen.RESULTS -> SearchTaskScreen(
                    onNavigate = { screen = it }, store, searchText, onSearchTextChange = { searchText = it }, filters
                )

            }
        }
    }
}