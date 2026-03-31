package software.ulpgc.code.architecture.model

import kotlin.uuid.Uuid

open class Task (
    val id: Uuid = Uuid.random(),
    var priority: Int,
    var name: String,
    var userId: Uuid,
    var description: String,
    var topicId: Uuid,
    var tags: List<Tag>? = null,
    var time: Time
)