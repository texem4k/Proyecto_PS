package software.ulpgc.code

import software.ulpgc.code.architecture.control.logs.Log
import software.ulpgc.code.architecture.control.logs.LogMaster
import software.ulpgc.code.architecture.control.logs.LogStorer
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class JavaLogStorer : LogStorer {
    private var writer: BufferedWriter? = null

    override fun openLogFile() {
        deleteOldFiles()
        val now = ZonedDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_VV")
        val fileName = now.format(formatter).replace("/", "-") + ".log"
        writer = BufferedWriter(FileWriter(fileName, true))
    }

    private fun deleteOldFiles() {
        val cutoff = LocalDate.now().minusDays(7)
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        File(".").listFiles { file ->
            file.isFile && file.name.endsWith(".log")
        }?.forEach { file ->
            val datePart = file.name.substringBefore("_") // "2026-04-11"
            runCatching {
                val fileDate = LocalDate.parse(datePart, dateFormatter)
                if (fileDate.isBefore(cutoff)) file.delete()
            }.onFailure { LogMaster.log("No se pudo parsear: ${file.name} — ${it.message}") }
        }
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