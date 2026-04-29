package software.ulpgc.code.architecture.model.tasks

import software.ulpgc.code.architecture.io.DBObject
import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.model.times.Time
import kotlin.uuid.Uuid

class Task (
    var priority: Int,
    var name: String,
    var userId: Uuid,
    var description: String,
    var topicId: Uuid,
    var time: Time,
    var interval: TaskInterval,
    var tags: MutableSet<Uuid> = mutableSetOf(),
    val id: Uuid = Uuid.random(),
    override var dbState: DBState = DBState.NEW,
) : DBObject {
    fun copy() = Task(priority, name, userId, description, topicId, time, interval, tags.map { it }.toMutableSet(), id)
    override fun toString(): String {
        return "Task(id=$id, name='$name', " +
                "userId=$userId, description='$description', " +
                "priority=$priority, topicId=$topicId, " +
                "time=Time($time), interval=$interval, " +
                "tags=$tags)"
    }

}
