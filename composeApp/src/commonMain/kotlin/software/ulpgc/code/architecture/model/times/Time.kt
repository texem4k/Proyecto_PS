package software.ulpgc.code.architecture.model.times

import kotlin.time.Instant
import kotlin.uuid.Uuid

abstract class Time internal constructor(
    val id: Uuid,
    var start: Instant,
    var end: Instant,
) {
    abstract val type: Int
}