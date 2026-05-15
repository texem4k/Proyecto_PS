package software.ulpgc.code.architecture.control.commands

import software.ulpgc.code.architecture.control.logs.LogMaster
import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.io.Store
import software.ulpgc.code.architecture.model.tasks.Task

class UnmarkCompleteTaskCommand (val task: Task) : Command {

    override fun execute(): List<Command> {
        LogMaster.log("UnmarkCompleteTaskCommand {$task}")
        task.isCompleted = false
        task.dbState = DBState.UPDATED
        setCompletionStat(task)
        return listOf(MarkCompleteTaskCommand(task))
    }

    private fun setCompletionStat(task: Task){
        val stat = Store.completions()
            .filter { it.taskId == task.id }
            .filter { it.completed }
            .sortedByDescending { it.date }
            .first()
        stat.completed = false
        stat.date = task.time.end
        stat.dbState = DBState.UPDATED
    }



}