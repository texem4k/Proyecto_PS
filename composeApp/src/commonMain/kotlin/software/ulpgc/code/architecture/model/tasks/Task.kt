package software.ulpgc.code.architecture.model.tasks

import software.ulpgc.code.architecture.io.DBObject
import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.model.Priority
import software.ulpgc.code.architecture.model.times.Time
import kotlin.uuid.Uuid
import kotlin.math.*
import kotlin.time.Duration

const val MAX = 10.0

data class Task (
    var topicId: Uuid,
    var name: String,
    var description: String,
    var time: Time,
    var interval: TaskInterval,
    var priority: Priority,
    var tags: MutableSet<Uuid> = mutableSetOf(),
    var users: MutableSet<Uuid> = mutableSetOf(),
    var isCompleted: Boolean = false,
    val id: Uuid = Uuid.generateV7(),
    override var localDBState: DBState = DBState.NEW,
    override var cloudDBState: DBState = DBState.NEW
) : DBObject {
    fun copy() = Task(topicId, name, description, time, interval,  priority, tags, users, isCompleted, id, localDBState, cloudDBState)

    fun significanceFactor(): Double {
        val hoursUntilEnd = hoursFrom(this.time.timeUntilEnd())
        val hoursDuration = hoursFrom(this.time.duration())
        val exponent = 1.0 / hoursDuration
        val factor = exponent * 0.9 * (hoursUntilEnd - hoursDuration * (3.75 + 0.2 * this.priority.value))
        return MAX * (1 + 10.0.pow(factor)).pow(-1)
    }
    private fun hoursFrom(time: Duration): Double {
        return time.inWholeMilliseconds/ 3600000.0
    }

    override fun toString(): String {
        return "Task(topicId=$topicId, name='$name', description='$description'," +
                " time=$time, interval=$interval, tags=$tags, users=$users," +
                " priority=$priority, isCompleted=$isCompleted, id=$id, localDBState=$localDBState," +
                " cloudDBState=$cloudDBState)"
    }
}
