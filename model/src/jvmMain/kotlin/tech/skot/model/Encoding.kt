@file:Suppress("SameReturnValue", )

package tech.skot.model

import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
actual fun decodeBase64(
    str: String,
    urlSafe: Boolean,
): String {
    return "TODO decodeBase64!!"
}

actual fun encodeBase64(str: String) = "TODO encodeBase64!!"

actual fun hashSHA256(str: String): String = "TODO hashSHA256!!"

actual fun aes128encrypt(
    textToEncrypt: String,
    secret: String,
    initializationVector: String,
): String = "TODO aes128encrypt!!"
