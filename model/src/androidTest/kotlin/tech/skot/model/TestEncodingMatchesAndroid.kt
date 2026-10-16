package tech.skot.model

import android.util.Base64
import org.junit.Test
import kotlin.random.Random
import kotlin.test.assertEquals

class TestEncodingMatchesAndroid {
    private val samples =
        listOf("", "a", "ab", "abc", "subjects?_d~", "Été à l'école ? ~ / + _ -") +
            List(50) { index ->
                val r = Random(index)
                String(CharArray(index * 7) { r.nextInt(32, 0x24F).toChar() })
            }

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

    @Test
    fun decodeBase64_identique_a_android_sur_entrees_malformees() {
        listOf("aGVs*bG8=", "aGVs!bG8=", "aGVs bG8", "aGVs\nbG8=\n", "aG+Vs/bG8=", "aG-Vs_bG8=", "aGVsbG8", "aGVsbG8=", "c3ViamVjdHM/X2R+", "c3ViamVjdHM_X2R-")
            .forEach {
                assertEquals(androidDecode(it, Base64.URL_SAFE), runCatching { decodeBase64(it, urlSafe = true) }.getOrNull(), it)
                assertEquals(androidDecode(it, Base64.DEFAULT), runCatching { decodeBase64(it, urlSafe = false) }.getOrNull(), it)
            }
    }

    /** `null` quand Android rejette l'entrée (`IllegalArgumentException`), ce que fait aussi `decodeBase64`. */
    private fun androidDecode(
        input: String,
        flags: Int,
    ): String? = runCatching { String(Base64.decode(input, flags)) }.getOrNull()
}
