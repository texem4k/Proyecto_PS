package software.ulpgc.code.architecture.control.commands

import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.io.Store
import software.ulpgc.code.architecture.model.Group
import software.ulpgc.code.architecture.model.Privilege
import kotlin.uuid.Uuid

class ExitGroupCommand(private val group: Group, private val userId: Uuid): Command {
    override fun execute(): List<Command> {
        group.users = group.users.minus(userId).toMutableMap()
        if (group.users[userId] == Privilege.ADMIN) setNextUserToAdmin(group)
        group.localDBState = DBState.DELETED
        group.cloudDBState = DBState.UPDATED
        if (group.id == Store.currentGroup().id) Store.changeGroupTo(Store.groups().first())
        return listOf()
    }

    private fun setNextUserToAdmin(group: Group) {
        val user = group.users.entries.minByOrNull { it.value.ordinal }
        if (user != null) group.users[user.key] = Privilege.ADMIN
        else {
            group.localDBState = DBState.DELETED
            group.cloudDBState = DBState.DELETED
        }
    }
}