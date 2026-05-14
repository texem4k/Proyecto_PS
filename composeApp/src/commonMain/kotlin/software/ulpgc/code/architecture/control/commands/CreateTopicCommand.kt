package software.ulpgc.code.architecture.control.commands

import androidx.compose.ui.graphics.Color
import software.ulpgc.code.architecture.control.logs.LogMaster
import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.Topic

class CreateTopicCommand internal constructor (private val store: Storage, private val topic: Topic): Command {
    constructor (store: Storage, name: String, color: Color): this(store, Topic(name, color, store.currentGroup().id))

    override fun execute(): List<Command> {
        LogMaster.log("CreateTopicCommand {$topic}")
        topic.dbState = DBState.NEW
        store.addTopic(topic)
        return listOf(DeleteTopicCommand(store, topic))
    }
}