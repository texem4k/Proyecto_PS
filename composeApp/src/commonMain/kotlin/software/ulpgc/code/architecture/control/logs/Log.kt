package software.ulpgc.code.architecture.control.logs

import kotlinx.datetime.LocalDateTime

interface Log {

    val time: LocalDateTime
    val message: String
    override fun toString(): String
}