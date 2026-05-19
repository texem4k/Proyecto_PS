package software.ulpgc.code.architecture.control.commands

import software.ulpgc.code.architecture.control.logs.LogMaster
import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.model.Group

class UpdateGroupCommand(private val currentGroup: Group, private val newGroup: Group): Command {
    internal constructor(group: Group, name: String, description: String):
            this(group, Group(name, description, group.users, group.id))

    override fun execute(): List<Command> {
        LogMaster.log("UpdateGroupCommand {from=$currentGroup to=$newGroup}")
        currentGroup.name = newGroup.name
        currentGroup.description = newGroup.description
        currentGroup.localDBState = DBState.UPDATED
        currentGroup.cloudDBState = DBState.UPDATED
        return listOf()
    }
}