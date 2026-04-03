package software.ulpgc.code.architecture.model.tasks

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.io.Store
import software.ulpgc.code.architecture.io.isDeleted
import kotlin.time.Clock

class TaskMonitor(
    private val store: Store,
) {
    private val monitorScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    init {
        startMonitoring()
    }

    private fun startMonitoring() {
        monitorScope.launch {
            while (isActive) {
                checkExpiredTasks()
                delay(60_000)
            }
        }
    }

    private fun checkExpiredTasks() {
        store.tasks()
            .filter { needsRenewal(it) }
            .forEach { task ->
                task.dbState = DBState.DELETED
                renew(task)
            }
    }

    private fun renew(task: Task) {
        task.time.start = task.interval + task.time.start
        task.time.end = task.interval + task.time.end
        task.dbState = DBState.UPDATED
    }

    private fun needsRenewal(task: Task): Boolean = !task.isDeleted() &&
            task.interval != TaskInterval.NONE &&
            task.time.end <= Clock.System.now()

    fun dispose() {
        monitorScope.cancel()
    }
}