package software.ulpgc.code.architecture.control

import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.tasks.Task
import software.ulpgc.code.architecture.model.tasks.TaskInterval
import software.ulpgc.code.architecture.model.times.Time
import kotlin.uuid.Uuid

class CreateTaskCommand(private val store: Storage, private val task: Task): Command {
    constructor(store: Storage, priority: Int, name: String,
                userId: Uuid, description: String, topicId: Uuid,
                time: Time, interval: TaskInterval, tags: MutableList<Uuid>) :
            this(store, Task(priority, name, userId, description, topicId, time, interval, tags))

    override fun execute(): Command {
        store.addTasks(listOf(task))
        return DeleteTaskCommand(store, task)
    }
}