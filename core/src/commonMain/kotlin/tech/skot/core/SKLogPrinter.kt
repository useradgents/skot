@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package tech.skot.core

expect object SKLogPrinter {
    fun d(
        tag: String,
        message: String,
    )

    fun i(
        tag: String,
        message: String,
    )

    fun v(
        tag: String,
        message: String,
    )

    fun w(
        tag: String,
        message: String,
    )

    fun e(
        e: Throwable,
        tag: String,
        message: String? = null,
    )
}
