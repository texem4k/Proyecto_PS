package software.ulpgc.code.architecture.control.commands

import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.Topic
import kotlin.uuid.Uuid

class DeleteTopicCommand internal constructor (private val store: Storage, private val topic: Topic): Command {
    constructor(store: Storage, id: Uuid): this(store, store.topics().find{ it.id == id }!!)

    override fun execute(): List<Command> {
        topic.dbState = DBState.DELETED
        val commands = store.tasks()
            .filter { task -> task.topicId == topic.id }
            .flatMap { task -> DeleteTaskCommand(store, task).execute() }
            .toMutableList()
        commands.addAll(store.tags()
            .filter { tag -> tag.topicId == topic.id }
            .flatMap { tag -> DeleteTagCommand(store, tag).execute() })
        commands.add(CreateTopicCommand(store, topic))
        return commands.toList().reversed()
    }
}