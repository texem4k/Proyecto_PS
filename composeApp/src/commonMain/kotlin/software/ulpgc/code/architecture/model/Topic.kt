package software.ulpgc.code.architecture.model

import software.ulpgc.code.architecture.io.DBObject
import kotlin.uuid.Uuid
import software.ulpgc.code.architecture.io.DBState

data class Topic (
    var name: String,
    var color: Int,
    var id: Uuid = Uuid.random(),
    override var dbState: DBState = DBState.NEW
) : DBObject