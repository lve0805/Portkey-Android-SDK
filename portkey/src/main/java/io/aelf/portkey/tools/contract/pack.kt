package io.aelf.portkey.tools.contract

import aelf.Core.Hash
import com.google.protobuf.ByteString
import io.aelf.utils.ByteArrayHelper
import io.aelf.utils.Sha256

/**
 * Convert a String to AElf's Hash.
 * @param hashAgain If the string is already a hash, set this to false.(Default true)
 */
fun String.toAElfHash(hashAgain: Boolean = true): Hash {
    return Hash.newBuilder().setValue(
        ByteString.copyFrom(if (hashAgain) Sha256.getBytesSha256(this) else this.toByteArray())
    ).build()
}

/**
 * Convert a String to ByteArray by AElf's way.
 */
fun String.toAElfBytes(): ByteArray {
    return ByteArrayHelper.hexToByteArray(this)
}