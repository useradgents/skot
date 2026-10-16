package tech.skot.core

/**
 * Point de sortie des logs SKot. Imprime sur la sortie standard par défaut ; côté Android,
 * `SKEnvInitProvider` (coreAndroid) installe un printer Timber avant `Application.onCreate()`.
 */
object SKLogPrinter {
    interface Printer {
        fun d(tag: String, message: String)

        fun i(tag: String, message: String)

        fun v(tag: String, message: String)

        fun w(tag: String, message: String)

        fun e(e: Throwable, tag: String, message: String?)
    }

    object StdoutPrinter : Printer {
        override fun d(tag: String, message: String) = println("$tag d ---$message")

        override fun i(tag: String, message: String) = println("$tag i ---$message")

        override fun v(tag: String, message: String) = println("$tag v ---$message")

        override fun w(tag: String, message: String) = println("$tag w ---$message")

        override fun e(e: Throwable, tag: String, message: String?) {
            println("$tag e ---${message ?: e.message ?: ""}")
            e.printStackTrace()
        }
    }

    @Volatile
    var printer: Printer = StdoutPrinter

    fun d(
        tag: String,
        message: String,
    ) = printer.d(tag, message)

    fun i(
        tag: String,
        message: String,
    ) = printer.i(tag, message)

    fun v(
        tag: String,
        message: String,
    ) = printer.v(tag, message)

    fun w(
        tag: String,
        message: String,
    ) = printer.w(tag, message)

    fun e(
        e: Throwable,
        tag: String,
        message: String? = null,
    ) = printer.e(e, tag, message)
}
