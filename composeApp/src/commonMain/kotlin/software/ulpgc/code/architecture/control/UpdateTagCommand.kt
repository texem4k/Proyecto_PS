package software.ulpgc.code.architecture.control

import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.model.Tag
import kotlin.uuid.Uuid

class UpdateTagCommand (private val currentTag: Tag,private val newTag: Tag): Command {

    constructor(currentTag: Tag, newName: String, newTopicId: Uuid) : this(
        currentTag, Tag(newName, newTopicId, currentTag.id),
    )

    override fun execute(): Command {
        val currentClone = currentTag.copy()
        currentTag.name = newTag.name
        currentTag.topicId = newTag.topicId
        currentTag.dbState = DBState.UPDATED
        return UpdateTagCommand(currentTag, currentClone)
    }
}