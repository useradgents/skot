@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
@file:OptIn(ExperimentalTime::class)

package tech.skot.core

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

expect class SKDateFormat(pattern: String) {
    fun format(instant: Instant): String

    fun format(localDateTime: LocalDateTime): String

    fun format(localDate: LocalDate): String

    fun parse(str: String): Instant
}

fun LocalDate.toLocalDateTime() =
    LocalDateTime(year = year,
        month = month,
        day = day,
        hour = 0,
        minute = 0,
        second = 0,
        nanosecond = 0
    )
