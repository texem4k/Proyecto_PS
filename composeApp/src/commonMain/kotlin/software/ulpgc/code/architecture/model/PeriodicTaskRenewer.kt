package software.ulpgc.code.architecture.model

import software.ulpgc.code.architecture.io.DBState

class PeriodicTaskRenewer(
    private val timeFactory: TimeFactory,

) {
    fun renew(task: PeriodicTask) {
        val newTime = timeFactory.createTime(
            start = task.time.end,
            duration = task.interval.hours,
            taskId = task.id
        )

        task.time = newTime
        task.dbState = DBState.UPDATED
    }
}