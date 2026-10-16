package tech.skot.core

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Format a decimal using pattern : Unicode Technical Standard #35
 * https://unicode.org/reports/tr35/tr35-numbers.html#Number_Format_Patterns
 */
class SKDecimalFormat(pattern: String, locale: Locale = Locale.getDefault()) {
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
