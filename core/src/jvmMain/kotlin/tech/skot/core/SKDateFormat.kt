@file:OptIn(ExperimentalTime::class)
package tech.skot.core

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

actual class SKDateFormat actual constructor(pattern: String, locale: TimeZone?) {
    private val sdf = SimpleDateFormat(pattern)

    actual fun format(instant: Instant): String {
        return sdf.format(Date(instant.toEpochMilliseconds()))
    }


    actual fun format(localDateTime: LocalDateTime): String {
        return format(localDateTime.toInstant(TimeZone.currentSystemDefault()))
    }

    actual fun format(localDate: LocalDate): String {
        return format(localDate.toLocalDateTime())
    }

    actual fun parse(str: String): Instant {
        return Instant.fromEpochMilliseconds(sdf.parse(str).time)
    }
}
