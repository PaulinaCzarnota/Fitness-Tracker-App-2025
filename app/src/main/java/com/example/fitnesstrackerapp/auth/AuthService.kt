package com.example.fitnesstrackerapp.auth

import android.util.Log
import com.example.fitnesstrackerapp.data.entity.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date

/**
 * Authentication service that handles user registration, login, and session management.
 * 
 * This service provides methods for user authentication and session management, including:
 * - User registration with email and password
 * - User login with email and password verification
 * - Session management (save, clear, retrieve)
 * - Input validation and error handling
 */
class AuthService(
    private val passwordManager: PasswordManager,
    private val sessionManager: SessionManager
) {
    companion object {
        private const val TAG = "AuthService"
        private val EMAIL_PATTERN = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$".toRegex()
        private val USERNAME_PATTERN = "^[a-zA-Z0-9_]+$".toRegex()
        private const val MIN_PASSWORD_LENGTH = 8
    }

    /**
     * Registers a new user with email and password.
     */
    suspend fun registerUser(
        email: String,
        password: String,
        username: String? = null
    ): Result<User> = withContext(Dispatchers.IO) {
        try {
            validateEmail(email)
            validatePassword(password)
            username?.let { validateUsername(it) }

            // Generate salt and hash password
            val salt = passwordManager.generateSalt()
            val hashedPassword = passwordManager.hashPassword(password, salt)

            // Create user with hashed password and salt as hex strings
            val user = User(
                id = 0,
                email = email.lowercase(),
                username = username?.trim() ?: email.substringBefore("@"),
                passwordHash = String(hashedPassword), // Convert ByteArray to String
                passwordSalt = String(salt), // Convert ByteArray to String
                registrationDate = Date(),
                isActive = true
            )

            Log.d(TAG, "User registered successfully: ${user.email}")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Registration failed", e)
            Result.failure(e)
        }
    }

    /**
     * Authenticates a user with email and password.
     */
    suspend fun loginUser(
        email: String,
        password: String
    ): Result<User> = withContext(Dispatchers.IO) {
        try {
            validateEmail(email)
            validatePassword(password)

            // Mock user for demonstration - replace with actual DAO call
            val user = User(
                id = 1L,
                email = email.lowercase(),
                username = email.substringBefore("@"),
                passwordHash = "mock_hash",
                passwordSalt = "mock_salt",
                registrationDate = Date(),
                isActive = true
            )

            // Verify password
            val salt = passwordManager.hexToBytes(user.passwordSalt)
            val storedHash = passwordManager.hexToBytes(user.passwordHash)
            val isPasswordValid = passwordManager.verifyPassword(password, storedHash, salt)

            if (isPasswordValid) {
                sessionManager.saveUserSession(user)
                Log.d(TAG, "User logged in successfully: ${user.email}")
                Result.success(user)
            } else {
                Result.failure(IllegalArgumentException("Invalid credentials"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Login failed", e)
            Result.failure(e)
        }
    }

    /**
     * Changes user password.
     */
    suspend fun changePassword(
        userId: Long,
        currentPassword: String,
        newPassword: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            validatePassword(newPassword)

            // Mock user verification - replace with actual DAO call
            val user = User(
                id = userId,
                email = "mock@example.com",
                username = "mock_user",
                passwordHash = "mock_hash",
                passwordSalt = "mock_salt",
                registrationDate = Date(),
                isActive = true
            )

            // Verify current password
            val salt = passwordManager.hexToBytes(user.passwordSalt)
            val storedHash = passwordManager.hexToBytes(user.passwordHash)
            val isCurrentPasswordValid = passwordManager.verifyPassword(currentPassword, storedHash, salt)

            if (!isCurrentPasswordValid) {
                return@withContext Result.failure(IllegalArgumentException("Current password is incorrect"))
            }

            // Generate new salt and hash new password
            val newSalt = passwordManager.generateSalt()
            passwordManager.hashPassword(newPassword, newSalt)

            // Update user password - replace with actual DAO call
            Log.d(TAG, "Password changed successfully for user: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Password change failed", e)
            Result.failure(e)
        }
    }

    /**
     * Logs out the current user.
     */
    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            sessionManager.clearUserSession()
            Log.d(TAG, "User logged out successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Logout failed", e)
            Result.failure(e)
        }
    }

    /**
     * Validates email format.
     */
    private fun validateEmail(email: String) {
        if (email.isBlank()) {
            throw IllegalArgumentException("Email cannot be empty")
        }
        if (!EMAIL_PATTERN.matches(email)) {
            throw IllegalArgumentException("Invalid email format")
        }
    }

    /**
     * Validates password requirements.
     */
    private fun validatePassword(password: String) {
        if (password.length < MIN_PASSWORD_LENGTH) {
            throw IllegalArgumentException("Password must be at least $MIN_PASSWORD_LENGTH characters long")
        }
    }

    /**
     * Validates username format.
     */
    private fun validateUsername(username: String) {
        if (username.isBlank()) {
            throw IllegalArgumentException("Username cannot be empty")
        }
        if (!USERNAME_PATTERN.matches(username)) {
            throw IllegalArgumentException("Username can only contain letters, numbers, and underscores")
        }
    }
}
