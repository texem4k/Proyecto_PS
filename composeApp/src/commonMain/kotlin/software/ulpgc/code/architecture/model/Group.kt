package software.ulpgc.code.architecture.model

import software.ulpgc.code.architecture.io.DBObject
import software.ulpgc.code.architecture.io.DBState
import kotlin.uuid.Uuid

data class Group (
    var name: String,
    var description: String,
    var users: MutableMap<Uuid, Privilege> = mutableMapOf(),
    val id: Uuid = Uuid.random(),
    override var dbState: DBState = DBState.NEW
): DBObject