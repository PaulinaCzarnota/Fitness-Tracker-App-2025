package com.example.fitnesstrackerapp.auth

import android.content.Context
import android.util.Log
import com.example.fitnesstrackerapp.data.dao.UserDao
import com.example.fitnesstrackerapp.data.entity.User
import com.example.fitnesstrackerapp.security.SecurePrefsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.Date

/**
 * Enhanced session manager with encrypted SharedPreferences and automatic login flow.
 *
 * This class provides:
 * - Secure storage of session data using EncryptedSharedPreferences
 * - Automatic session restoration on app start
 * - Session timeout and security features
 * - Biometric authentication integration
 * - Reactive session state management
 */
class SessionManager(
    context: Context,
    private val userDao: UserDao,
) {

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "email"
        private const val KEY_USERNAME = "username"
        private const val KEY_FIRST_NAME = "first_name"
        private const val KEY_LAST_NAME = "last_name"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_LOGIN_TIMESTAMP = "login_timestamp"
        private const val KEY_SESSION_TOKEN = "session_token"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_AUTO_LOGIN_ENABLED = "auto_login_enabled"
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val SESSION_TIMEOUT_HOURS = 72 // 3 days
        private const val TAG = "SessionManager"
    }

    private val securePrefs = SecurePrefsManager(context, "user_session")

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: Flow<User?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: Flow<Boolean> = _isLoggedIn.asStateFlow()

    /**
     * Saves user session data securely with encryption
     */
    suspend fun saveUserSession(user: User, rememberMe: Boolean = false) = withContext(Dispatchers.IO) {
        try {
            val currentTime = System.currentTimeMillis()
            val sessionToken = generateSessionToken()

            securePrefs.putLong(KEY_USER_ID, user.id)
            securePrefs.putString(KEY_EMAIL, user.email)
            securePrefs.putString(KEY_USERNAME, user.username)
            securePrefs.putString(KEY_FIRST_NAME, user.firstName ?: "")
            securePrefs.putString(KEY_LAST_NAME, user.lastName ?: "")
            securePrefs.putBoolean(KEY_IS_LOGGED_IN, true)
            securePrefs.putLong(KEY_LOGIN_TIMESTAMP, currentTime)
            securePrefs.putString(KEY_SESSION_TOKEN, sessionToken)
            securePrefs.putBoolean(KEY_REMEMBER_ME, rememberMe)

            _currentUser.value = user
            _isLoggedIn.value = true

            // Update last login in database
            userDao.updateLastLogin(user.id, Date())

            Log.d(TAG, "User session saved securely for: ${user.email}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save user session", e)
            throw SessionException("Failed to save session", e)
        }
    }

    /**
     * Retrieves current user session with full user data from database
     */
    suspend fun getCurrentUser(): User? = withContext(Dispatchers.IO) {
        return@withContext if (isSessionValid()) {
            try {
                val userId = securePrefs.getLong(KEY_USER_ID, 0L)
                if (userId != 0L) {
                    val user = userDao.getUserById(userId)
                    if (user != null && user.canLogin()) {
                        _currentUser.value = user
                        user
                    } else {
                        clearUserSession()
                        null
                    }
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error retrieving current user", e)
                clearUserSession()
                null
            }
        } else {
            null
        }
    }

    /**
     * Checks if user is currently logged in and session is valid
     */
    fun isLoggedIn(): Boolean {
        return _isLoggedIn.value && isSessionValid()
    }

    /**
     * Validates current session including timeout check
     */
    private fun isSessionValid(): Boolean {
        val isLoggedIn = securePrefs.getBoolean(KEY_IS_LOGGED_IN, false)
        if (!isLoggedIn) return false

        val loginTimestamp = securePrefs.getLong(KEY_LOGIN_TIMESTAMP, 0L)
        val currentTime = System.currentTimeMillis()
        val sessionTimeoutMs = SESSION_TIMEOUT_HOURS * 60 * 60 * 1000L

        val rememberMe = securePrefs.getBoolean(KEY_REMEMBER_ME, false)

        return if (rememberMe) {
            // Extended session for remember me (30 days)
            (currentTime - loginTimestamp) < (30 * 24 * 60 * 60 * 1000L)
        } else {
            // Standard session timeout
            (currentTime - loginTimestamp) < sessionTimeoutMs
        }
    }

    /**
     * Clears user session data securely
     */
    suspend fun clearUserSession() = withContext(Dispatchers.IO) {
        try {
            securePrefs.clear()
            _currentUser.value = null
            _isLoggedIn.value = false
            Log.d(TAG, "User session cleared securely")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear user session", e)
            throw SessionException("Failed to clear session", e)
        }
    }

    /**
     * Gets current user ID
     */
    fun getCurrentUserId(): Long {
        return if (isSessionValid()) {
            securePrefs.getLong(KEY_USER_ID, 0L)
        } else {
            0L
        }
    }

    /**
     * Attempts to restore user session on app start
     */
    suspend fun restoreSession(): SessionRestoreResult = withContext(Dispatchers.IO) {
        return@withContext try {
            if (isSessionValid()) {
                val user = getCurrentUser()
                if (user != null) {
                    _isLoggedIn.value = true
                    SessionRestoreResult.Success(user)
                } else {
                    clearUserSession()
                    SessionRestoreResult.Failed("User data not found")
                }
            } else {
                clearUserSession()
                SessionRestoreResult.Failed("Session expired or invalid")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore session", e)
            clearUserSession()
            SessionRestoreResult.Failed("Session restore error: ${e.message}")
        }
    }

    /**
     * Enables biometric authentication for the current user
     */
    suspend fun enableBiometricAuth() = withContext(Dispatchers.IO) {
        securePrefs.putBoolean(KEY_BIOMETRIC_ENABLED, true)
        Log.d(TAG, "Biometric authentication enabled")
    }

    /**
     * Disables biometric authentication
     */
    suspend fun disableBiometricAuth() = withContext(Dispatchers.IO) {
        securePrefs.putBoolean(KEY_BIOMETRIC_ENABLED, false)
        Log.d(TAG, "Biometric authentication disabled")
    }

    /**
     * Checks if biometric authentication is enabled
     */
    fun isBiometricAuthEnabled(): Boolean {
        return securePrefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    /**
     * Enables auto-login functionality
     */
    suspend fun enableAutoLogin() = withContext(Dispatchers.IO) {
        securePrefs.putBoolean(KEY_AUTO_LOGIN_ENABLED, true)
        Log.d(TAG, "Auto-login enabled")
    }

    /**
     * Disables auto-login functionality
     */
    suspend fun disableAutoLogin() = withContext(Dispatchers.IO) {
        securePrefs.putBoolean(KEY_AUTO_LOGIN_ENABLED, false)
        Log.d(TAG, "Auto-login disabled")
    }

    /**
     * Checks if auto-login is enabled
     */
    fun isAutoLoginEnabled(): Boolean {
        return securePrefs.getBoolean(KEY_AUTO_LOGIN_ENABLED, true)
    }

    /**
     * Refreshes the session timestamp to extend session
     */
    suspend fun refreshSession() = withContext(Dispatchers.IO) {
        if (isSessionValid()) {
            securePrefs.putLong(KEY_LOGIN_TIMESTAMP, System.currentTimeMillis())
            Log.d(TAG, "Session refreshed")
        }
    }

    /**
     * Gets session information for debugging
     */
    fun getSessionInfo(): SessionInfo {
        val loginTimestamp = securePrefs.getLong(KEY_LOGIN_TIMESTAMP, 0L)
        val currentTime = System.currentTimeMillis()
        val sessionAge = currentTime - loginTimestamp

        return SessionInfo(
            isActive = isSessionValid(),
            loginTimestamp = loginTimestamp,
            sessionAge = sessionAge,
            rememberMeEnabled = securePrefs.getBoolean(KEY_REMEMBER_ME, false),
            biometricEnabled = isBiometricAuthEnabled(),
            autoLoginEnabled = isAutoLoginEnabled(),
        )
    }

    /**
     * Generates a secure session token
     */
    private fun generateSessionToken(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..64)
            .map { chars.random() }
            .joinToString("")
    }
}

/**
 * Sealed class representing session restore results
 */
sealed class SessionRestoreResult {
    data class Success(val user: User) : SessionRestoreResult()
    data class Failed(val message: String) : SessionRestoreResult()
}

/**
 * Data class containing session information
 */
data class SessionInfo(
    val isActive: Boolean,
    val loginTimestamp: Long,
    val sessionAge: Long,
    val rememberMeEnabled: Boolean,
    val biometricEnabled: Boolean,
    val autoLoginEnabled: Boolean,
)

/**
 * Custom exception for session-related errors
 */
class SessionException(message: String, cause: Throwable? = null) : Exception(message, cause)
