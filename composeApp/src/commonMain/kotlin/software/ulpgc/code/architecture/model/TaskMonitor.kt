package software.ulpgc.code.architecture.model

import kotlinx.coroutines.*
import kotlinx.coroutines.delay
import software.ulpgc.code.architecture.io.Store

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
            .filterIsInstance<PeriodicTask>()
            .filter { PeriodicTaskChecker.needsRenewal(it) }
            .forEach { task ->
                PeriodicTaskChecker.markAsDeleted(task)
                PeriodicTaskRenewer.renew(task)
            }
    }

    fun dispose() {
        monitorScope.cancel()
    }
}