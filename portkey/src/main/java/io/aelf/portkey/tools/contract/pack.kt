package io.aelf.portkey.tools.contract

import aelf.Core.Hash
import com.google.protobuf.ByteString
import io.aelf.utils.ByteArrayHelper
import io.aelf.utils.Sha256

/**
 * Convert a String to AElf's Hash.
 */
fun String.toAElfHash(): Hash {
    return Hash.newBuilder().setValue(
        ByteString.copyFrom(Sha256.getBytesSha256(this))
    ).build()
}

/**
 * Convert a String to ByteArray by AElf's way.
 */
fun String.toAElfBytes(): ByteArray {
    return ByteArrayHelper.hexToByteArray(this)
}