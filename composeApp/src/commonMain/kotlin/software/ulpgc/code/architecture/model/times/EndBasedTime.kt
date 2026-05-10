package software.ulpgc.code.architecture.model.times

import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class EndBasedTime internal constructor(
    id: Uuid,
    start: Instant,
    end: Instant,
) : Time(id, start, end) {
    override val type: Int = 1

    override val priorityModifier: Double = 1.5

    override fun hasFinished(): Boolean {
        return Clock.System.now() >= end
    }
}