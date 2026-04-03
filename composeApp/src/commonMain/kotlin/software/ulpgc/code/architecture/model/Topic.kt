package software.ulpgc.code.architecture.model

import kotlin.uuid.Uuid
import software.ulpgc.code.architecture.io.DBState

data class Topic (
    override var id: Uuid = Uuid.random(),
    override var dbState: DBState = DBState.NEW,
    var name: String,
    var color: Int
) : Entity