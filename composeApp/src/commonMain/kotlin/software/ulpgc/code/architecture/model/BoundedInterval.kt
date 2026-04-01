package software.ulpgc.code.architecture.model

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant
import kotlin.uuid.Uuid

class BoundedInterval(
    id: Uuid = Uuid.random(),
    start: Instant,
    end: Instant,
    taskId: Uuid
) : Time(id, start, end, taskId) {
    override val type: Int = 3
}