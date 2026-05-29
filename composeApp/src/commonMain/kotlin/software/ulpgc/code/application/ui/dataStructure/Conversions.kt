package software.ulpgc.code.application.ui.dataStructure

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

fun Instant.toFormattedHour(timeZone: TimeZone = TimeZone.currentSystemDefault()): String {
    val localDateTime = this.toLocalDateTime(timeZone)
    return "${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}"
}

fun Instant.toFormattedDate(timeZone: TimeZone = TimeZone.currentSystemDefault()): String {
    val localDate = this.toLocalDateTime(timeZone).date
    return "${localDate.day.toString().padStart(2, '0')}${localDate.month.number.toString().padStart(2, '0')}${localDate.year}"
}

fun Instant.toFormattedDateDisplay(timeZone: TimeZone = TimeZone.currentSystemDefault()): String {
    val localDate = this.toLocalDateTime(timeZone).date
    val day = localDate.day.toString().padStart(2, '0')
    val month = localDate.month.number.toString().padStart(2, '0')
    val year = localDate.year
    return "$day/$month/$year"
}

fun createInstantFromDateAndHour(dateInstant: Instant, hora: String): Instant {
    val tz = TimeZone.currentSystemDefault()
    val localDate = dateInstant.toLocalDateTime(tz).date
    val parts = hora.split(':')
    val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
    return LocalDateTime(
        year = localDate.year,
        month = localDate.month,
        dayOfMonth = localDate.dayOfMonth,
        hour = hour,
        minute = minute,
        second = 0,
        nanosecond = 0
    ).toInstant(tz)
}