package software.ulpgc.code.architecture.model

import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import kotlin.uuid.Uuid

class TimeFactory() {
    fun createTime(id: Uuid = Uuid.random(), start: Instant, duration: Double, taskId: Uuid): StartBasedInterval {
        return StartBasedInterval(id=id, start=start, end=(start+duration.hours), taskId=taskId)
    }
    fun createTime(id: Uuid = Uuid.random(), duration: Double, end: Instant, taskId: Uuid): EndBasedInterval {
        return EndBasedInterval(id=id, start=(end-duration.hours), end=end, taskId=taskId)
    }
    fun createTime(id: Uuid = Uuid.random(), start: Instant, end: Instant, taskId: Uuid): BoundedInterval {
        return BoundedInterval(id=id, start=start, end=end, taskId=taskId)
    }
}
