package software.ulpgc.code.architecture.control

import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.Topic
import kotlin.uuid.Uuid

class DeleteTopicCommand internal constructor (private val store: Storage, private val topic: Topic): Command {
    constructor(store: Storage, id: Uuid): this(store, store.topics().find{ it.id == id }!!)

    override fun execute(): Command {
        topic.dbState = DBState.DELETED
        return CreateTopicCommand(store, topic)
    }
}