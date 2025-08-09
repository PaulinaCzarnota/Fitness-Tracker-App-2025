/**
 * Repository class for handling user authentication operations in the Fitness Tracker application.
 *
 * This repository manages:
 * - User registration with secure password hashing
 * - User login validation and session management
 * - Authentication state tracking
 * - Password security and encryption
 *
 * @param userDao Data access object for user database operations
 * @param passwordManager Security manager for password encryption and validation
 */

package com.example.fitnesstrackerapp.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.fitnesstrackerapp.auth.SessionManager
import com.example.fitnesstrackerapp.auth.SessionRestoreResult
import com.example.fitnesstrackerapp.data.dao.UserDao
import com.example.fitnesstrackerapp.data.entity.User
import com.example.fitnesstrackerapp.security.CryptoManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date

/**
 * Repository class for handling user authentication operations.
 * Provides secure login, registration, and session management functionality.
 */
class AuthRepository(
    private val userDao: UserDao,
    private val passwordManager: CryptoManager,
    private val sessionManager: SessionManager,
    context: Context,
) {

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Add authState property that screens expect
    val authState: StateFlow<User?> = _currentUser.asStateFlow()

    private var currentUserId: Long? = null

    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(
        "fitness_tracker_prefs",
        Context.MODE_PRIVATE,
    )

    /**
     * Registers a new user with enhanced security
     */
    suspend fun register(email: String, password: String, name: String, rememberMe: Boolean = false): AuthResult {
        return try {
            // Check if user already exists
            val existingUser = userDao.getUserByEmail(email)
            if (existingUser != null) {
                return AuthResult.Error("User with this email already exists")
            }

            // Validate input
            if (email.isBlank() || password.isBlank() || name.isBlank()) {
                return AuthResult.Error("All fields are required")
            }

            // Create new user with secure password hashing
            val salt = passwordManager.generateSalt()
            val hashedPassword = passwordManager.hashPassword(password, salt)

            val user = User(
                email = email.trim().lowercase(),
                username = name.trim(),
                passwordHash = passwordManager.bytesToHex(hashedPassword),
                passwordSalt = passwordManager.bytesToHex(salt),
                firstName = name.split(" ").firstOrNull()?.trim(),
                lastName = if (name.split(" ").size > 1) name.split(" ").drop(1).joinToString(" ").trim() else null,
                registrationDate = Date(),
                isActive = true,
                failedLoginAttempts = 0,
                isAccountLocked = false,
            )

            val userId = userDao.insertUser(user)
            val createdUser = user.copy(id = userId)

            // Save session using enhanced SessionManager
            sessionManager.saveUserSession(createdUser, rememberMe)

            _currentUser.value = createdUser
            _isAuthenticated.value = true
            currentUserId = userId

            AuthResult.Success("Registration successful")
        } catch (e: Exception) {
            AuthResult.Error("Registration failed: ${e.message}")
        }
    }

    /**
     * Logs in an existing user with enhanced security checks
     */
    suspend fun login(email: String, password: String, rememberMe: Boolean = false): AuthResult {
        return try {
            if (email.isBlank() || password.isBlank()) {
                return AuthResult.Error("Email and password are required")
            }

            val user = userDao.getUserByEmail(email.trim().lowercase())
            if (user == null) {
                return AuthResult.Error("Invalid email or password")
            }

            // Check if account is locked
            if (user.isAccountLocked) {
                return AuthResult.Error("Account is locked due to too many failed attempts. Please contact support.")
            }

            // Check if account is active
            if (!user.isActive) {
                return AuthResult.Error("Account is deactivated. Please contact support.")
            }

            // Verify password
            val salt = passwordManager.hexToBytes(user.passwordSalt)
            val storedHash = passwordManager.hexToBytes(user.passwordHash)

            if (passwordManager.verifyPassword(password, storedHash, salt)) {
                // Reset failed login attempts on successful login
                if (user.failedLoginAttempts > 0) {
                    userDao.resetFailedLoginAttempts(user.id, Date())
                }

                // Update last login timestamp
                val updatedUser = user.copy(
                    lastLogin = Date(),
                    failedLoginAttempts = 0,
                )
                userDao.updateUser(updatedUser)

                // Save session using enhanced SessionManager
                sessionManager.saveUserSession(updatedUser, rememberMe)

                _currentUser.value = updatedUser
                _isAuthenticated.value = true
                currentUserId = user.id

                AuthResult.Success("Login successful")
            } else {
                // Handle failed login attempt
                return handleFailedLogin(email)
            }
        } catch (e: Exception) {
            AuthResult.Error("Login failed: ${e.message}")
        }
    }

    /**
     * Logs out the current user with secure session cleanup
     */
    suspend fun logout() {
        try {
            sessionManager.clearUserSession()
            _currentUser.value = null
            _isAuthenticated.value = false
            currentUserId = null

            // Clear user ID from SharedPreferences as backup
            sharedPrefs.edit { remove("current_user_id") }
        } catch (e: Exception) {
            // Even if secure session clearing fails, clear local state
            _currentUser.value = null
            _isAuthenticated.value = false
            currentUserId = null
            sharedPrefs.edit { clear() }
        }
    }

    /**
     * Gets the current user's ID
     */
    fun getCurrentUserId(): Long? {
        return currentUserId
    }

    /**
     * Gets the current user
     */
    fun getCurrentUser(): User? {
        return _currentUser.value
    }

    /**
     * Updates user profile information
     */
    suspend fun updateProfile(user: User): AuthResult {
        return try {
            userDao.updateUser(user)
            _currentUser.value = user
            AuthResult.Success("Profile updated successfully")
        } catch (e: Exception) {
            AuthResult.Error("Failed to update profile: ${e.message}")
        }
    }

    /**
     * Initiates password reset process
     */
    suspend fun initiatePasswordReset(email: String): AuthResult {
        return try {
            val user = userDao.getUserByEmail(email)
            if (user != null) {
                // In a real app, this would send an email with reset token
                // For now, we'll simulate the process
                val resetToken = generateResetToken()

                // Store reset token temporarily (in production, use a separate table)
                sharedPrefs.edit {
                    putString("reset_token_$email", resetToken)
                        .putLong("reset_token_time_$email", System.currentTimeMillis())
                }

                AuthResult.Success("Password reset instructions sent to your email")
            } else {
                // Don't reveal whether email exists for security
                AuthResult.Success("If this email is registered, you will receive reset instructions")
            }
        } catch (e: Exception) {
            AuthResult.Error("Failed to process password reset: ${e.message}")
        }
    }

    /**
     * Resets password using reset token
     */
    suspend fun resetPassword(email: String, resetToken: String, newPassword: String): AuthResult {
        return try {
            val storedToken = sharedPrefs.getString("reset_token_$email", null)
            val tokenTime = sharedPrefs.getLong("reset_token_time_$email", 0)
            val currentTime = System.currentTimeMillis()
            val tokenValidityHours = 24 * 60 * 60 * 1000 // 24 hours

            if (storedToken != null && storedToken == resetToken &&
                (currentTime - tokenTime) < tokenValidityHours
            ) {
                val user = userDao.getUserByEmail(email)
                if (user != null) {
                    val salt = passwordManager.generateSalt()
                    val hashedPassword = passwordManager.hashPassword(newPassword, salt)

                    val updatedUser = user.copy(
                        passwordHash = passwordManager.bytesToHex(hashedPassword),
                        passwordSalt = passwordManager.bytesToHex(salt),
                        lastPasswordChange = Date(),
                        failedLoginAttempts = 0,
                        isAccountLocked = false,
                    )

                    userDao.updateUser(updatedUser)

                    // Clear reset token
                    sharedPrefs.edit {
                        remove("reset_token_$email")
                            .remove("reset_token_time_$email")
                    }

                    AuthResult.Success("Password reset successfully")
                } else {
                    AuthResult.Error("User not found")
                }
            } else {
                AuthResult.Error("Invalid or expired reset token")
            }
        } catch (e: Exception) {
            AuthResult.Error("Failed to reset password: ${e.message}")
        }
    }

    /**
     * Changes user password (requires current password)
     */
    suspend fun changePassword(currentPassword: String, newPassword: String): AuthResult {
        return try {
            val user = _currentUser.value
            if (user != null) {
                val salt = passwordManager.hexToBytes(user.passwordSalt)
                val storedHash = passwordManager.hexToBytes(user.passwordHash)

                if (passwordManager.verifyPassword(currentPassword, storedHash, salt)) {
                    val newSalt = passwordManager.generateSalt()
                    val newHashedPassword = passwordManager.hashPassword(newPassword, newSalt)

                    val updatedUser = user.copy(
                        passwordHash = passwordManager.bytesToHex(newHashedPassword),
                        passwordSalt = passwordManager.bytesToHex(newSalt),
                        lastPasswordChange = Date(),
                    )

                    userDao.updateUser(updatedUser)
                    _currentUser.value = updatedUser

                    AuthResult.Success("Password changed successfully")
                } else {
                    AuthResult.Error("Current password is incorrect")
                }
            } else {
                AuthResult.Error("User not authenticated")
            }
        } catch (e: Exception) {
            AuthResult.Error("Failed to change password: ${e.message}")
        }
    }

    /**
     * Handles failed login attempts and account locking
     */
    suspend fun handleFailedLogin(email: String): AuthResult {
        return try {
            val user = userDao.getUserByEmail(email)
            if (user != null) {
                val updatedAttempts = user.failedLoginAttempts + 1
                val shouldLock = updatedAttempts >= MAX_FAILED_ATTEMPTS

                val updatedUser = user.copy(
                    failedLoginAttempts = updatedAttempts,
                    isAccountLocked = shouldLock,
                )

                userDao.updateUser(updatedUser)

                if (shouldLock) {
                    AuthResult.Error("Account locked due to too many failed attempts. Contact support to unlock.")
                } else {
                    val remainingAttempts = MAX_FAILED_ATTEMPTS - updatedAttempts
                    AuthResult.Error("Invalid credentials. $remainingAttempts attempts remaining.")
                }
            } else {
                AuthResult.Error("Invalid email or password")
            }
        } catch (e: Exception) {
            AuthResult.Error("Authentication error: ${e.message}")
        }
    }

    /**
     * Restores user session using enhanced SessionManager
     */
    suspend fun restoreSession(): AuthResult {
        return try {
            when (val result = sessionManager.restoreSession()) {
                is SessionRestoreResult.Success -> {
                    val user = result.user
                    _currentUser.value = user
                    _isAuthenticated.value = true
                    currentUserId = user.id

                    // Refresh session to extend timeout
                    sessionManager.refreshSession()

                    AuthResult.Success("Session restored successfully")
                }
                is SessionRestoreResult.Failed -> {
                    // Clear any remaining legacy session data
                    sharedPrefs.edit { clear() }
                    AuthResult.Error(result.message)
                }
            }
        } catch (e: Exception) {
            AuthResult.Error("Failed to restore session: ${e.message}")
        }
    }

    private fun generateResetToken(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..32)
            .map { chars.random() }
            .joinToString("")
    }

    companion object {
        private const val MAX_FAILED_ATTEMPTS = 5
    }
}
