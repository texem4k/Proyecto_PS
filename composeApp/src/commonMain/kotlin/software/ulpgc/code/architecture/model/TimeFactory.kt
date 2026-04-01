package software.ulpgc.code.architecture.model

import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import kotlin.uuid.Uuid

class TimeFactory() {
    fun createTime(start: Instant, duration: Double, taskid: Uuid): StartBasedInterval {
        return StartBasedInterval(start=start, end=(start+duration.hours), taskId=taskid)
    }
    fun createTime(duration: Double, end: Instant, taskid: Uuid): EndBasedInterval {
        return EndBasedInterval(start=(end-duration.hours), end=end, taskId=taskid)
    }
    fun createTime(start: Instant, end: Instant, taskid: Uuid): BoundedInterval {
        return BoundedInterval(start=start, end=end, taskId=taskid)
    }
}
