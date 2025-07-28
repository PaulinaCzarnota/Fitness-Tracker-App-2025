package com.example.fitnesstrackerapp.repository

import com.example.fitnesstrackerapp.data.User
import com.example.fitnesstrackerapp.data.UserDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * UserRepository
 *
 * Repository layer for managing user-related database operations.
 * Wraps Room DAO in coroutine-safe suspend functions.
 */
class UserRepository(private val userDao: UserDao) {

    /**
     * Registers a user if the email doesn't already exist.
     *
     * @param user User with normalized email and hashed password.
     * @return True if registration succeeds, false if email already exists.
     */
    suspend fun registerUser(user: User): Boolean = withContext(Dispatchers.IO) {
        val existingUser = userDao.getUserByEmail(user.email)
        return@withContext if (existingUser == null) {
            userDao.insertUser(user)
            true
        } else {
            false
        }
    }

    /**
     * Attempts login by verifying email and hashed password.
     *
     * @param email Normalized email
     * @param passwordHash Hashed password using SHA-256
     * @return User if credentials are valid, or null.
     */
    suspend fun login(email: String, passwordHash: String): User? = withContext(Dispatchers.IO) {
        return@withContext userDao.authenticate(email, passwordHash)
    }

    /**
     * Finds a user by their unique ID.
     *
     * @param userId Primary key from the users table.
     * @return Matching user or null if not found.
     */
    suspend fun getUserById(userId: Int): User? = withContext(Dispatchers.IO) {
        return@withContext userDao.getUserById(userId)
    }

    /**
     * (Optional) Checks whether a user with the given email already exists.
     *
     * This is useful for:
     * - Pre-validating user input in registration forms
     * - Displaying inline "email already registered" messages
     *
     * @param email Normalized email to check.
     * @return True if user exists, false otherwise.
     */
    suspend fun doesUserExist(email: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext userDao.getUserByEmail(email) != null
    }
}
