@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package tech.skot.core

import java.text.DecimalFormat


actual class SKDecimalFormat actual constructor(pattern: String) {
    private val df = DecimalFormat(pattern)
    actual fun format(number: Number): String {
        return df.format(number)
    }

    actual fun parse(str: String): Number? {
        return df.parse(str)
    }
}