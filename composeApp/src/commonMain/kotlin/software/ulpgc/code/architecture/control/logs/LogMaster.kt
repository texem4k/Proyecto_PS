package software.ulpgc.code.architecture.control.logs

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import software.ulpgc.code.architecture.control.coroutines.Coroutinable
import software.ulpgc.code.architecture.control.coroutines.CoroutineManager
import kotlin.time.Clock

object LogMaster : Coroutinable {
    var storer: LogStorer? = null
    fun startLogger(storer: LogStorer) {
        CoroutineManager.add(this)
        this.storer = storer
    }

    fun log(msg: String) {
        logs.add(MessageLog(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()), msg))
    }

    fun addLog(log: Log) {
        logs.add(log)
    }

    override val delayMilis: Long = 60_000L

    override suspend fun onInit() {
        storer?.openLogFile()
    }

    private val logs: MutableList<Log> = mutableListOf()
    override suspend fun execute() {
        storer?.addLogs(logs)
        logs.clear()
    }

    override suspend fun onDispose() {
        execute()
        storer?.closeLogFile()
    }

}