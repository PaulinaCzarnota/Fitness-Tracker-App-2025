package com.example.fitnesstrackerapp.repository

import com.example.fitnesstrackerapp.data.User
import com.example.fitnesstrackerapp.data.UserDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * UserRepository
 *
 * Provides an abstraction layer over the UserDao.
 * Exposes suspend functions for accessing and modifying user data.
 *
 * This repository allows the ViewModel to:
 * - Register users (if the email is unique)
 * - Login/authenticate users
 * - Query users by ID
 * - Optionally check if an email already exists before submission
 */
class UserRepository(private val userDao: UserDao) {

    /**
     * Registers a new user only if the email doesn't already exist.
     *
     * @param user A User object with normalized email and hashed password.
     * @return True if inserted successfully; false if user already exists.
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
     * Logs in the user by verifying their email and hashed password.
     *
     * @param email Normalized email.
     * @param passwordHash SHA-256 hash of the user's password.
     * @return The User object if authentication is successful, or null otherwise.
     */
    suspend fun login(email: String, passwordHash: String): User? = withContext(Dispatchers.IO) {
        return@withContext userDao.authenticate(email, passwordHash)
    }

    /**
     * Retrieves a user by their unique user ID.
     *
     * @param userId Auto-generated Room primary key.
     * @return The corresponding User object or null if not found.
     */
    suspend fun getUserById(userId: Int): User? = withContext(Dispatchers.IO) {
        return@withContext userDao.getUserById(userId)
    }

    /**
     * Checks whether a user with the given email already exists.
     *
     * This function can be used in:
     * - Registration form to give real-time validation feedback.
     * - Admin dashboards or user management features.
     *
     * NOTE: Although this function is currently unused, it is good
     * to keep it in the repository for future feature expansion.
     *
     * @param email Normalized email to check.
     * @return True if a user exists with this email; false otherwise.
     */
    suspend fun doesUserExist(email: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext userDao.getUserByEmail(email) != null
    }
}
