package com.example.fitnesstrackerapp.util

import java.security.MessageDigest
import java.util.*

/**
 * SecurityUtils
 *
 * Utility object providing security-related functions used for:
 * - Normalizing email addresses
 * - Hashing passwords using SHA-256
 *
 * These functions help standardize input and securely store sensitive data.
 * This version is appropriate for academic or basic production apps.
 */
object SecurityUtils {

    /**
     * normaliseEmail
     *
     * Standardizes email input for consistency in the database.
     * - Removes leading/trailing spaces.
     * - Converts to lowercase using a locale-safe method.
     *
     * @param raw The user-entered email string.
     * @return A trimmed and lowercased version of the email.
     *
     * Example:
     *     "  USER@Example.com  " → "user@example.com"
     */
    fun normaliseEmail(raw: String): String {
        return raw.trim().lowercase(Locale.ROOT)
    }

    /**
     * sha256
     *
     * Hashes a plain-text password into a SHA-256 hex string.
     * Used to store password securely in the database.
     *
     * Note:
     * - No salt or pepper is used here.
     * - This is fine for simple projects or educational use.
     * - For real-world applications, consider using BCrypt or Argon2 with a salt.
     *
     * @param password The user-entered password.
     * @return A 64-character lowercase hexadecimal string of the SHA-256 hash.
     *
     * Example:
     *     sha256("mypassword") → "34819d7beeabb9260a5c854bc85b3e44..."
     */
    fun sha256(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))

        return buildString {
            for (byte in hashBytes) {
                append(String.format("%02x", byte)) // Each byte to two-digit hex
            }
        }
    }
}
