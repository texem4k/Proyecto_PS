package software.ulpgc.code.architecture.control.optimizer

import software.ulpgc.code.architecture.control.coroutines.Coroutinable
import software.ulpgc.code.architecture.control.coroutines.CoroutineManager
import software.ulpgc.code.architecture.io.Storage
import software.ulpgc.code.architecture.model.tasks.Task
import software.ulpgc.code.architecture.model.times.BoundedTime
import software.ulpgc.code.architecture.model.times.EndBasedTime
import software.ulpgc.code.architecture.model.times.StartBasedTime
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

object TaskOptimizer: Coroutinable {
    lateinit var sortedTasks: Sequence<Task>
        private set

    fun getOptimized(duration: Duration): List<TimeFrame> {
        val now = Clock.System.now()
        val windowEnd = now + duration

        val placed = mutableListOf<TimeFrame>()
        val gaps = mutableListOf<Pair<Instant, Instant>>()
        val flexible = mutableListOf<Task>()
        var cursor = now

        // Pass 1: place constrained tasks in significance order, defer the flexible onesº
        for (task in sortedTasks) {
            when (val time = task.time) {
                is BoundedTime -> {
                    flexible += task
                }

                is EndBasedTime -> {
                    // Hard constraint: must finish by time.end. Can start whenever.
                    val taskEnd = cursor + time.duration()
                    if (taskEnd <= windowEnd && taskEnd <= time.end) {
                        placed += TimeFrame(task, cursor, taskEnd)
                        cursor = taskEnd
                    }
                }

                is StartBasedTime -> {
                    // Hard constraint: must start at/after time.start. Can finish past time.end.
                    val actualStart = maxOf(cursor, time.start)
                    val taskEnd = actualStart + time.duration()
                    if (taskEnd <= windowEnd) {
                        if (actualStart > cursor) gaps += cursor to actualStart
                        placed += TimeFrame(task, actualStart, taskEnd)
                        cursor = taskEnd
                    }
                }
            }
        }

        // Trailing space after the last constrained task is also a gap
        if (cursor < windowEnd) gaps += cursor to windowEnd

        // Pass 2: fit BoundedTime tasks into gaps, still in significance order
        val gapCursors = gaps.map { it.first }.toMutableList()
        val gapEnds = gaps.map { it.second }
        val gapFills = mutableListOf<TimeFrame>()

        for (task in flexible) {
            val taskDuration = task.time.duration()
            for (i in gapCursors.indices) {
                val start = gapCursors[i]
                val end = start + taskDuration
                if (end <= gapEnds[i]) {
                    gapFills += TimeFrame(task, start, end)
                    gapCursors[i] = end
                    break
                }
            }
        }

        return (placed + gapFills).sortedBy { it.start }
    }

    lateinit var store: Storage

    override val delayMilis: Long = 15_000L

    fun setUp(store: Storage) {
        this.store = store
        CoroutineManager.add(this)
    }

    override suspend fun onInit() {
        execute()
    }

    override suspend fun execute() {
        updateTimeFramesFor(store.tasks().filterNot(Task::isCompleted).filterNot{task -> task.time.hasFinished()})
    }

    private fun updateTimeFramesFor(tasks: Sequence<Task>) {
        this.sortedTasks = tasks.sortedBy { task -> - task.significanceFactor() * task.time.priorityModifier }
    }

    override suspend fun onDispose() {
        return
    }
}