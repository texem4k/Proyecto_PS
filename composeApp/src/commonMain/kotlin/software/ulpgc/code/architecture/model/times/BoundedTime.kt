package software.ulpgc.code.architecture.model.times

import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class BoundedTime internal constructor(
    id: Uuid,
    start: Instant,
    end: Instant,
) : Time(id, start, end) {
    override val type: Int = 2

    override val priorityModifier: Double = 1.25

    override fun hasFinished(): Boolean {
        return Clock.System.now() >= end
    }
}