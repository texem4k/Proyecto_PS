package software.ulpgc.code.architecture.control.commands

import software.ulpgc.code.architecture.control.logs.LogMaster
import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.io.Store
import software.ulpgc.code.architecture.model.Priority
import software.ulpgc.code.architecture.model.tasks.CompletionStat
import software.ulpgc.code.architecture.model.tasks.Task
import software.ulpgc.code.architecture.model.tasks.TaskInterval
import software.ulpgc.code.architecture.model.times.Time
import kotlin.uuid.Uuid

class CreateTaskCommand internal constructor (private val task: Task, private val stats: List<CompletionStat>): Command {
    constructor(priority: Priority, name: String, description: String,
                topicId: Uuid, time: Time, interval: TaskInterval, tags: MutableSet<Uuid>, users: MutableSet<Uuid>) :
            this(Task(topicId, name, description, time, interval, priority, tags, users), listOf())

    override fun execute(): List<Command> {
        LogMaster.log("CreateTaskCommand {$task}")
        task.dbState = DBState.NEW
        Store.addTask(task)
        if (stats.isEmpty()) {
            Store.addCompletionStat(CompletionStat(task.id, false, task.time.end))
        } else {
            stats.forEach { Store.addCompletionStat(it) }
        }
        return listOf(DeleteTaskCommand(task))
    }
}