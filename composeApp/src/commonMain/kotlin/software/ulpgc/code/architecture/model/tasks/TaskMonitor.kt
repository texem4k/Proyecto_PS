package software.ulpgc.code.architecture.model.tasks

import software.ulpgc.code.architecture.control.coroutines.Coroutine
import software.ulpgc.code.architecture.control.coroutines.CoroutineManager
import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.io.Store
import software.ulpgc.code.architecture.io.isDeleted
import kotlin.time.Clock

class TaskMonitor(
    private val store: Store,
) : Coroutine {

    init {
        CoroutineManager.add(this)
    }

    private fun renew(task: Task) {
        task.time.start = task.interval + task.time.start
        task.time.end = task.interval + task.time.end
        task.dbState = DBState.UPDATED
    }

    private fun needsRenewal(task: Task): Boolean = !task.isDeleted() &&
            task.interval != TaskInterval.NONE &&
            task.time.end <= Clock.System.now()

    override val delayMilis: Long = 60_000L

    override suspend fun onInit() {
        execute()
    }

    override suspend fun execute() {
        store.tasks()
            .filter { needsRenewal(it) }
            .forEach { task ->
                task.dbState = DBState.DELETED
                renew(task)
            }
    }

    override suspend fun onDispose() {
        execute()
    }
}