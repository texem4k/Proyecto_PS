package software.ulpgc.code.architecture.control.commands

import software.ulpgc.code.architecture.control.logs.LogMaster
import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.io.Store
import software.ulpgc.code.architecture.model.tasks.Task
import kotlin.uuid.Uuid

class DeleteTaskCommand internal constructor (private val task: Task): Command {

    constructor(id: Uuid): this(Store.tasks().find { it.id == id }!! )

    override fun execute(): List<Command> {
        LogMaster.log("DeleteTaskCommand {$task}")
        task.dbState = DBState.DELETED
        return listOf(CreateTaskCommand(task))
    }
}