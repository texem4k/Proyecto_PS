package software.ulpgc.code.architecture.control.commands

import software.ulpgc.code.architecture.control.logs.LogMaster
import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.model.Group
import software.ulpgc.code.architecture.model.Privilege
import kotlin.uuid.Uuid

class EditPrivilegesCommand(private val currentGroup: Group, private val newGroup: Group): Command {
    constructor(group: Group, privileges: MutableMap<Uuid, Privilege>):
            this(group, Group(group.name,group.description,privileges,group.id))

    override fun execute(): List<Command> {
        LogMaster.log("UpdatePrivilegesCommand {from=$currentGroup to=$newGroup}")
        currentGroup.users = newGroup.users
        currentGroup.localDBState = DBState.UPDATED
        currentGroup.cloudDBState = DBState.UPDATED
        return listOf()
    }
}