package software.ulpgc.code.architecture.model

import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import kotlin.uuid.Uuid

class TimeFactory() {
    fun createTime(id: Uuid = Uuid.random(), start: Instant, duration: Double, taskid: Uuid): StartBasedInterval {
        return StartBasedInterval(id=id, start=start, end=(start+duration.hours), taskId=taskid)
    }
    fun createTime(id: Uuid = Uuid.random(), duration: Double, end: Instant, taskid: Uuid): EndBasedInterval {
        return EndBasedInterval(id=id, start=(end-duration.hours), end=end, taskId=taskid)
    }
    fun createTime(id: Uuid = Uuid.random(), start: Instant, end: Instant, taskid: Uuid): BoundedInterval {
        return BoundedInterval(id=id, start=start, end=end, taskId=taskid)
    }
}
