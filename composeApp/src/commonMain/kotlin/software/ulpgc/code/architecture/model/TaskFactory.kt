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

    fun toPeriodicTask(task: Task, interval: Interval): PeriodicTask {
        return PeriodicTask(
            id = task.id,
            dbState = task.dbState,
            priority = task.priority,
            name = task.name,
            userId = task.userId,
            description = task.description,
            topicId = task.topicId,
            tags = task.tags,
            time = task.time,
            interval = interval
        )
    }

    fun toTask(periodicTask: PeriodicTask): Task {
        return Task(
            id = periodicTask.id,
            dbState = periodicTask.dbState,
            priority = periodicTask.priority,
            name = periodicTask.name,
            userId = periodicTask.userId,
            description = periodicTask.description,
            topicId = periodicTask.topicId,
            tags = periodicTask.tags,
            time = periodicTask.time
        )
    }
}