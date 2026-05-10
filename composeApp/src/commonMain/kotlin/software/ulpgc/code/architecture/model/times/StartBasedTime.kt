package software.ulpgc.code.architecture.model.times

import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class StartBasedTime internal constructor(
    id: Uuid,
    start: Instant,
    end: Instant,
) : Time(id, start, end) {
    override val type: Int = 0

    override val priorityModifier: Double = 1.0

    override fun hasFinished(): Boolean {
        return false
    }
}