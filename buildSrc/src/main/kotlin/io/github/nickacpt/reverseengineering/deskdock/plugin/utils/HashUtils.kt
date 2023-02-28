package io.github.nickacpt.reverseengineering.deskdock.plugin.utils

import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.readBytes

object HashUtils {
    private fun ByteArray.toHex() = joinToString(separator = "") { byte -> "%02x".format(byte) }
    private val md5Digest: MessageDigest = MessageDigest.getInstance("MD5")

    fun getFileHash(path: Path): String {
        return md5Digest.digest(path.readBytes()).toHex()
    }
}