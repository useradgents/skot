@file:Suppress("SameParameterValue")

package tech.skot.model

import kotlin.io.encoding.Base64
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

private const val HEX_CHARS = "0123456789ABCDEF"

private val urlSafeDecoder = Base64.UrlSafe.withPadding(Base64.PaddingOption.PRESENT_OPTIONAL)
private val defaultDecoder = Base64.Default.withPadding(Base64.PaddingOption.PRESENT_OPTIONAL)

/**
 * Comme `android.util.Base64.decode` : les blancs sont ignorés et le padding est facultatif.
 */
fun decodeBase64(
    str: String,
    urlSafe: Boolean = true,
): String =
    String(
        (if (urlSafe) urlSafeDecoder else defaultDecoder)
            .decode(str.filterNot { it.isWhitespace() }),
    )

/** Base64 url-safe avec padding, sans retour à la ligne (`NO_WRAP or URL_SAFE` d'Android). */
fun encodeBase64(str: String): String = Base64.UrlSafe.encode(str.toByteArray())

fun hashSHA256(str: String) : String = hashString("SHA-256", str)

private fun hashString(
    type: String,
    input: String,
): String {

    val bytes =
        MessageDigest
            .getInstance(type)
            .digest(input.toByteArray())
    val result = StringBuilder(bytes.size * 2)

    bytes.forEach {
        val i = it.toInt()
        result.append(HEX_CHARS[i shr 4 and 0x0f])
        result.append(HEX_CHARS[i and 0x0f])
    }

    return result.toString()
}

fun aes128encrypt(
    textToEncrypt: String,
    secret: String,
    initializationVector: String,
): String {
    val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
    val keySpec = SecretKeySpec(secret.toByteArray().copyOf(32), "AES")
    val ivSpec = IvParameterSpec(initializationVector.toByteArray())
    cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
    return Base64.Default.encode(cipher.doFinal(textToEncrypt.toByteArray()))
}
