package software.ulpgc.code.architecture.control.optimizer

import software.ulpgc.code.architecture.model.tasks.Task
import kotlin.time.Instant

data class TimeFrame(val task: Task, val start: Instant, val end: Instant)
