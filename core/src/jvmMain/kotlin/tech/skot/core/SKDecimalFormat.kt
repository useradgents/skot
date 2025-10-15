@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package tech.skot.core

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

actual class SKDecimalFormat actual constructor(pattern: String, locale: Locale) {
    private val df = DecimalFormat(pattern).apply {
        decimalFormatSymbols = DecimalFormatSymbols.getInstance(locale)
    }

    actual fun format(number: Number): String {
        return df.format(number)
    }

    actual fun parse(str: String): Number? {
        return df.parse(str)
    }
}
