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

private const val ALPHABET_COMMON = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
private const val ALPHABET_URL_SAFE = ALPHABET_COMMON + "-_"
private const val ALPHABET_DEFAULT = ALPHABET_COMMON + "+/"

/**
 * Comme `android.util.Base64.decode` : les caractères hors de l'alphabet choisi (blancs, retours à la ligne,
 * caractères de l'autre alphabet, `*`...) sont ignorés au lieu de lever une exception, le padding est facultatif
 * et les bits de poids faible non significatifs du dernier caractère sont ignorés (le décodeur Kotlin les refuse).
 */
fun decodeBase64(
    str: String,
    urlSafe: Boolean = true,
): String {
    val alphabet = if (urlSafe) ALPHABET_URL_SAFE else ALPHABET_DEFAULT
    val filtered = str.filter { it in alphabet || it == '=' }
    val body = filtered.trimEnd('=')
    val padding = filtered.substring(body.length)
    val clearedBits =
        when (body.length % 4) {
            2 -> 0x30
            3 -> 0x3C
            else -> 0x3F
        }
    val last = body.lastOrNull()?.let { alphabet[alphabet.indexOf(it) and clearedBits] }
    val normalized = if (last == null) filtered else body.dropLast(1) + last + padding
    return String((if (urlSafe) urlSafeDecoder else defaultDecoder).decode(normalized))
}

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
