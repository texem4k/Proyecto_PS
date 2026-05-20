package software.ulpgc.code.architecture.control.commands

import software.ulpgc.code.architecture.control.logs.LogMaster
import software.ulpgc.code.architecture.io.Store
import software.ulpgc.code.architecture.model.Group
import software.ulpgc.code.architecture.model.Privilege

class CreateGroupCommand(private val group: Group): Command {
internal constructor(name: String, description: String):
        this(Group(name, description, mutableMapOf(Store.currentUser() to Privilege.ADMIN)))

    override fun execute(): List<Command> {
        LogMaster.log("CreateGroupCommand {$group}")
        Store.add(group)
        Store.changeGroupTo(group)
        return listOf()
    }
}