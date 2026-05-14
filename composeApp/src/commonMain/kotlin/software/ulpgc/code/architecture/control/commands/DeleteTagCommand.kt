package software.ulpgc.code.architecture.control.commands

import software.ulpgc.code.architecture.control.logs.LogMaster
import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.io.Store
import software.ulpgc.code.architecture.model.Tag
import software.ulpgc.code.architecture.model.tasks.Task
import kotlin.uuid.Uuid

class DeleteTagCommand internal constructor (private val tag: Tag): Command {
    constructor(id: Uuid): this(Store.tags().find { it.id == id }!!)

    override fun execute(): List<Command> {
        LogMaster.log("DeleteTagCommand {$tag}")
        tag.dbState = DBState.DELETED
        val commands = Store.tasks()
            .filter { task -> task.tags.contains(tag.id) }
            .flatMap { task -> removeTagIn(task, tag.id) }
            .toMutableList()
        commands.add(CreateTagCommand(tag))
        return commands.toList().reversed()
    }

    private fun removeTagIn(task: Task, tagId : Uuid) : List<Command> {
        val newTask = task.copy()
        newTask.tags.remove(tagId)
        return UpdateTaskCommand(task, newTask).execute()
    }
}