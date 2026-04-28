package software.ulpgc.code.architecture.control.commands

import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.Tag
import software.ulpgc.code.architecture.model.tasks.Task
import kotlin.uuid.Uuid

class DeleteTagCommand internal constructor (private val store: Storage, private val tag: Tag): Command {
    constructor(store: Storage, id: Uuid): this(store, store.tags().find { it.id == id }!!)

    override fun execute(): List<Command> {
        tag.dbState = DBState.DELETED
        val commands = store.tasks()
            .filter { task -> task.tags.contains(tag.id) }
            .flatMap { task -> removeTagIn(task, tag.id) }
            .toMutableList()
        commands.add(CreateTagCommand(store, tag))
        return commands.toList().reversed()
    }

    private fun removeTagIn(task: Task, tagId : Uuid) : List<Command> {
        val newTask = task.copy();
        newTask.tags.remove(tagId)
        return UpdateTaskCommand(task, newTask).execute()
    }
}