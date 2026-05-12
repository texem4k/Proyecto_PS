package software.ulpgc.code.architecture.model

import software.ulpgc.code.architecture.io.DBObject
import software.ulpgc.code.architecture.io.DBState
import kotlin.uuid.Uuid

data class User(
    var name: String,
    val id: Uuid = Uuid.random(),
    override var dbState: DBState = DBState.NEW
): DBObject {
    override fun toString(): String {
        return "User(id=$id, name='$name')"
    }
}