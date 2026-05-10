package software.ulpgc.code.architecture.control.commands

import software.ulpgc.code.architecture.control.logs.LogMaster
import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.model.tasks.Task

class UnmarkCompleteTaskCommand (val task: Task) : Command {

    override fun execute(): List<Command> {
        LogMaster.log("UnmarkCompleteTaskCommand {$task}")
        task.isCompleted = false
        task.dbState = DBState.UPDATED
        return listOf(MarkCompleteTaskCommand(task))
    }
}