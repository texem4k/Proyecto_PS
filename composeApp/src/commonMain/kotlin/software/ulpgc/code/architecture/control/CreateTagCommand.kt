package software.ulpgc.code.architecture.control

import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.Tag
import kotlin.uuid.Uuid

class CreateTagCommand(private val store: Storage, private val tag: Tag) : Command {
    constructor(store: Storage, name: String, topicId: Uuid) : this(store, Tag(name, topicId))

    override fun execute(): Command {
        store.addTags(listOf(tag))
        return DeleteTagCommand(store, tag)
    }
}