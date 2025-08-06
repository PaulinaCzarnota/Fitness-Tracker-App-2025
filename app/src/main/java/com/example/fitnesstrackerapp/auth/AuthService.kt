package com.example.fitnesstrackerapp.auth

import android.util.Log
import com.example.fitnesstrackerapp.data.dao.UserDao
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
 * 
 * @property userDao Data access object for user-related database operations
 * @property passwordManager Service for secure password hashing and verification
 * @property sessionManager Service for managing user sessions
 * @property biometricAuthManager Service for biometric authentication
 */

class AuthService(
    private val userDao: UserDao,
    private val passwordManager: PasswordManager,
    private val sessionManager: SessionManager,
    private val biometricAuthManager: BiometricAuthManager
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

            // Check if user already exists
            if (userDao.getUserByEmail(email) != null) {
                return@withContext Result.failure(
                    IllegalStateException("Email already registered")
                )
            }

            // Create user with hashed password
            val hashedPassword = passwordManager.hashPassword(password)
            val user = User(
                id = 0,
                email = email.lowercase(),
                username = username?.trim() ?: email.substringBefore("@"),
                passwordHash = hashedPassword,
                registrationDate = Date(),
                isActive = true
            )

            val userId = userDao.insertUser(user)
            val savedUser = user.copy(id = userId.toInt().toLong())

            Log.d(TAG, "User registered successfully: ${savedUser.email}")
            Result.success(savedUser)
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

            val user = userDao.getUserByEmail(email.lowercase())
                ?: return@withContext Result.failure(
                    IllegalArgumentException("Invalid email or password")
                )

            if (!passwordManager.verifyPassword(password, user.passwordHash)) {
                return@withContext Result.failure(
                    IllegalArgumentException("Invalid email or password")
                )
            }

            if (!user.isActive) {
                return@withContext Result.failure(
                    IllegalStateException("Account is deactivated")
                )
            }

            // Save session
            sessionManager.saveSession(user.id.toString(), user.email)

            Log.d(TAG, "User logged in successfully: ${user.email}")
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Login failed", e)
            Result.failure(e)
        }
    }

    /**
     * Logs out the current user.
     */
    suspend fun logoutUser(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            sessionManager.clearSession()
            Log.d(TAG, "User logged out successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Logout failed", e)
            Result.failure(e)
        }
    }

    /**
     * Gets the current authenticated user.
     */
    suspend fun getCurrentUser(): Result<User?> = withContext(Dispatchers.IO) {
        try {
            val userId = sessionManager.getCurrentUserId()
                ?: return@withContext Result.success(null)

            val user = userDao.getUserById(userId.toInt())
            Result.success(user)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get current user", e)
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
     * Validates password strength.
     */
    private fun validatePassword(password: String) {
        if (password.length < MIN_PASSWORD_LENGTH) {
            throw IllegalArgumentException("Password must be at least $MIN_PASSWORD_LENGTH characters")
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
