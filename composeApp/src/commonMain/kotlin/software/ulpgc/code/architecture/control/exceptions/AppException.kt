package software.ulpgc.code.architecture.control.exceptions

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import software.ulpgc.code.architecture.control.logs.Log
import software.ulpgc.code.architecture.control.logs.LogMaster
import kotlin.time.Clock

open class AppException internal constructor (override val time: LocalDateTime, msg: String) :
    Exception(msg), Log {

    constructor(msg: String) : this(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()), msg)

    init {
        LogMaster.addLog(this)
    }

    override fun toString(): String {
        return "**AppException** [$time]: '$message'"
    }

    override val message: String = msg
}