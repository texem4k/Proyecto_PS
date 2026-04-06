package software.ulpgc.code.architecture.control

import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.Topic

class CreateTopicCommand(private val store: Storage, private val topic: Topic): Command {
    constructor (store: Storage, name: String, color: Int): this(store, Topic(name, color))

    override fun execute(): Command {
        store.addTopics(listOf(topic))
        return DeleteTopicCommand(store, topic)
    }
}