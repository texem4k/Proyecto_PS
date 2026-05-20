package software.ulpgc.code.architecture.model

import software.ulpgc.code.architecture.io.DBObject
import software.ulpgc.code.architecture.io.DBState
import kotlin.uuid.Uuid

data class Tag(
    var name: String,
    var topicId: Uuid,
    val id: Uuid = Uuid.generateV7(),
    override var localDBState: DBState = DBState.NEW,
    override var cloudDBState: DBState = DBState.NEW
) : DBObject {
    override fun toString(): String {
        return "Tag(id=$id, name='$name', topicId=$topicId)"
    }
}