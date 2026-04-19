package software.ulpgc.code.architecture.control

import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.Tag
import kotlin.uuid.Uuid

class DeleteTagCommand internal constructor (private val store: Storage, private val tag: Tag): Command {
    constructor(store: Storage, id: Uuid): this(store, store.tags().find { it.id == id }!!)

    override fun execute(): Command {
        tag.dbState = DBState.DELETED
        return CreateTagCommand(store, tag)
    }
}