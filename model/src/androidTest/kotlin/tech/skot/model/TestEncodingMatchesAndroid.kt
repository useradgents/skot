package tech.skot.model

import android.util.Base64
import org.junit.Test
import kotlin.random.Random
import kotlin.test.assertEquals

class TestEncodingMatchesAndroid {
    private val samples =
        listOf("", "a", "ab", "abc", "subjects?_d~", "Été à l'école ? ~ / + _ -") +
            List(50) { index -> String(CharArray(index * 7) { Random(index).nextInt(32, 0x24F).toChar() }) }

    @Test
    fun encodeBase64_identique_a_android() {
        samples.forEach {
            assertEquals(Base64.encodeToString(it.toByteArray(), Base64.NO_WRAP or Base64.URL_SAFE), encodeBase64(it), it)
        }
    }

    @Test
    fun decodeBase64_identique_a_android() {
        samples.forEach {
            val urlSafe = Base64.encodeToString(it.toByteArray(), Base64.URL_SAFE)
            val standard = Base64.encodeToString(it.toByteArray(), Base64.DEFAULT)
            assertEquals(String(Base64.decode(urlSafe, Base64.URL_SAFE)), decodeBase64(urlSafe), it)
            assertEquals(String(Base64.decode(standard, Base64.DEFAULT)), decodeBase64(standard, urlSafe = false), it)
        }
    }
}
