package io.aelf.portkey.component.storage

import io.fastkv.interfaces.FastCipher
import kotlin.experimental.xor

private const val XOR_KEY: Byte = 0x5A

object PortkeyDefaultCipher : FastCipher {
    override fun encrypt(src: ByteArray): ByteArray {
        for (i in src.indices) {
            src[i] = src[i].xor(XOR_KEY)
        }
        return src
    }

    override fun encrypt(src: Int): Int {
        return src.xor(XOR_KEY.toInt())
    }

    override fun encrypt(src: Long): Long {
        return src.xor(XOR_KEY.toLong())
    }

    override fun decrypt(dst: ByteArray): ByteArray {
        for (i in dst.indices) {
            dst[i] = dst[i].xor(XOR_KEY)
        }
        return dst
    }

    override fun decrypt(dst: Int): Int {
        return dst.xor(XOR_KEY.toInt())
    }

    override fun decrypt(dst: Long): Long {
        return dst.xor(XOR_KEY.toLong())
    }
}