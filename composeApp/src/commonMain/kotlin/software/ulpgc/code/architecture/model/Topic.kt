package software.ulpgc.code.architecture.model

import kotlin.uuid.Uuid
import software.ulpgc.code.architecture.io.DBState

data class Topic (
    override var dbState: DBState = DBState.DEFAULT,
    override var id: Uuid = Uuid.random(),
    var name: String,
    var color: Int
) : Entity