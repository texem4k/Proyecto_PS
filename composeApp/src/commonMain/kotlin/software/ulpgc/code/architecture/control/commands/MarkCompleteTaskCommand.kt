package software.ulpgc.code.architecture.control.commands

import software.ulpgc.code.architecture.control.logs.LogMaster
import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.model.tasks.Task

class MarkCompleteTaskCommand (val task: Task) : Command {
    override fun execute(): List<Command> {
        LogMaster.log("MarkCompleteTaskCommand {$task}")
        task.isCompleted = true
        task.dbState = DBState.UPDATED
        return listOf(UnmarkCompleteTaskCommand(task))
    }
}