package software.ulpgc.code.architecture.model.tasks

import com.mmk.kmpnotifier.notification.Notifier
import com.mmk.kmpnotifier.notification.NotifierManager
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import software.ulpgc.code.architecture.control.coroutines.Coroutinable
import software.ulpgc.code.architecture.control.coroutines.CoroutineManager
import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.io.Store
import software.ulpgc.code.architecture.io.isDeleted
import kotlin.time.Clock

object TaskMonitor: Coroutinable {

    private lateinit var notifier: Notifier

    fun initialize() {
        notifier = NotifierManager.getLocalNotifier()
        CoroutineManager.add(this)
    }

    private fun renew(task: Task) {
        task.time.start = task.interval + task.time.start
        task.time.end = task.interval + task.time.end
        task.isCompleted = false
        task.dbState = DBState.UPDATED
        Store.addCompletionStat(CompletionStat(task.id, false, task.time.end))
    }

    private fun sendNotification(task: Task) {
        notifier.notify(
            "Tarea renovada",
            "La tarea ${task.name} se ha renovado para la fecha " +
                    "${task.time.start.toLocalDateTime(TimeZone.currentSystemDefault())}."
        )
    }

    private fun needsRenewal(task: Task): Boolean =
        !task.isDeleted() &&
        task.interval != TaskInterval.NONE &&
        task.time.end <= Clock.System.now()

    override val delayMilis: Long = 15_000L

    override suspend fun onInit() {
        execute()
    }

    override suspend fun execute() {
        Store.tasks()
            .filter { needsRenewal(it) }
            .forEach {
                while(needsRenewal(it)) {
                    renew(it)
                }
                sendNotification(it)
            }
    }

    override suspend fun onDispose() {
        execute()
    }
}