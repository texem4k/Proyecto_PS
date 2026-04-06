package software.ulpgc.code.architecture.model.tasks

import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant
import kotlinx.datetime.Instant as DatetimeInstant

enum class TaskInterval {
    NONE, DAY, WEEK, MONTH, YEAR;

    operator fun plus(instant: Instant): Instant {
        val timezone = TimeZone.currentSystemDefault()
        return when (this) {
            NONE  -> instant
            DAY   -> instant.plus(1.days)
            WEEK  -> instant.plus(7.days)
            MONTH -> instant.toDatetime().plus(DateTimePeriod(months = 1), timezone).toKotlin()
            YEAR  -> instant.toDatetime().plus(DateTimePeriod(years = 1), timezone).toKotlin()
        }
    }

    private fun Instant.toDatetime(): DatetimeInstant =
        DatetimeInstant.fromEpochMilliseconds(this.toEpochMilliseconds())

    private fun DatetimeInstant.toKotlin(): Instant =
        Instant.fromEpochMilliseconds(this.toEpochMilliseconds())
}