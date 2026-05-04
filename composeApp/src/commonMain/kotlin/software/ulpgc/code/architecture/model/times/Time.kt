package software.ulpgc.code.architecture.model.times

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant
import kotlin.uuid.Uuid

abstract class Time internal constructor(
    val id: Uuid,
    var start: Instant,
    var end: Instant,
) {
    abstract val type: Int

    abstract fun hasFinished(): Boolean
    abstract val priorityModifier: Double

    override fun toString(): String {
        return "$type, $start, $end, $id"
    }

    fun duration(): Duration {
        return end - start
    }

    fun timeUntilEnd(): Duration {
        return end - Clock.System.now()
    }
}