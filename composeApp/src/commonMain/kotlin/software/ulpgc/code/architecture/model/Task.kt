package software.ulpgc.code.architecture.model

import software.ulpgc.code.architecture.io.DBState
import kotlin.uuid.Uuid

open class Task (
    var dbState: DBState = DBState.DEFAULT,
    val id: Uuid = Uuid.random(),
    var priority: Int,
    var name: String,
    var userId: Uuid,
    var description: String,
    var topicId: Uuid,
    var tags: List<Tag>? = listOf(),
    var time: Time
)
