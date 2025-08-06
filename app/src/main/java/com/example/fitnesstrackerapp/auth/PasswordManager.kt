package com.example.fitnesstrackerapp.auth

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Manages password hashing and verification using PBKDF2
 */
class PasswordManager {

    companion object {
        private const val ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val ITERATIONS = 10000
        private const val KEY_LENGTH = 256
        private const val SALT_LENGTH = 32
    }

    /**
     * Generates a random salt for password hashing
     */
    fun generateSalt(): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(SALT_LENGTH)
        random.nextBytes(salt)
        return salt
    }

    /**
     * Hashes a password with the given salt using PBKDF2
     */
    fun hashPassword(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        return factory.generateSecret(spec).encoded
    }

    /**
     * Verifies a password against the stored hash and salt
     */
    fun verifyPassword(password: String, storedHash: ByteArray, salt: ByteArray): Boolean {
        val hashedPassword = hashPassword(password, salt)
        return hashedPassword.contentEquals(storedHash)
    }

    /**
     * Converts byte array to hexadecimal string
     */
    fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Converts hexadecimal string to byte array
     */
    fun hexToBytes(hex: String): ByteArray {
        return hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }
}
