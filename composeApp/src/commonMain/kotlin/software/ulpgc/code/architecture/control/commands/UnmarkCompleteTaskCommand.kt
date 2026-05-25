package software.ulpgc.code.architecture.control.commands

import software.ulpgc.code.architecture.control.logs.LogMaster
import software.ulpgc.code.architecture.io.DBState
import software.ulpgc.code.architecture.io.Store
import software.ulpgc.code.architecture.io.isCloudDisabled
import software.ulpgc.code.architecture.model.tasks.Task

class UnmarkCompleteTaskCommand (val task: Task) : Command {

    override fun execute(): List<Command> {
        LogMaster.log("UnmarkCompleteTaskCommand {$task}")
        task.isCompleted = false
        task.localDBState = DBState.UPDATED
        if(!Store.currentGroup().isCloudDisabled()) task.cloudDBState = DBState.UPDATED
        setCompletionStat(task)
        return listOf(MarkCompleteTaskCommand(task))
    }

    private fun setCompletionStat(task: Task){
        val stat = Store.completions()
            .filter { it.taskId == task.id }
            .map{
                println("Medios filtros $it")
                it
            }
            .filter { it.completed }
            .map{
                println("Todos filtros $it")
                it
            }
            .maxBy { it.endDate }
        stat.completed = false
        stat.endDate = task.time.end
        stat.localDBState = DBState.UPDATED
        if(!Store.currentGroup().isCloudDisabled()) stat.cloudDBState = DBState.UPDATED
    }



}