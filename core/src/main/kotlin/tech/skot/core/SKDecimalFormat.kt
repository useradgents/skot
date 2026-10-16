@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package tech.skot.core

import java.util.Locale

/**
 * Format a decimal using pattern : Unicode Technical Standard #35
 * https://unicode.org/reports/tr35/tr35-numbers.html#Number_Format_Patterns
 */
expect class SKDecimalFormat(pattern: String, locale : Locale = Locale.getDefault()) {
    fun format(number: Number): String

    fun parse(str: String): Number?
}
