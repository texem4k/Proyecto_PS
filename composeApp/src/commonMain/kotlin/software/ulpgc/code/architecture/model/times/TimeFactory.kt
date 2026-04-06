package software.ulpgc.code.architecture.model.times

import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import kotlin.uuid.Uuid

class TimeFactory() {
    fun createTime(
        start: Instant,
        end: Instant,
        type: Int,
        id: Uuid
    ): Time {
        return when (type) {
            0 -> createTime(start, (end-start).inWholeHours, id)
            1 -> createTime((end-start).inWholeHours, end, id)
            2 -> createTime(start, end, id)
            else -> createTime(Clock.System.now(), 1L)
        }
    }

    fun parse(time: String): Time {
        val (type, start , end, id) = time.split(", ")
        return createTime(Instant.parse(start), Instant.parse(end), type.toInt(), Uuid.parse(id))
    }
    fun createTime(
        start: Instant,
        durationHours: Long,
        id: Uuid = Uuid.random()
    ): StartBasedTime = StartBasedTime(id, start, (start + durationHours.hours))

    fun createTime(
        durationHours: Long,
        end: Instant,
        id: Uuid = Uuid.random()
    ): EndBasedTime = EndBasedTime(id, (end - durationHours.hours), end)

    fun createTime(
        start: Instant,
        end: Instant,
        id: Uuid = Uuid.random()
    ): BoundedTime = BoundedTime(id, start, end)
}