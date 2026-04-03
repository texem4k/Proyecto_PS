package software.ulpgc.code.architecture.model

import kotlinx.coroutines.delay
import software.ulpgc.code.architecture.io.Store
import kotlin.time.Clock

class TaskMonitor(
    private val store: Store,
    private val checker: PeriodicTaskChecker,
    private val renewer: PeriodicTaskRenewer
) {
    suspend fun startMonitoring() {
        while (true) {
            checkExpiredTasks()
            delay(60_000)
        }
    }

    private fun checkExpiredTasks() {
        store.tasks()
            .filterIsInstance<PeriodicTask>()
            .filter { checker.needsRenewal(it) }
            .forEach { task ->
                checker.markAsDeleted(task)
                renewer.renew(task)
            }
    }
}