package software.ulpgc.code.architecture.model

import software.ulpgc.code.architecture.io.DBObject
import software.ulpgc.code.architecture.io.DBState
import kotlin.uuid.Uuid

data class Group (
    var name: String,
    var description: String,
    var users: MutableMap<Uuid, Privilege> = mutableMapOf(),
    val id: Uuid = Uuid.generateV7(),
    override var localDBState: DBState = DBState.NEW,
    override var cloudDBState: DBState = DBState.NEW
): DBObject {
    companion object{
        fun privilegeString(privileges: MutableMap<Uuid, Privilege>): String{
            return privileges.entries.joinToString(";") { (uuid, privilege) ->
                "${uuid}:${privilege.name}"
            }
        }
        fun parsePrivileges(privileges: String): MutableMap<Uuid, Privilege> {
            return privileges
                .split(";")
                .mapNotNull { entry ->
                    runCatching {
                        val (uuidStr, privilegeStr) = entry.split(":")
                        Uuid.parse(uuidStr) to Privilege.valueOf(privilegeStr)
                    }.getOrNull()
                }
                .toMap()
                .toMutableMap()
        }
    }
}