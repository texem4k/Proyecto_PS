package software.ulpgc.code.architecture.control.commands

import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.Tag
import kotlin.uuid.Uuid

class CreateTagCommand internal constructor (private val store: Storage, private val tag: Tag) : Command {
    internal constructor(store: Storage, name: String, topicId: Uuid) : this(store, Tag(name, topicId))

    override fun execute(): List<Command> {
        tag.dbState = DBState.NEW
        store.addTags(listOf(tag))
        return listOf(DeleteTagCommand(store, tag))
    }
}