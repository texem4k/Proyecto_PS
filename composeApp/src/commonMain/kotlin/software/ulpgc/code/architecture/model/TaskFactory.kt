package software.ulpgc.code.architecture.model

import kotlin.uuid.Uuid

class TaskFactory {
    fun createTask(
        priority: Int,
        name: String,
        userId: Uuid,
        description: String,
        topicId: Uuid,
        tags: List<Tag>,
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

    fun createPeriodicTask(
        priority: Int,
        name: String,
        userId: Uuid,
        description: String,
        topicId: Uuid,
        tags: List<Tag>,
        time: Time
        ) : PeriodicTask {
        return PeriodicTask(
            priority = priority,
            name = name,
            userId = userId,
            description = description,
            topicId = topicId,
            tags = tags,
            time = time
        )
    }
}