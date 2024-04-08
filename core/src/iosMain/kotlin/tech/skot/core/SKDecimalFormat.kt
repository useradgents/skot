@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package tech.skot.core


actual class SKDecimalFormat actual constructor(pattern: String) {
    actual fun format(number: Number): String {
        TODO()
    }

    actual fun parse(str: String): Number? {
        TODO()
    }
}