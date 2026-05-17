package software.ulpgc.code.architecture.control.commands

import software.ulpgc.code.architecture.control.logs.LogMaster
import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.model.Tag

class UpdateTagCommand internal constructor (private val currentTag: Tag,private val newTag: Tag): Command {

    constructor(currentTag: Tag, newName: String) : this(
        currentTag, Tag(newName, currentTag.topicId, currentTag.id),
    )

    override fun execute(): List<Command> {
        LogMaster.log("UpdateTopicCommand {from=$currentTag to=$newTag}")
        val currentClone = currentTag.copy()
        currentTag.name = newTag.name
        currentTag.localDBState = DBState.UPDATED
        currentTag.cloudDBState = DBState.UPDATED
        return listOf(UpdateTagCommand(currentTag, currentClone))
    }
}