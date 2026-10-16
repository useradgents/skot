package tech.skot.core

import timber.log.Timber

/** Printer SKot vers Timber, installé par [SKEnvInitProvider]. */
object TimberLogPrinter : SKLogPrinter.Printer {
    override fun d(tag: String, message: String) {
        Timber.tag(tag).d(message)
    }

    override fun i(tag: String, message: String) {
        Timber.tag(tag).i(message)
    }

    override fun v(tag: String, message: String) {
        Timber.tag(tag).v(message)
    }

    override fun w(tag: String, message: String) {
        Timber.tag(tag).w(message)
    }

    override fun e(e: Throwable, tag: String, message: String?) {
        Timber.tag(tag).e(Throwable(e), message)
    }
}
