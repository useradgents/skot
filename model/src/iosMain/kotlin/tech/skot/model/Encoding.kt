package tech.skot.model

actual fun decodeBase64(
    str: String,
    urlSafe: Boolean,
): String = TODO()

actual fun encodeBase64(str: String): String = TODO()

actual fun hashSHA256(str: String): String = TODO()

actual fun aes128encrypt(
    textToEncrypt: String,
    secret: String,
    initializationVector: String,
): String = TODO()
