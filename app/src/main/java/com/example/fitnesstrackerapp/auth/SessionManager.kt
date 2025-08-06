package com.example.fitnesstrackerapp.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.fitnesstrackerapp.data.entity.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

/**
 * Manages user session data using SharedPreferences
 */
class SessionManager(private val context: Context) {

    companion object {
        private const val PREF_NAME = "user_session"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "email"
        private const val KEY_USERNAME = "username"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val TAG = "SessionManager"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /**
     * Saves user session data
     */
    suspend fun saveUserSession(user: User) = withContext(Dispatchers.IO) {
        try {
            prefs.edit().apply {
                putLong(KEY_USER_ID, user.id)
                putString(KEY_EMAIL, user.email)
                putString(KEY_USERNAME, user.username)
                putBoolean(KEY_IS_LOGGED_IN, true)
                apply()
            }
            Log.d(TAG, "User session saved for: ${user.email}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save user session", e)
            throw e
        }
    }

    /**
     * Retrieves current user session
     */
    fun getCurrentUser(): User? {
        return if (isLoggedIn()) {
            User(
                id = prefs.getLong(KEY_USER_ID, 0L),
                email = prefs.getString(KEY_EMAIL, "") ?: "",
                username = prefs.getString(KEY_USERNAME, "") ?: "",
                passwordHash = "", // Don't store password hash in session
                passwordSalt = "", // Don't store salt in session
                registrationDate = Date(),
                isActive = true
            )
        } else null
    }

    /**
     * Checks if user is currently logged in
     */
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    /**
     * Clears user session data
     */
    suspend fun clearUserSession() = withContext(Dispatchers.IO) {
        try {
            prefs.edit().clear().apply()
            Log.d(TAG, "User session cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear user session", e)
            throw e
        }
    }

    /**
     * Gets current user ID
     */
    fun getCurrentUserId(): Long {
        return prefs.getLong(KEY_USER_ID, 0L)
    }
}
