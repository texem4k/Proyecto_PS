package software.ulpgc.code.architecture.model

import kotlin.uuid.Uuid

class PeriodicTask (
    priority: Int,
    name: String,
    userId: Uuid,
    description: String,
    topicId: Uuid,
    tags: List<Tag>? = null,
    time: Time
) : Task(
    priority = priority,
    name = name,
    userId = userId,
    description = description,
    topicId = topicId,
    tags = tags,
    time = time
){}