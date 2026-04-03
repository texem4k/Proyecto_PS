package software.ulpgc.code.architecture.model

import kotlin.time.Instant
import kotlin.uuid.Uuid

class StartBasedInterval(
    id: Uuid,
    start: Instant,
    end: Instant,
    taskId: Uuid
) : Time(id, start, end, taskId) {
    override val type: Int = 1
}