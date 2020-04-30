package com.github.nikvoloshin.exposed.verification.encoding

import javax.crypto.Mac
import javax.crypto.SecretKey

internal class MACEncoder(private val secretKey: SecretKey) : HexEncoder() {
    override val codeLengthBytes: Int

    private val mac get() = Mac.getInstance(secretKey.algorithm).apply { init(secretKey) }

    init {
        codeLengthBytes = mac.macLength
    }

    override fun encode(data: ByteArray): ByteArray = mac.doFinal(data)

}
