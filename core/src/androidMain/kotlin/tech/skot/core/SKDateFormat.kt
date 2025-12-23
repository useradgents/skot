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


actual class SKDateFormat actual constructor(pattern: String, val locale: TimeZone?) {
    private val sdf = SimpleDateFormat(pattern, Locale.getDefault()).apply {
        locale?.let { timeZone = java.util.TimeZone.getTimeZone(it.id) }
    }

    actual fun format(instant: Instant): String {
        return sdf.format(Date(instant.toEpochMilliseconds()))
    }

    actual fun format(localDateTime: LocalDateTime): String {
        return format(localDateTime.toInstant(TimeZone.currentSystemDefault()))
    }

    actual fun format(localDate: LocalDate): String {
        return format(localDate.toLocalDateTime())
    }

    @Throws(java.text.ParseException::class)
    actual fun parse(str: String): Instant {
        return Instant.fromEpochMilliseconds(sdf.parse(str).time)
    }
}
