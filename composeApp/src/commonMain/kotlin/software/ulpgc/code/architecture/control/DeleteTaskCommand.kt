package software.ulpgc.code.architecture.control

import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.tasks.Task
import kotlin.uuid.Uuid

class DeleteTaskCommand(private val store: Storage, private val task: Task): Command {

    constructor(store: Storage, id: Uuid): this(store, store.tasks().find { it.id == id }!! )

    override fun execute(): Command {
        task.dbState = DBState.DELETED
        return DeleteTaskCommand(store, task)
    }
}