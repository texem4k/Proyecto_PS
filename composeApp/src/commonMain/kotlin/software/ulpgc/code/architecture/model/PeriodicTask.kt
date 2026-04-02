package software.ulpgc.code.architecture.model

import software.ulpgc.code.architecture.io.DBState
import kotlin.uuid.Uuid

class PeriodicTask (
    id: Uuid,
    dbState: DBState,
    priority: Int,
    name: String,
    userId: Uuid,
    description: String,
    topicId: Uuid,
    tags: MutableList<Tag> = mutableListOf<Tag>(),
    time: Time,
    var interval: Interval
) : Task(
    id = id,
    dbState = dbState,
    priority = priority,
    name = name,
    userId = userId,
    description = description,
    topicId = topicId,
    tags = tags,
    time = time
){}