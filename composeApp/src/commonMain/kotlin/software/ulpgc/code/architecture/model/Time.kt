package software.ulpgc.code.architecture.model

import kotlin.time.Instant
import kotlin.uuid.Uuid

abstract class Time(

    val id: Uuid = Uuid.random(),
    var start: Instant,
    var end: Instant,
    var taskId: Uuid
) {
    abstract val type: Int
}