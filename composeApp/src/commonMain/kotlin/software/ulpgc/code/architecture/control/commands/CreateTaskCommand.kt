package software.ulpgc.code.architecture.control.commands

import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.tasks.Task
import software.ulpgc.code.architecture.model.tasks.TaskInterval
import software.ulpgc.code.architecture.model.times.Time
import kotlin.uuid.Uuid

class CreateTaskCommand internal constructor (private val store: Storage, private val task: Task): Command {
    constructor(store: Storage, priority: Int, name: String,
                userId: Uuid, description: String, topicId: Uuid,
                time: Time, interval: TaskInterval, tags: MutableSet<Uuid>) :
            this(store, Task(priority, name, userId, description, topicId, time, interval, tags))

    override fun execute(): List<Command> {
        task.dbState = DBState.NEW
        store.addTasks(listOf(task))
        return listOf(DeleteTaskCommand(store, task))
    }
}