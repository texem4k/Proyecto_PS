package software.ulpgc.code.architecture.control.logs

import kotlinx.datetime.LocalDateTime

data class Log (val time: LocalDateTime, val message: String) {

    override fun toString(): String {
        return "[$time]: '$message'"
    }
}