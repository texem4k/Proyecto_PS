package software.ulpgc.code.architecture.control.logs

import kotlinx.datetime.LocalDateTime

class MessageLog (override val time: LocalDateTime, override val message: String): Log {
    override fun toString(): String {
        return "[$time]: '$message'"
    }
}