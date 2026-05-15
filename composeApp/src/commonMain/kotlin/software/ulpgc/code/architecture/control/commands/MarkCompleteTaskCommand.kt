package software.ulpgc.code.architecture.control.commands

import software.ulpgc.code.architecture.control.logs.LogMaster
import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.io.Store
import software.ulpgc.code.architecture.model.tasks.Task
import kotlin.time.Clock

class MarkCompleteTaskCommand (val task: Task) : Command {
    override fun execute(): List<Command> {
        LogMaster.log("MarkCompleteTaskCommand {$task}")
        task.isCompleted = true
        task.dbState = DBState.UPDATED
        setCompletionStat(task)
        return listOf(UnmarkCompleteTaskCommand(task))
    }


    fun setCompletionStat(task: Task){
        val stat = Store.completions()
            .filter{ it.taskId == task.id }
            .filterNot { it.completed }
            .sortedByDescending { it.date }
            .first()
        stat.completed = true
        stat.date = Clock.System.now()
        stat.dbState = DBState.UPDATED
    }
}