package software.ulpgc.code.architecture.model

import kotlin.uuid.Uuid

data class TaskParams(
    val priority: Int,
    val name: String,
    val userId: Uuid,
    val description: String,
    val topicId: Uuid,
    val tags: List<Tag>,
    val time: Time
)

class TaskFactory {
    fun createTask(
        params: TaskParams,
        ): Task {
        return Task(
            priority = params.priority,
            name = params.name,
            userId = params.userId,
            description = params.description,
            topicId = params.topicId,
            tags = params.tags,
            time = params.time
        )
    }

    fun createPeriodicTask(
        params: TaskParams,
        interval: Interval,
        ) : PeriodicTask {
        return PeriodicTask(
            priority = params.priority,
            name = params.name,
            userId = params.userId,
            description = params.description,
            topicId = params.topicId,
            tags = params.tags,
            time = params.time,
            interval = interval
        )
    }
}