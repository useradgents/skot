@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package tech.skot.core

import kotlinx.datetime.*

actual class SKDateFormat actual constructor(pattern: String) {
    actual fun format(instant: Instant): String {
        TODO()
    }

    actual fun format(localDateTime: LocalDateTime): String {
        TODO()
    }

    actual fun format(localDate: LocalDate): String {
        TODO()
    }

    actual fun parse(str: String): Instant {
        TODO()
    }
}
