package software.ulpgc.code.architecture.model

import software.ulpgc.code.architecture.io.DBState
import kotlin.uuid.Uuid

open class Task (
    override val id: Uuid,
    override var dbState: DBState,
    var priority: Int,
    var name: String,
    var userId: Uuid,
    var description: String,
    var topicId: Uuid,
    var tags: MutableList<Tag>,
    var time: Time
) : Entity
