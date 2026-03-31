package software.ulpgc.code.architecture.model

import kotlin.uuid.Uuid
import software.ulpgc.code.architecture.io.DBState

data class Topic (
    var dbState: DBState = DBState.DEFAULT,
    var id: Uuid,
    var name: String,
    var color: Int
)