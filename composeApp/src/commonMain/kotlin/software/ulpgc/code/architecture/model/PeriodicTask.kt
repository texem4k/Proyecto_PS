package software.ulpgc.code.architecture.model

import software.ulpgc.code.architecture.io.DBState
import kotlin.uuid.Uuid

class PeriodicTask (
    dbState: DBState = DBState.DEFAULT,
    priority: Int,
    name: String,
    userId: Uuid,
    description: String,
    topicId: Uuid,
    tags: MutableList<Tag>? = null,
    time: Time,
    interval: Interval
) : Task(
    dbState = dbState,
    priority = priority,
    name = name,
    userId = userId,
    description = description,
    topicId = topicId,
    tags = tags,
    time = time
){}