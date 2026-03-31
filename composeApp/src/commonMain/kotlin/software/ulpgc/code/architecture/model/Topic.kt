package software.ulpgc.code.architecture.model

import kotlin.uuid.Uuid

data class Topic (
    var id: Uuid,
    var name: String,
    var color: Int
)