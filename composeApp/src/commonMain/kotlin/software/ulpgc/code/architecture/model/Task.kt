package software.ulpgc.code.architecture.model

import kotlin.uuid.Uuid

data class Task (
    var id: Uuid,
    var type: Int,
    var name: String,
    var userid: Int,
    var description: String,
    var topicid: Int
)