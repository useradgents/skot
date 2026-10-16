package tech.skot.core

import org.junit.After
import org.junit.Test
import kotlin.test.assertEquals

class TestSKLogPrinter {
    private val recorded = mutableListOf<String>()

    private val recordingPrinter =
        object : SKLogPrinter.Printer {
            override fun d(tag: String, message: String) { recorded += "d/$tag/$message" }
            override fun i(tag: String, message: String) { recorded += "i/$tag/$message" }
            override fun v(tag: String, message: String) { recorded += "v/$tag/$message" }
            override fun w(tag: String, message: String) { recorded += "w/$tag/$message" }
            override fun e(e: Throwable, tag: String, message: String?) { recorded += "e/$tag/$message/${e.message}" }
        }

    @After
    fun resetPrinter() {
        SKLogPrinter.printer = SKLogPrinter.StdoutPrinter
    }

    @Test
    fun `le printer par défaut est la sortie standard`() {
        assertEquals(SKLogPrinter.StdoutPrinter, SKLogPrinter.printer)
    }

    @Test
    fun `chaque niveau est délégué au printer installé`() {
        SKLogPrinter.printer = recordingPrinter
        SKLogPrinter.d("T", "debug")
        SKLogPrinter.i("T", "info")
        SKLogPrinter.v("T", "verbose")
        SKLogPrinter.w("T", "warn")
        SKLogPrinter.e(IllegalStateException("boom"), "T")
        assertEquals(
            listOf("d/T/debug", "i/T/info", "v/T/verbose", "w/T/warn", "e/T/null/boom"),
            recorded,
        )
    }
}
