package com.financeapp.core.utils

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PinHasher {
    private const val ITERATIONS = 100_000
    private const val SALT_LENGTH = 16
    private const val KEY_LENGTH = 256

    fun hash(pin: String): String {
        val salt = ByteArray(SALT_LENGTH).also { SecureRandom().nextBytes(it) }
        val hash = pbkdf2(pin.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        return formatBytes(salt) + ":" + formatBytes(hash)
    }

    fun verify(pin: String, stored: String): Boolean {
        return try {
            if (":" !in stored) {
                verifyLegacy(pin, stored)
            } else {
                val (saltHex, hashHex) = stored.split(":", limit = 2)
                val salt = parseBytes(saltHex)
                val storedHash = parseBytes(hashHex)
                val computedHash = pbkdf2(pin.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
                MessageDigest.isEqual(storedHash, computedHash)
            }
        } catch (e: Exception) {
            false
        }
    }

    fun needsMigration(stored: String): Boolean = ":" !in stored

    private fun pbkdf2(pin: CharArray, salt: ByteArray, iterations: Int, keyLength: Int): ByteArray {
        val spec = PBEKeySpec(pin, salt, iterations, keyLength)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val key = factory.generateSecret(spec).encoded
        spec.clearPassword()
        return key
    }

    private fun verifyLegacy(pin: String, storedHash: String): Boolean {
        val computed = MessageDigest.getInstance("SHA-256")
            .digest(pin.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        return MessageDigest.isEqual(computed.toByteArray(), storedHash.toByteArray())
    }

    private fun formatBytes(bytes: ByteArray): String =
        bytes.joinToString("") { "%02x".format(it) }

    private fun parseBytes(hex: String): ByteArray =
        hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}
