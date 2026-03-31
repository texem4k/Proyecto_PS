package software.ulpgc.code.architecture.model

import kotlin.uuid.Uuid

data class User(
    var id: Uuid,
    var name: String,
)