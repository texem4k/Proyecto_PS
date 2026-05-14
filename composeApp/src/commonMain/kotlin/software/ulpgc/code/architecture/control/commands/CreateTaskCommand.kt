package software.ulpgc.code.architecture.control.commands

import software.ulpgc.code.architecture.control.logs.LogMaster
import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.Priority
import software.ulpgc.code.architecture.model.tasks.Task
import software.ulpgc.code.architecture.model.tasks.TaskInterval
import software.ulpgc.code.architecture.model.times.Time
import kotlin.uuid.Uuid

class CreateTaskCommand internal constructor (private val store: Storage, private val task: Task): Command {
    constructor(store: Storage, priority: Priority, name: String, description: String,
                topicId: Uuid, time: Time, interval: TaskInterval, tags: MutableSet<Uuid>, users: MutableSet<Uuid>) :
            this(store, Task(topicId, name, description, time, interval, priority, tags, users))

    override fun execute(): List<Command> {
        LogMaster.log("CreateTaskCommand {$task}")
        task.dbState = DBState.NEW
        store.addTask(task)
        return listOf(DeleteTaskCommand(store, task))
    }
}