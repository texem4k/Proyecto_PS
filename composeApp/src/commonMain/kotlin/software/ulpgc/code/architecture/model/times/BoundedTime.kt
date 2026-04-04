package software.ulpgc.code.architecture.model.times

import kotlin.time.Instant
import kotlin.uuid.Uuid

class BoundedTime internal constructor(
    id: Uuid,
    start: Instant,
    end: Instant,
) : Time(id, start, end) {
    override val type: Int = 2
}