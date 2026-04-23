package software.ulpgc.code.architecture.control.commands


import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.model.tasks.Task
import software.ulpgc.code.architecture.model.tasks.TaskInterval
import software.ulpgc.code.architecture.model.times.Time
import kotlin.uuid.Uuid

class UpdateTaskCommand internal constructor (private val currentTask: Task, private val newTask: Task): Command {

    constructor(currentTask: Task, priority: Int, name: String, description: String, topicId: Uuid,
                time: Time, interval: TaskInterval, tags: MutableSet<Uuid>) :
            this(currentTask, Task(priority, name, currentTask.userId, description, topicId, time, interval, tags,currentTask.id))

    override fun execute(): List<Command> {
        val currentClone = currentTask.copy()
        currentTask.priority = newTask.priority
        currentTask.name = newTask.name
        currentTask.description = newTask.description
        currentTask.topicId = newTask.topicId
        currentTask.time = newTask.time
        currentTask.interval = newTask.interval
        currentTask.tags = newTask.tags
        currentTask.dbState = DBState.UPDATED
        return listOf(UpdateTaskCommand(currentTask, currentClone))
    }
}