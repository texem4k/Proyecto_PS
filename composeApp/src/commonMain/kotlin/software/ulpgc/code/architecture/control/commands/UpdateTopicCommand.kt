package software.ulpgc.code.architecture.control.commands

import software.ulpgc.code.architecture.control.logs.LogMaster
import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.model.Topic

class UpdateTopicCommand internal constructor (private val currentTopic: Topic, private val newTopic: Topic): Command {

    constructor(currentTopic: Topic, newName: String, newColor: Int) : this(
        currentTopic, Topic(newName, newColor, currentTopic.id),
    )

    override fun execute(): List<Command> {
        LogMaster.log("UpdateTopicCommand {from=$currentTopic, to=$newTopic}")
        val currentClone = currentTopic.copy()
        currentTopic.name = newTopic.name
        currentTopic.color = newTopic.color
        currentTopic.dbState = DBState.UPDATED
        return listOf(UpdateTopicCommand(currentTopic, currentClone))
    }
}