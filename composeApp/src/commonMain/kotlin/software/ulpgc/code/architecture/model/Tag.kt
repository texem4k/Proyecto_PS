package software.ulpgc.code.architecture.model

import software.ulpgc.code.architecture.io.DBState
import kotlin.uuid.Uuid

data class Tag(
    var dbState: DBState = DBState.DEFAULT,
    val id: Uuid = Uuid.random(),
    var name: String,
    var topicId: Uuid
)