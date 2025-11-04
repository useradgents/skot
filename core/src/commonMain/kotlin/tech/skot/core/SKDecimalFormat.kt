@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package tech.skot.core

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class SKDecimalFormat(pattern: String, locale: Locale) {
    private val df = DecimalFormat(pattern).apply {
        decimalFormatSymbols = DecimalFormatSymbols.getInstance(locale)
    }

    fun format(number: Number): String {
        return df.format(number)
    }

    fun parse(str: String): Number? {
        return df.parse(str)
    }
}