package software.ulpgc.code.architecture.control.commands

import software.ulpgc.code.architecture.control.logs.LogMaster
import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.io.Store
import software.ulpgc.code.architecture.io.isCloudDisabled
import software.ulpgc.code.architecture.model.Tag
import kotlin.uuid.Uuid

class CreateTagCommand internal constructor (private val tag: Tag) : Command {
    internal constructor(name: String, topicId: Uuid) : this(Tag(name, topicId))

    override fun execute(): List<Command> {
        LogMaster.log("CreateTagCommand {$tag}")
        tag.localDBState = DBState.NEW
        if (!Store.currentGroup().isCloudDisabled()) tag.cloudDBState = DBState.NEW
        Store.add(tag)
        return listOf(DeleteTagCommand(tag))
    }
}