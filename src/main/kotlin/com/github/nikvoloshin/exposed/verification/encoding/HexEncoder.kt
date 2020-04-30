package com.github.nikvoloshin.exposed.verification.encoding

import com.github.nikvoloshin.exposed.verification.utils.toHex

internal abstract class HexEncoder {
    abstract val codeLengthBytes: Int
    val codeLength get() = 2 * codeLengthBytes

    fun encode(first: Any?, vararg data: Any?): String =
        data.fold(encode(first)) { acc, elem -> encode(acc, encode(elem)) }

    fun encode(data: String) = encode(data.toByteArray()).toHex()

    abstract fun encode(data: ByteArray): ByteArray

    fun verify(data: String, encoded: String) = encoded == encode(data)

}