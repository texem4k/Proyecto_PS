package software.ulpgc.code.architecture.model

import software.ulpgc.code.architecture.io.DBState

object PeriodicTaskRenewer {
    fun renew(task: PeriodicTask) {
        val newTime = TimeFactory.createTime(
            start = task.time.end,
            duration = task.interval.hours,
            taskId = task.id
        )

        task.time = newTime
        task.dbState = DBState.UPDATED
    }
}