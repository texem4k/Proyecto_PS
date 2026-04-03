package software.ulpgc.code.architecture.model

import software.ulpgc.code.architecture.io.DBState
import kotlin.uuid.Uuid

data class Tag(
    override val id: Uuid = Uuid.random(),
    override var dbState: DBState = DBState.NEW,
    var name: String,
    var topicId: Uuid
) : Entity