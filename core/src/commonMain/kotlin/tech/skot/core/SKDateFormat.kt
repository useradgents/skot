@file:OptIn(ExperimentalTime::class)

package tech.skot.core

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


class SKDateFormat constructor(pattern: String, formatTimeZone: TimeZone?=null) {
    private val sdf = SimpleDateFormat(pattern, Locale.getDefault()).apply {
        formatTimeZone?.let {
            timeZone = java.util.TimeZone.getTimeZone(it.id)
        }
    }

    fun format(instant: Instant): String {
        return sdf.format(Date(instant.toEpochMilliseconds()))
    }

    fun format(localDateTime: LocalDateTime): String {
        return format(localDateTime.toInstant(TimeZone.currentSystemDefault()))
    }

    fun format(localDate: LocalDate): String {
        return format(localDate.toLocalDateTime())
    }

    @Throws(java.text.ParseException::class)
    fun parse(str: String): Instant {
        return Instant.fromEpochMilliseconds(sdf.parse(str).time)
    }
}


fun LocalDate.toLocalDateTime() =
    LocalDateTime(
        year = year,
        month = month,
        day = day,
        hour = 0,
        minute = 0,
        second = 0,
        nanosecond = 0
    )
