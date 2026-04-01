package software.ulpgc.code.architecture.model

import software.ulpgc.code.architecture.io.DBState
import kotlin.uuid.Uuid

class TaskFactory {
    fun createTask(
        id: Uuid = Uuid.random(),
        dbState: DBState = DBState.NEW,
        priority: Int,
        name: String,
        userId: Uuid,
        description: String,
        topicId: Uuid,
        tags: MutableList<Tag> = mutableListOf<Tag>(),
        time: Time
        ): Task {
        return Task(
            id = id,
            dbState =  dbState,
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
        id: Uuid = Uuid.random(),
        dbState: DBState = DBState.NEW,
        priority: Int,
        name: String,
        userId: Uuid,
        description: String,
        topicId: Uuid,
        tags: MutableList<Tag> = mutableListOf<Tag>(),
        time: Time,
        interval: Interval,
        ) : PeriodicTask {
        return PeriodicTask(
            id = id,
            dbState =  dbState,
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