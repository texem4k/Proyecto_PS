package software.ulpgc.code.architecture.model

import kotlin.uuid.Uuid

data class User(
    var id: Uuid = Uuid.random(),
    var name: String,
)