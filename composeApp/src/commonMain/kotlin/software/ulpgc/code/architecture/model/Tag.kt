package software.ulpgc.code.architecture.model

import kotlin.uuid.Uuid

data class Tag(
    val id: Uuid = Uuid.random(),
    var name: String,
    var topicId: Uuid
)