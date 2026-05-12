package software.ulpgc.code.architecture.model.tasks

import software.ulpgc.code.architecture.io.DBObject
import software.ulpgc.code.architecture.io.DBState
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class CompletionStat (
    val taskId: Uuid,
    var completed: Boolean,
    var date: Instant = Clock.System.now(),
    override var dbState: DBState = DBState.NEW
): DBObject {

}