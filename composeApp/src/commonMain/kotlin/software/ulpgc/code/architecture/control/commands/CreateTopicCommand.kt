package software.ulpgc.code.architecture.control.commands

import androidx.compose.ui.graphics.Color
import software.ulpgc.code.architecture.control.logs.LogMaster
import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.io.Store
import software.ulpgc.code.architecture.io.isCloudDisabled
import software.ulpgc.code.architecture.model.Topic

class CreateTopicCommand internal constructor (private val topic: Topic): Command {
    constructor (name: String, color: Color): this(Topic(name, color, Store.currentGroup().id))

    override fun execute(): List<Command> {
        LogMaster.log("CreateTopicCommand {$topic}")
        topic.localDBState = DBState.NEW
        if (!Store.currentGroup().isCloudDisabled()) topic.cloudDBState = DBState.NEW
        Store.add(topic)
        return listOf(DeleteTopicCommand(topic))
    }
}