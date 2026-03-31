package software.ulpgc.code.architecture.model

import kotlin.uuid.Uuid

data class Tag(
    var id: Uuid,
    var name: String,
    var topicid: Int
)