package software.ulpgc.code.architecture.model

import kotlin.uuid.Uuid

class TaskFactory {
    fun createTask(
        priority: Int,
        name: String,
        userId: Uuid,
        description: String,
        topicId: Uuid,
        tags: MutableList<Tag>?,
        time: Time
        ): Task {
        return Task(
            priority = priority,
            name = name,
            userId = userId,
            description = description,
            topicId = topicId,
            tags = tags,
            time = time
        )
    }

    fun createTask(
        priority: Int,
        name: String,
        userId: Uuid,
        description: String,
        topicId: Uuid,
        tags: MutableList<Tag>?,
        time: Time,
        interval: Interval,
        ) : PeriodicTask {
        return PeriodicTask(
            priority = priority,
            name = name,
            userId = userId,
            description = description,
            topicId = topicId,
            tags = tags,
            time = time,
            interval = interval
        )
    }
}