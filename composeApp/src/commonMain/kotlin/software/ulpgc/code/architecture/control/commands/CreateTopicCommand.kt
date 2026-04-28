package software.ulpgc.code.architecture.control.commands

import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.Topic

class CreateTopicCommand internal constructor (private val store: Storage, private val topic: Topic): Command {
    constructor (store: Storage, name: String, color: Int): this(store, Topic(name, color))

    override fun execute(): List<Command> {
        topic.dbState = DBState.NEW
        store.addTopics(listOf(topic))
        return listOf(DeleteTopicCommand(store, topic))
    }
}