package software.ulpgc.code.architecture.model

import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class BoundedInterval(
    id: Uuid = Uuid.random(),
    start: Instant = Clock.System.now(),
    end: Instant,
    taskId: Uuid
) : Time(id, start, end, taskId)