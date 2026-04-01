package software.ulpgc.code.architecture.model

import software.ulpgc.code.architecture.io.DBState
import kotlin.uuid.Uuid

data class Tag(
    override var dbState: DBState = DBState.DEFAULT,
    override val id: Uuid = Uuid.random(),
    var name: String,
    var topicId: Uuid
) : Entity