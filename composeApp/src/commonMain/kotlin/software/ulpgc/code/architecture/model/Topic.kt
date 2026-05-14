package software.ulpgc.code.architecture.model

import androidx.compose.ui.graphics.Color
import software.ulpgc.code.architecture.io.DBObject
import kotlin.uuid.Uuid
import software.ulpgc.code.architecture.io.DBState

data class Topic (
    var name: String,
    var color: Color,
    val groupId: Uuid,
    var id: Uuid = Uuid.random(),
    override var dbState: DBState = DBState.NEW
) : DBObject {
    override fun toString(): String {
        return "Topic(id=$id, groupId=$groupId, name='$name', color=$color)"
    }
}