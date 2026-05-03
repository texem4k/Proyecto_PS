package software.ulpgc.code.architecture.control.commands

import software.ulpgc.code.architecture.control.logs.LogMaster
import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.tasks.Task
import kotlin.uuid.Uuid

class DeleteTaskCommand internal constructor (private val store: Storage, private val task: Task): Command {

    constructor(store: Storage, id: Uuid): this(store, store.tasks().find { it.id == id }!! )

    override fun execute(): List<Command> {
        LogMaster.log("DeleteTaskCommand {$task}")
        task.dbState = DBState.DELETED
        return listOf(CreateTaskCommand(store, task))
    }
}