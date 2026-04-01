package software.ulpgc.code.architecture.model

import software.ulpgc.code.architecture.io.DBState
import kotlin.uuid.Uuid

open class Task (
    override var dbState: DBState = DBState.DEFAULT,
    override val id: Uuid? = Uuid.random(),
    var priority: Int,
    var name: String,
    var userId: Uuid,
    var description: String,
    var topicId: Uuid,
    var tags: MutableList<Tag>? = mutableListOf<Tag>(),
    var time: Time
) : Entity
