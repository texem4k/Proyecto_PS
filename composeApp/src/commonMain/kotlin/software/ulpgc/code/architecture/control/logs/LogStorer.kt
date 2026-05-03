package software.ulpgc.code.architecture.control.logs

interface LogStorer {
    fun openLogFile()
    fun closeLogFile()
    fun addLogs(logs: List<Log>)
}