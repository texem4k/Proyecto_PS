package software.ulpgc.code.architecture.control.commands

import software.ulpgc.code.architecture.control.logs.LogMaster
import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.model.Tag
import kotlin.uuid.Uuid

class UpdateTagCommand internal constructor (private val currentTag: Tag,private val newTag: Tag): Command {

    constructor(currentTag: Tag, newName: String, newTopicId: Uuid) : this(
        currentTag, Tag(newName, newTopicId, currentTag.id),
    )

    override fun execute(): List<Command> {
        LogMaster.log("UpdateTopicCommand {from=$currentTag to=$newTag}")
        val currentClone = currentTag.copy()
        currentTag.name = newTag.name
        currentTag.topicId = newTag.topicId
        currentTag.dbState = DBState.UPDATED
        return listOf(UpdateTagCommand(currentTag, currentClone))
    }
}