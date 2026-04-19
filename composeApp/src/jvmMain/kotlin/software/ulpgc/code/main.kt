package software.ulpgc.code

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.asCoroutineDispatcher
import software.ulpgc.code.application.ui.App
import software.ulpgc.code.architecture.io.Store
import java.util.concurrent.Executors

fun main() = application {
    val dbThread = Executors.newSingleThreadExecutor { r ->
        Thread(r, "db-thread").also { it.isDaemon = true }
    }
    val dbDispatcher = dbThread.asCoroutineDispatcher()
    var store: Store? = null

    Window(
        onCloseRequest = {
            store?.dispose()
            exitApplication()
            dbThread.shutdown()
        },
        title = "Proyecto_PS",
    ) {
        App(
            JavaDatabaseDriverFactory(),
            dbDispatcher,
            onStoreCreated = { store = it }
        )
    }
}