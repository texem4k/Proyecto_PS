package software.ulpgc.code.architecture.control.commands


import software.ulpgc.code.architecture.control.logs.LogMaster
import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.model.Priority
import software.ulpgc.code.architecture.model.tasks.Task
import software.ulpgc.code.architecture.model.tasks.TaskInterval
import software.ulpgc.code.architecture.model.times.Time
import kotlin.uuid.Uuid

class UpdateTaskCommand internal constructor (private val currentTask: Task, private val newTask: Task): Command {

    constructor(currentTask: Task, priority: Priority, name: String, description: String, time: Time,
                interval: TaskInterval, tags: MutableSet<Uuid>, users: MutableSet<Uuid>) :
            this(
                currentTask,
                Task(currentTask.topicId, name, description, time, interval, priority, tags, users, currentTask.isCompleted , currentTask.id)
            )

    override fun execute(): List<Command> {
        LogMaster.log("UpdateTaskCommand {from=$currentTask to=$newTask}")
        val currentClone = currentTask.copy()
        currentTask.priority = newTask.priority
        currentTask.name = newTask.name
        currentTask.description = newTask.description
        currentTask.time = newTask.time
        currentTask.interval = newTask.interval
        currentTask.tags = newTask.tags
        currentTask.users = newTask.users
        currentTask.isCompleted = newTask.isCompleted
        currentTask.localDBState = DBState.UPDATED
        currentTask.cloudDBState = DBState.UPDATED
        return listOf(UpdateTaskCommand(currentTask, currentClone))
    }
}