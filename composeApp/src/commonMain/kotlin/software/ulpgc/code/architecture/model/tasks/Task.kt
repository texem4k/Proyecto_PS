package software.ulpgc.code.architecture.model.tasks

import org.koin.core.time.inMs
import software.ulpgc.code.architecture.io.DBObject
import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.model.times.Time
import kotlin.uuid.Uuid
import kotlin.math.*
import kotlin.time.Duration

private const val MAX = 10.0

class Task (
    var priority: Int,
    var name: String,
    var userId: Uuid,
    var description: String,
    var topicId: Uuid,
    var time: Time,
    var interval: TaskInterval,
    var tags: MutableSet<Uuid> = mutableSetOf(),
    var isCompleted: Boolean = false,
    val id: Uuid = Uuid.random(),
    override var dbState: DBState = DBState.NEW,
) : DBObject {
    fun copy() = Task(priority, name, userId, description, topicId, time, interval, tags.map { it }.toMutableSet(), isCompleted, id)
    override fun toString(): String {
        return "Task(id=$id, name='$name', " +
                "userId=$userId, description='$description', " +
                "priority=$priority, topicId=$topicId, " +
                "time=Time($time), interval=$interval, " +
                "tags=$tags, isCompleted=$isCompleted)"
    }

    fun significanceFactor(): Double {
        val hoursUntilEnd = hoursFrom(this.time.timeUntilEnd())
        val hoursDuration = hoursFrom(this.time.duration())
        val exponent = 1.0 / hoursDuration
        val factor = exponent * 0.9 * (hoursUntilEnd - hoursDuration * (3.75 + 0.2 * this.priority))
        return MAX * (1 + 10.0.pow(factor)).pow(-1)
    }
    private fun hoursFrom(time: Duration): Double {
        return time.inMs / 3600000.0
    }
}
