package com.github.nikvoloshin.exposed.verification.encoding

import com.github.nikvoloshin.exposed.verification.utils.toHex
import javax.crypto.Mac
import javax.crypto.SecretKey

internal class MacEncoder(internal var secretKey: SecretKey) {
    private val mac get() = Mac.getInstance(secretKey.algorithm).apply { init(secretKey) }

    fun encode(data: ByteArray): ByteArray = mac.doFinal(data)

    fun encode(first: String, vararg data: String) =
        data.fold(encode(first.toByteArray())) { hash, elem -> encode(hash + elem.toByteArray()) }.toHex()

    fun encode(data: List<String>) = encode(data.first(), *data.drop(1).toTypedArray())
}
