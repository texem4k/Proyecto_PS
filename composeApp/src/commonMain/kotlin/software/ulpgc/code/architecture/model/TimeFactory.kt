package software.ulpgc.code.architecture.model
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class TimeFactory() {
    fun createTime(start: Instant, end: Instant, taskId: Uuid): Time {
        return BoundedInterval(start = start, end = end, taskId = taskId)
    }

    fun createTime(start: Instant, duration: Instant, taskId: Uuid): Time {
        return StartBasedInterval(start = start, duration: duration, taskId = taskId)
    }

    fun createTime(end: Instant, duration: Instant): Time {}
}