package software.ulpgc.code.architecture.model.tasks

import software.ulpgc.code.architecture.io.DBObject
import software.ulpgc.code.architecture.io.DBState
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

data class CompletionStat (
    val taskId: Uuid,
    val proposedDate: Instant,
    var completed: Boolean,
    var endDate: Instant = Clock.System.now(),
    val id: Uuid = Uuid.random(),
    override var dbState: DBState = DBState.NEW
): DBObject