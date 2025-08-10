package com.example.fitnesstrackerapp.security

/**
 * Security Manager
 *
 * Responsibilities:
 * - Handles secure storage of user credentials
 * - Manages authentication tokens
 * - Encrypts sensitive user data
 *
 * This class uses EncryptedSharedPreferences and MasterKey to securely store sensitive data.
 * All storage and retrieval operations are performed on the IO dispatcher for thread safety.
 */

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SecurityManager(private val context: Context) {
    // Lazily initialize the MasterKey for encryption/decryption
    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .setKeyGenParameterSpec(
                KeyGenParameterSpec.Builder(
                    "_androidx_security_master_key_",
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build(),
            )
            .build()
    }

    // Lazily initialize EncryptedSharedPreferences for secure key-value storage
    private val securePreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    /**
     * Stores the authentication token securely.
     *
     * @param token The authentication token to store.
     */
    suspend fun storeAuthToken(token: String) = withContext(Dispatchers.IO) {
        // Store the token in encrypted preferences
        securePreferences.edit {
            putString(KEY_AUTH_TOKEN, token)
        }
    }

    /**
     * Retrieves the authentication token securely.
     *
     * @return The stored authentication token, or null if not found.
     */
    suspend fun getAuthToken(): String? = withContext(Dispatchers.IO) {
        securePreferences.getString(KEY_AUTH_TOKEN, null)
    }

    /**
     * Clears the stored authentication token.
     */
    suspend fun clearAuthToken() = withContext(Dispatchers.IO) {
        securePreferences.edit {
            remove(KEY_AUTH_TOKEN)
        }
    }

    /**
     * Stores the user's email and hashed password securely.
     *
     * @param email The user's email address.
     * @param hashedPassword The user's hashed password.
     */
    suspend fun storeUserCredentials(email: String, hashedPassword: String) = withContext(Dispatchers.IO) {
        securePreferences.edit {
            putString(KEY_USER_EMAIL, email)
                .putString(KEY_USER_PASSWORD_HASH, hashedPassword)
        }
    }

    /**
     * Clears the stored user credentials (email and password hash).
     */
    suspend fun clearUserCredentials() = withContext(Dispatchers.IO) {
        securePreferences.edit {
            remove(KEY_USER_EMAIL)
                .remove(KEY_USER_PASSWORD_HASH)
        }
    }

    companion object {
        // Key for storing the authentication token
        private const val KEY_AUTH_TOKEN = "auth_token"

        // Key for storing the user's email
        private const val KEY_USER_EMAIL = "user_email"

        // Key for storing the user's password hash
        private const val KEY_USER_PASSWORD_HASH = "user_password_hash"
    }
}
