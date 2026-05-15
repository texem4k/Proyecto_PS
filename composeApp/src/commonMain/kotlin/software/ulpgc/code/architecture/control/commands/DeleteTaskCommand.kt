package software.ulpgc.code.architecture.control.commands

import software.ulpgc.code.architecture.control.logs.LogMaster
import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.io.Store
import software.ulpgc.code.architecture.model.tasks.CompletionStat
import software.ulpgc.code.architecture.model.tasks.Task
import kotlin.uuid.Uuid

class DeleteTaskCommand internal constructor (private val task: Task): Command {

    constructor(id: Uuid): this(Store.tasks().find { it.id == id }!! )

    override fun execute(): List<Command> {
        LogMaster.log("DeleteTaskCommand {$task}")
        task.dbState = DBState.DELETED
        val stats = Store.completions().filter {task.id == it.taskId}.toList()
        val copyStats = copyOf(stats)
        stats.forEach { it.dbState = DBState.DELETED }
        return listOf(CreateTaskCommand(task, copyStats))
    }

    fun copyOf(stats: List<CompletionStat>): List<CompletionStat> {
        return stats.map{ CompletionStat(it.taskId, it.completed, it.date, it.id) }
    }
}