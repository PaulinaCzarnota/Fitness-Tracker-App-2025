/**
 * Security Manager
 *
 * Responsibilities:
 * - Handles secure storage of user credentials
 * - Manages authentication tokens
 * - Encrypts sensitive user data
 */
package com.example.fitnesstrackerapp.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SecurityManager(private val context: Context) {

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

    private val securePreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    suspend fun storeAuthToken(token: String) = withContext(Dispatchers.IO) {
        securePreferences.edit {
            putString(KEY_AUTH_TOKEN, token)
        }
    }

    suspend fun getAuthToken(): String? = withContext(Dispatchers.IO) {
        securePreferences.getString(KEY_AUTH_TOKEN, null)
    }

    suspend fun clearAuthToken() = withContext(Dispatchers.IO) {
        securePreferences.edit {
            remove(KEY_AUTH_TOKEN)
        }
    }

    suspend fun storeUserCredentials(email: String, hashedPassword: String) = withContext(Dispatchers.IO) {
        securePreferences.edit {
            putString(KEY_USER_EMAIL, email)
                .putString(KEY_USER_PASSWORD_HASH, hashedPassword)
        }
    }

    suspend fun clearUserCredentials() = withContext(Dispatchers.IO) {
        securePreferences.edit {
            remove(KEY_USER_EMAIL)
                .remove(KEY_USER_PASSWORD_HASH)
        }
    }

    companion object {
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_PASSWORD_HASH = "user_password_hash"
    }
}
