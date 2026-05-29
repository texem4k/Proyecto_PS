package software.ulpgc.code.architecture.model.times

import io.ktor.util.reflect.instanceOf
import kotlinx.datetime.TimeZone
import software.ulpgc.code.application.ui.dataStructure.toFormattedDateDisplay
import software.ulpgc.code.application.ui.dataStructure.toFormattedHour
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

    fun mostrar(): String {
        val tz = TimeZone.currentSystemDefault()
        val startDate = start.toFormattedDateDisplay(tz)
        val startHour = start.toFormattedHour(tz)
        val endDate = end.toFormattedDateDisplay(tz)
        val endHour = end.toFormattedHour(tz)
        return "${startDate},${startHour},${endDate},${endHour}"
    }

    fun duration(): Duration {
        return end - start
    }

    fun timeUntilEnd(): Duration {
        return end - Clock.System.now()
    }

    override fun equals(other: Any?): Boolean {
        return this.toString() == other.toString()
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + start.hashCode()
        result = 31 * result + end.hashCode()
        result = 31 * result + type
        result = 31 * result + priorityModifier.hashCode()
        return result
    }
}