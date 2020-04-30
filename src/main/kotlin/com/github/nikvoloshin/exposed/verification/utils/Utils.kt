package com.github.nikvoloshin.exposed.verification.utils

import kotlin.random.Random

internal fun Random.randomSalt64() = randomSalt(64)

internal fun Random.randomSalt(n: Int) = nextBytes(n).toHex()

internal fun ByteArray.toHex(): String {
    val chars = CharArray(2 * size)
    for (i in 0 until size) {
        chars[2 * i] = HEX_CHARS[(this[i].toInt() and 0xF0) ushr 4]
        chars[2 * i + 1] = HEX_CHARS[this[i].toInt() and 0x0F]
    }
    return String(chars)
}

private const val HEX_CHARS = "0123456789abcdef"
