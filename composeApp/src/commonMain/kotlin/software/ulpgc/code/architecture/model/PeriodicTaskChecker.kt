package software.ulpgc.code.architecture.model

import software.ulpgc.code.architecture.io.DBState
import kotlin.time.Clock

object PeriodicTaskChecker {
    fun needsRenewal(task: PeriodicTask): Boolean {
        val now = Clock.System.now()
        return task.dbState != DBState.DELETED && task.time.end <= now
    }

    fun markAsDeleted(task: PeriodicTask) {
        task.dbState = DBState.DELETED
    }
}