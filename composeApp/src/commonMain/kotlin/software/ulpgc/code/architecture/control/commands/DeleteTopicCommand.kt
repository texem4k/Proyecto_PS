package software.ulpgc.code.architecture.control.commands

import software.ulpgc.code.architecture.control.logs.LogMaster
import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.io.Store
import software.ulpgc.code.architecture.model.Topic
import kotlin.uuid.Uuid

class DeleteTopicCommand internal constructor (private val topic: Topic): Command {
    constructor(id: Uuid): this(Store.topics().find{ it.id == id }!!)

    override fun execute(): List<Command> {
        LogMaster.log("DeleteTopicCommand {$topic}")
        topic.localDBState = DBState.DELETED
        topic.cloudDBState = DBState.DELETED
        val commands = Store.tasks()
            .filter { task -> task.topicId == topic.id }
            .flatMap { task -> DeleteTaskCommand(task).execute() }
            .toMutableList()
        commands.addAll(Store.tags()
            .filter { tag -> tag.topicId == topic.id }
            .flatMap { tag -> DeleteTagCommand(tag).execute() })
        commands.add(CreateTopicCommand(topic))
        return commands.toList().reversed()
    }
}