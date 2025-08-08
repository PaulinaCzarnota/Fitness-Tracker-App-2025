package com.example.fitnesstrackerapp.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec

/**
 * Manages cryptographic operations for the application.
 * Provides methods for encrypting and decrypting sensitive data
 * using AES encryption with GCM mode and password hashing.
 *
 * @property context The application context for accessing system services.
 */
class CryptoManager(private val context: Context) {
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    companion object {
        private const val KEY_ALIAS = "fitness_tracker_key"
        private const val KEY_SIZE = 256
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_LENGTH = 12 // 12 bytes for GCM
        private const val TAG_LENGTH = 128 // 128 bits for GCM
        private const val PBKDF2_ITERATIONS = 100000
        private const val SALT_LENGTH = 32
    }

    /**
     * Generates a cryptographically secure random salt.
     *
     * @return A byte array containing the generated salt.
     */
    fun generateSalt(): ByteArray {
        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(salt)
        return salt
    }

    /**
     * Hashes a password using PBKDF2 with SHA-256.
     *
     * @param password The password to hash.
     * @param salt The salt to use for hashing.
     * @return The hashed password as a byte array.
     */
    fun hashPassword(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return factory.generateSecret(spec).encoded
    }

    /**
     * Verifies a password against a stored hash.
     *
     * @param password The password to verify.
     * @param storedHash The stored password hash.
     * @param salt The salt used during hashing.
     * @return True if the password matches, false otherwise.
     */
    fun verifyPassword(password: String, storedHash: ByteArray, salt: ByteArray): Boolean {
        val computedHash = hashPassword(password, salt)
        return MessageDigest.isEqual(computedHash, storedHash)
    }

    /**
     * Converts a byte array to a hexadecimal string.
     *
     * @param bytes The byte array to convert.
     * @return The hexadecimal string representation.
     */
    fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Converts a hexadecimal string to a byte array.
     *
     * @param hex The hexadecimal string to convert.
     * @return The byte array representation.
     */
    fun hexToBytes(hex: String): ByteArray {
        return hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }

    /**
     * Encrypts the given plaintext using AES/GCM/NoPadding.
     *
     * @param plaintext The text to encrypt.
     * @return A [CryptoResult] containing the encrypted data and IV, or an error.
     */
    fun encrypt(plaintext: String): CryptoResult {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val secretKey = getOrCreateSecretKey()

            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv

            val ciphertext = cipher.doFinal(plaintext.toByteArray(StandardCharsets.UTF_8))

            CryptoResult.Success(
                encryptedData = ciphertext,
                iv = iv,
            )
        } catch (e: Exception) {
            CryptoResult.Error(e)
        }
    }

    /**
     * Decrypts the given ciphertext using AES/GCM/NoPadding.
     *
     * @param ciphertext The encrypted data to decrypt.
     * @param iv The initialization vector used during encryption.
     * @return A [CryptoResult] containing the decrypted text, or an error.
     */
    fun decrypt(ciphertext: ByteArray, iv: ByteArray): CryptoResult {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val secretKey = getOrCreateSecretKey()
            val spec = GCMParameterSpec(TAG_LENGTH, iv)

            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            val plaintext = cipher.doFinal(ciphertext)

            CryptoResult.Success(
                decryptedData = plaintext.toString(StandardCharsets.UTF_8),
                iv = iv,
            )
        } catch (e: Exception) {
            CryptoResult.Error(e)
        }
    }

    /**
     * Gets or creates the secret key for encryption/decryption.
     *
     * @return The secret key.
     */
    private fun getOrCreateSecretKey(): SecretKey {
        return if (keyStore.containsAlias(KEY_ALIAS)) {
            keyStore.getKey(KEY_ALIAS, null) as SecretKey
        } else {
            generateSecretKey()
        }
    }

    /**
     * Generates a new secret key and stores it in the Android Keystore.
     *
     * @return The generated secret key.
     */
    private fun generateSecretKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(KEY_SIZE)
            .build()

        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }
}
