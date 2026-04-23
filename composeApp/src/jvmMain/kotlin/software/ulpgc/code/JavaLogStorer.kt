package software.ulpgc.code

import software.ulpgc.code.architecture.control.logs.Log
import software.ulpgc.code.architecture.control.logs.LogStorer
import java.io.BufferedWriter
import java.io.FileWriter
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class JavaLogStorer : LogStorer {
    private var writer: BufferedWriter? = null

    override fun openLogFile() {
        val now = ZonedDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_VV")
        val fileName = now.format(formatter).replace("/", "-") + ".log"
        writer = BufferedWriter(FileWriter(fileName, true))
    }

    override fun closeLogFile() {
        writer?.close()
        writer = null
    }

    override fun addLogs(logs: List<Log>) {
        val w = writer ?: return
        for (log in logs) {
            w.write(log.toString())
            w.newLine()
        }
        w.flush()
    }
}