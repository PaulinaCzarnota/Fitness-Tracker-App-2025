package com.example.fitnesstrackerapp.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import androidx.core.content.edit

/**
 * Manages secure storage of sensitive data using Android's EncryptedSharedPreferences.
 * This class provides a secure way to store and retrieve sensitive information
 * such as authentication tokens, user preferences, and other sensitive data.
 *
 * @property context The application context for accessing shared preferences.
 * @property prefsFileName The name of the shared preferences file.
 */
class SecurePrefsManager(
    private val context: Context,
    private val prefsFileName: String = "secure_prefs"
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        prefsFileName,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    /**
     * Stores a string value securely.
     *
     * @param key The key with which the value will be stored.
     * @param value The value to store.
     */
    fun putString(key: String, value: String) {
        encryptedPrefs.edit { putString(key, value) }
    }

    /**
     * Retrieves a securely stored string value.
     *
     * @param key The key of the value to retrieve.
     * @param defaultValue The default value to return if the key doesn't exist.
     * @return The stored string or the default value if not found.
     */
    fun getString(key: String, defaultValue: String = ""): String {
        return encryptedPrefs.getString(key, defaultValue) ?: defaultValue
    }

    /**
     * Stores an integer value securely.
     *
     * @param key The key with which the value will be stored.
     * @param value The value to store.
     */
    fun putInt(key: String, value: Int) {
        encryptedPrefs.edit { putInt(key, value) }
    }

    /**
     * Retrieves a securely stored integer value.
     *
     * @param key The key of the value to retrieve.
     * @param defaultValue The default value to return if the key doesn't exist.
     * @return The stored integer or the default value if not found.
     */
    fun getInt(key: String, defaultValue: Int = 0): Int {
        return encryptedPrefs.getInt(key, defaultValue)
    }

    /**
     * Stores a long value securely.
     *
     * @param key The key with which the value will be stored.
     * @param value The value to store.
     */
    fun putLong(key: String, value: Long) {
        encryptedPrefs.edit { putLong(key, value) }
    }

    /**
     * Retrieves a securely stored long value.
     *
     * @param key The key of the value to retrieve.
     * @param defaultValue The default value to return if the key doesn't exist.
     * @return The stored long or the default value if not found.
     */
    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return encryptedPrefs.getLong(key, defaultValue)
    }

    /**
     * Stores a boolean value securely.
     *
     * @param key The key with which the value will be stored.
     * @param value The value to store.
     */
    fun putBoolean(key: String, value: Boolean) {
        encryptedPrefs.edit { putBoolean(key, value) }
    }

    /**
     * Retrieves a securely stored boolean value.
     *
     * @param key The key of the value to retrieve.
     * @param defaultValue The default value to return if the key doesn't exist.
     * @return The stored boolean or the default value if not found.
     */
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return encryptedPrefs.getBoolean(key, defaultValue)
    }

    /**
     * Stores a float value securely.
     *
     * @param key The key with which the value will be stored.
     * @param value The value to store.
     */
    fun putFloat(key: String, value: Float) {
        encryptedPrefs.edit { putFloat(key, value) }
    }

    /**
     * Retrieves a securely stored float value.
     *
     * @param key The key of the value to retrieve.
     * @param defaultValue The default value to return if the key doesn't exist.
     * @return The stored float or the default value if not found.
     */
    fun getFloat(key: String, defaultValue: Float = 0f): Float {
        return encryptedPrefs.getFloat(key, defaultValue)
    }

    /**
     * Removes a securely stored value.
     *
     * @param key The key of the value to remove.
     */
    fun remove(key: String) {
        encryptedPrefs.edit { remove(key) }
    }

    /**
     * Checks if a key exists in the secure preferences.
     *
     * @param key The key to check.
     * @return True if the key exists, false otherwise.
     */
    fun contains(key: String): Boolean {
        return encryptedPrefs.contains(key)
    }

    /**
     * Clears all securely stored values.
     */
    fun clear() {
        encryptedPrefs.edit { clear() }
    }

    /**
     * Stores a set of strings securely.
     *
     * @param key The key with which the set will be stored.
     * @param values The set of strings to store.
     */
    fun putStringSet(key: String, values: Set<String>) {
        encryptedPrefs.edit { putStringSet(key, values) }
    }

    /**
     * Retrieves a securely stored set of strings.
     *
     * @param key The key of the set to retrieve.
     * @param defaultValue The default value to return if the key doesn't exist.
     * @return The stored set of strings or the default value if not found.
     */
    fun getStringSet(key: String, defaultValue: Set<String> = emptySet()): Set<String> {
        return encryptedPrefs.getStringSet(key, defaultValue) ?: defaultValue
    }
}
