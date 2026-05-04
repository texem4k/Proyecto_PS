package software.ulpgc.code.application.control

import com.mmk.kmpnotifier.notification.NotifierManager
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import software.ulpgc.code.architecture.control.coroutines.Coroutinable
import software.ulpgc.code.architecture.control.coroutines.CoroutineManager
import software.ulpgc.code.architecture.control.logs.LogMaster
import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.tasks.Task
import kotlin.time.Clock
import kotlin.uuid.Uuid

object TaskNotifier : Coroutinable {
    private enum class NotificationState {
        REMINDED, IMPORTANT, FINISHED
    }
    private var store: Storage? = null
    private val notifier = NotifierManager.getLocalNotifier()
    private val notificationStates: MutableMap<Uuid, NotificationState> = mutableMapOf()
    private const val REMINDING_FACTOR = 3

    fun setUpWith(store: Storage) {
        this.store = store
        CoroutineManager.add(this)
    }

    private fun notify(task: Task, state: NotificationState) {
        LogMaster.log("Notificando ${task.name} como ${state.name}")
        when (state) {
            NotificationState.REMINDED -> notifier.notify(
                "Recordatorio de tarea",
                "La tarea ${task.name} deberías empezarla antes de " +
                        "${task.time.start.toLocalDateTime(TimeZone.currentSystemDefault())}."
            )
            NotificationState.IMPORTANT -> notifier.notify(
                "Tarea importante",
                "Deberías empezar ya la tarea ${task.name} para poder terminarla antes de " +
                        "${task.time.end.toLocalDateTime(TimeZone.currentSystemDefault())}."
            )
            NotificationState.FINISHED -> notifier.notify(
                "Tarea terminada",
                "La tarea ${task.name} a llegado a su fecha final. Recuerda marcarla como terminada."
            )
        }
        notificationStates[task.id] = state
    }

    private fun hasFinished(task: Task): Boolean {
        return task.time.end <= Clock.System.now()
    }


    private fun isImportant(task: Task): Boolean {
        return task.time.start <= Clock.System.now()
    }


    private fun shouldRemind(task: Task): Boolean {
        return task.time.start.minus(task.time.duration()*REMINDING_FACTOR) <= Clock.System.now()
    }


    override val delayMilis: Long = 30_000L

    override suspend fun onInit() {
        execute()
    }

    override suspend fun execute() {
        store?.tasks()?.filterNot(Task::isCompleted)?.forEach { task ->
            if (hasFinished(task)) {
                if (notificationStates[task.id] == NotificationState.FINISHED) return
                notify(task, NotificationState.FINISHED)
            } else if (isImportant(task)) {
                if (notificationStates[task.id] == NotificationState.IMPORTANT) return
                notify(task, NotificationState.IMPORTANT)
            } else if (shouldRemind(task)) {
                if (notificationStates[task.id] == NotificationState.REMINDED) return
                notify(task, NotificationState.REMINDED)
            }
        }
    }

    override suspend fun onDispose() {
        notifier.removeAll()
    }
}

