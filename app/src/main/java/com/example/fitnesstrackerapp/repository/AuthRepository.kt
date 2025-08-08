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
import com.example.fitnesstrackerapp.data.dao.UserDao
import com.example.fitnesstrackerapp.data.entity.User
import com.example.fitnesstrackerapp.security.CryptoManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Date
import androidx.core.content.edit

/**
 * Repository class for handling user authentication operations.
 * Provides secure login, registration, and session management functionality.
 */
class AuthRepository(
    private val userDao: UserDao,
    private val passwordManager: CryptoManager,
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
     * Registers a new user
     */
    suspend fun register(email: String, password: String, name: String): AuthResult {
        return try {
            val existingUser = userDao.getUserByEmail(email)
            if (existingUser != null) {
                AuthResult.Error("User with this email already exists")
            } else {
                val salt = passwordManager.generateSalt()
                val hashedPassword = passwordManager.hashPassword(password, salt)
                val user = User(
                    email = email,
                    username = name,
                    passwordHash = passwordManager.bytesToHex(hashedPassword),
                    passwordSalt = passwordManager.bytesToHex(salt),
                    registrationDate = Date(),
                    isActive = true,
                )

                val userId = userDao.insertUser(user)
                val createdUser = user.copy(id = userId)
                _currentUser.value = createdUser
                _isAuthenticated.value = true
                currentUserId = userId

                // Save user ID to SharedPreferences
                sharedPrefs.edit {putLong("current_user_id", userId) }

                AuthResult.Success("Registration successful")
            }
        } catch (e: Exception) {
            AuthResult.Error("Registration failed: ${e.message}")
        }
    }

    /**
     * Logs in an existing user
     */
    suspend fun login(email: String, password: String): AuthResult {
        return try {
            val user = userDao.getUserByEmail(email)
            if (user != null) {
                val salt = passwordManager.hexToBytes(user.passwordSalt)
                val storedHash = passwordManager.hexToBytes(user.passwordHash)
                if (passwordManager.verifyPassword(password, storedHash, salt)) {
                    _currentUser.value = user
                    _isAuthenticated.value = true
                    currentUserId = user.id

                    // Save user ID to SharedPreferences
                    sharedPrefs.edit { putLong("current_user_id", user.id) }

                    AuthResult.Success("Login successful")
                } else {
                    AuthResult.Error("Invalid email or password")
                }
            } else {
                AuthResult.Error("Invalid email or password")
            }
        } catch (e: Exception) {
            AuthResult.Error("Login failed: ${e.message}")
        }
    }

    /**
     * Logs out the current user
     */
    fun logout() {
        _currentUser.value = null
        _isAuthenticated.value = false
        currentUserId = null

        // Clear user ID from SharedPreferences
        sharedPrefs.edit { remove("current_user_id") }
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
     * Restores user session from stored data
     */
    suspend fun restoreSession(): AuthResult {
        return try {
            val userId = sharedPrefs.getLong("current_user_id", 0L)
            if (userId != 0L) {
                val user = userDao.getUserById(userId)
                if (user != null && user.canLogin()) {
                    _currentUser.value = user
                    _isAuthenticated.value = true
                    currentUserId = userId

                    // Update last login
                    val updatedUser = user.copy(lastLogin = Date())
                    userDao.updateUser(updatedUser)

                    AuthResult.Success("Session restored")
                } else {
                    // Clear invalid session
                    sharedPrefs.edit {remove("current_user_id") }
                    AuthResult.Error("Session expired or account disabled")
                }
            } else {
                AuthResult.Error("No saved session")
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
