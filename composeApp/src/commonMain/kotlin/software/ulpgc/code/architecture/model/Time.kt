package software.ulpgc.code.architecture.model

import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

abstract class Time(
    val id: Uuid = Uuid.random(),
    var start: Instant = Clock.System.now(),
    var end: Instant,
    var taskId: Uuid
)