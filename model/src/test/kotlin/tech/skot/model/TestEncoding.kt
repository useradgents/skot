package tech.skot.model

import org.junit.Test
import kotlin.test.assertEquals

class TestEncoding {
    @Test
    fun `encodeBase64 produit du base64 url-safe avec padding et sans retour à la ligne`() {
        assertEquals("c3ViamVjdHM_X2R-", encodeBase64("subjects?_d~"))
        assertEquals("aGVsbG8=", encodeBase64("hello"))
    }

    @Test
    fun `decodeBase64 url-safe accepte l'absence de padding`() {
        assertEquals("hello", decodeBase64("aGVsbG8"))
        assertEquals("hello", decodeBase64("aGVsbG8="))
        assertEquals("subjects?_d~", decodeBase64("c3ViamVjdHM_X2R-"))
    }

    @Test
    fun `decodeBase64 standard accepte l'alphabet standard et les retours à la ligne`() {
        assertEquals("subjects?_d~", decodeBase64("c3ViamVjdHM/X2R+", urlSafe = false))
        assertEquals("hello", decodeBase64("aGVs\nbG8=\n", urlSafe = false))
    }

    @Test
    fun `decodeBase64 ignore les caractères hors de l'alphabet comme Android`() {
        assertEquals("hello", decodeBase64("aGVs*bG8="))
        assertEquals("hello", decodeBase64("aGVs bG8"))
        assertEquals("hello", decodeBase64("aG+Vs/bG8="))
        assertEquals("hello", decodeBase64("aG-Vs_bG8=", urlSafe = false))
        assertEquals("hello", decodeBase64("aGVsbG9="))
    }

    @Test
    fun `hashSHA256 renvoie l'empreinte hexadécimale en majuscules`() {
        assertEquals("BA7816BF8F01CFEA414140DE5DAE2223B00361A396177A9CB410FF61F20015AD", hashSHA256("abc"))
    }

    @Test
    fun `aes128encrypt chiffre en AES-CBC avec une clé complétée à 32 octets`() {
        assertEquals("/KCvLepb2+9Qh+IilwL0yQ==", aes128encrypt("hello world", "secret", "0123456789abcdef"))
    }

    @Test
    fun `encodeBase64 puis decodeBase64 restitue la chaîne`() {
        val text = "Été à l'école ? ~ / + _ -"
        assertEquals(text, decodeBase64(encodeBase64(text)))
    }
}
