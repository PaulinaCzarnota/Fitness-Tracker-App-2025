package com.example.fitnesstrackerapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.fitnesstrackerapp.data.entity.User
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * User Data Access Object for the Fitness Tracker application.
 *
 * This DAO provides comprehensive database operations for User entities including
 * authentication, profile management, security features, and account status tracking.
 * All operations are coroutine-based for optimal performance and UI responsiveness.
 *
 * Key Features:
 * - User authentication and credential management
 * - Profile information updates and retrieval
 * - Account security (locking, failed attempts tracking)
 * - User status management (active/inactive)
 * - Email and username uniqueness validation
 * - Batch operations for administrative functions
 */
@Dao
interface UserDao {

    /**
     * Inserts a new user into the database.
     *
     * @param user User entity to insert
     * @return The ID of the inserted user
     * @throws SQLException if email or username already exists
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: User): Long

    /**
     * Updates an existing user in the database.
     *
     * @param user User entity with updated data
     */
    @Update
    suspend fun updateUser(user: User)

    /**
     * Updates user height and timestamp.
     *
     * @param userId User ID
     * @param height New height in centimeters
     * @param updatedAt Update timestamp
     */
    @Query("UPDATE users SET height_cm = :height, updated_at = :updatedAt WHERE id = :userId")
    suspend fun updateHeight(userId: Long, height: Float, updatedAt: Date)

    /**
     * Updates user weight and timestamp.
     *
     * @param userId User ID
     * @param weight New weight in kilograms
     * @param updatedAt Update timestamp
     */
    @Query("UPDATE users SET weight_kg = :weight, updated_at = :updatedAt WHERE id = :userId")
    suspend fun updateWeight(userId: Long, weight: Float, updatedAt: Date)

    /**
     * Updates daily step goal and timestamp.
     *
     * @param userId User ID
     * @param stepGoal New daily step goal
     * @param updatedAt Update timestamp
     */
    @Query("UPDATE users SET daily_step_goal = :stepGoal, updated_at = :updatedAt WHERE id = :userId")
    suspend fun updateStepGoal(userId: Long, stepGoal: Int, updatedAt: Date)

    /**
     * Updates metric units preference and timestamp.
     *
     * @param updatedAt Update timestamp
     */
    @Query("UPDATE users SET use_metric_units = :useMetric, updated_at = :updatedAt WHERE id = :userId")
    suspend fun updateMetricUnits(userId: Long, useMetric: Boolean, updatedAt: Date)

    /**
     * Updates user password hash and salt.
     *
     * @param userId User ID
     * @param passwordHash New password hash
     * @param passwordSalt New password salt
     * @param updatedAt Update timestamp
     */
    @Query("UPDATE users SET password_hash = :passwordHash, password_salt = :passwordSalt, last_password_change = :updatedAt, updated_at = :updatedAt WHERE id = :userId")
    suspend fun updatePassword(userId: Long, passwordHash: String, passwordSalt: String, updatedAt: Date)

    /**
     * Deletes a user from the database.
     *
     * @param user User entity to delete
     */
    @Delete
    suspend fun deleteUser(user: User)

    /**
     * Gets a user by their email address.
     *
     * @param email Email address to search for
     * @return User entity or null if not found
     */
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    /**
     * Gets a user by their ID.
     *
     * @param userId User ID to search for
     * @return User entity or null if not found
     */
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Long): User?

    /**
     * Gets a user by their username.
     *
     * @param username Username to search for
     * @return User entity or null if not found
     */
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    /**
     * Gets all active users as a Flow for reactive updates.
     *
     * @return Flow of list of active users ordered by creation date
     */
    @Query("SELECT * FROM users WHERE is_active = 1 ORDER BY created_at DESC")
    fun getAllActiveUsers(): Flow<List<User>>

    /**
     * Gets all users (including inactive) as a Flow.
     *
     * @return Flow of list of all users ordered by creation date
     */
    @Query("SELECT * FROM users ORDER BY created_at DESC")
    fun getAllUsers(): Flow<List<User>>

    /**
     * Gets users with locked accounts.
     *
     * @return Flow of list of locked users
     */
    @Query("SELECT * FROM users WHERE is_account_locked = 1 OR failed_login_attempts >= 5")
    fun getLockedUsers(): Flow<List<User>>

    /**
     * Checks if an email is already registered.
     *
     * @param email Email to check
     * @return true if email exists
     */
    @Query("SELECT COUNT(*) > 0 FROM users WHERE email = :email")
    suspend fun isEmailRegistered(email: String): Boolean

    /**
     * Checks if a username is already taken.
     *
     * @param username Username to check
     * @return true if username exists
     */
    @Query("SELECT COUNT(*) > 0 FROM users WHERE username = :username")
    suspend fun isUsernameTaken(username: String): Boolean

    /**
     * Updates the last login time for a user.
     *
     * @param userId User ID
     * @param lastLogin Last login timestamp
     */
    @Query("UPDATE users SET last_login = :lastLogin, updated_at = :lastLogin WHERE id = :userId")
    suspend fun updateLastLogin(userId: Long, lastLogin: Date)

    /**
     * Increments failed login attempts for a user.
     *
     * @param email User's email
     * @param currentTime Current timestamp
     */
    @Query("UPDATE users SET failed_login_attempts = failed_login_attempts + 1, updated_at = :currentTime WHERE email = :email")
    suspend fun incrementFailedLoginAttempts(email: String, currentTime: Date)

    /**
     * Resets failed login attempts for a user.
     *
     * @param userId User ID
     * @param currentTime Current timestamp
     */
    @Query("UPDATE users SET failed_login_attempts = 0, is_account_locked = 0, updated_at = :currentTime WHERE id = :userId")
    suspend fun resetFailedLoginAttempts(userId: Long, currentTime: Date)

    /**
     * Locks a user account due to security violations.
     *
     * @param userId User ID
     * @param currentTime Current timestamp
     */
    @Query("UPDATE users SET is_account_locked = 1, updated_at = :currentTime WHERE id = :userId")
    suspend fun lockAccount(userId: Long, currentTime: Date)

    /**
     * Unlocks a user account and resets failed attempts.
     *
     * @param userId User ID
     * @param currentTime Current timestamp
     */
    @Query("UPDATE users SET is_account_locked = 0, failed_login_attempts = 0, updated_at = :currentTime WHERE id = :userId")
    suspend fun unlockAccount(userId: Long, currentTime: Date)

    /**
     * Deactivates a user account (soft delete).
     *
     * @param userId User ID
     * @param currentTime Current timestamp
     */
    @Query("UPDATE users SET is_active = 0, updated_at = :currentTime WHERE id = :userId")
    suspend fun deactivateUser(userId: Long, currentTime: Long)

    /**
     * Activates a user account.
     *
     * @param userId User ID
     */
    @Query("UPDATE users SET is_active = 1, updated_at = :currentTime WHERE id = :userId")
    suspend fun activateUser(userId: Long, currentTime: Long)

    /**
     * Updates user profile information.
     *
     * @param userId User ID
     * @param firstName First name
     * @param lastName Last name
     * @param heightCm Height in centimeters
     * @param weightKg Weight in kilograms
     * @param currentTime Current timestamp
     */
    @Query("""
        UPDATE users SET 
        first_name = :firstName, 
        last_name = :lastName, 
        height_cm = :heightCm, 
        weight_kg = :weightKg, 
        updated_at = :currentTime 
        WHERE id = :userId
    """)
    suspend fun updateUserProfile(
        userId: Long,
        firstName: String?,
        lastName: String?,
        heightCm: Float?,
        weightKg: Float?,
        currentTime: Long
    )

    /**
     * Updates user password and related security information.
     *
     * @param userId User ID
     * @param passwordHash New password hash
     * @param passwordSalt New password salt
     * @param currentTime Current timestamp
     */
    @Query("""
        UPDATE users SET 
        password_hash = :passwordHash, 
        password_salt = :passwordSalt, 
        last_password_change = :currentTime, 
        updated_at = :currentTime 
        WHERE id = :userId
    """)
    suspend fun updatePassword(
        userId: Long,
        passwordHash: String,
        passwordSalt: String,
        currentTime: Long
    )

    /**
     * Gets the count of registered users.
     *
     * @return Total number of users
     */
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    /**
     * Gets the count of active users.
     *
     * @return Number of active users
     */
    @Query("SELECT COUNT(*) FROM users WHERE is_active = 1")
    suspend fun getActiveUserCount(): Int

    /**
     * Deletes a user by ID
     */
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: Long)
}
