package com.example.fitnesstrackerapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.fitnesstrackerapp.data.entity.User
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for User entity operations.
 *
 * Responsibilities:
 * - Insert, update, delete users
 * - Query users by email, ID, and status
 * - Handle user authentication data
 */
@Dao
interface UserDao {

    /**
     * Inserts a new user into the database.
     *
     * @param user User entity to insert
     * @return The ID of the inserted user
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
    suspend fun getUserById(userId: Int): User?

    /**
     * Gets a user by their username.
     *
     * @param username Username to search for
     * @return User entity or null if not found
     */
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    /**
     * Gets all active users.
     *
     * @return Flow of list of active users
     */
    @Query("SELECT * FROM users WHERE is_active = 1 ORDER BY created_at DESC")
    fun getAllActiveUsers(): Flow<List<User>>

    /**
     * Gets all users (including inactive).
     *
     * @return Flow of list of all users
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
    suspend fun updateLastLogin(userId: Long, lastLogin: Long)

    /**
     * Increments failed login attempts for a user.
     *
     * @param email User's email
     */
    @Query("UPDATE users SET failed_login_attempts = failed_login_attempts + 1, updated_at = :currentTime WHERE email = :email")
    suspend fun incrementFailedLoginAttempts(email: String, currentTime: Long)

    /**
     * Resets failed login attempts for a user.
     *
     * @param userId User ID
     */
    @Query("UPDATE users SET failed_login_attempts = 0, is_account_locked = 0, updated_at = :currentTime WHERE id = :userId")
    suspend fun resetFailedLoginAttempts(userId: Long, currentTime: Long)

    /**
     * Locks a user account.
     *
     * @param userId User ID
     */
    @Query("UPDATE users SET is_account_locked = 1, updated_at = :currentTime WHERE id = :userId")
    suspend fun lockAccount(userId: Long, currentTime: Long)

    /**
     * Unlocks a user account.
     *
     * @param userId User ID
     */
    @Query("UPDATE users SET is_account_locked = 0, failed_login_attempts = 0, updated_at = :currentTime WHERE id = :userId")
    suspend fun unlockAccount(userId: Long, currentTime: Long)

    /**
     * Deactivates a user account.
     *
     * @param userId User ID
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
}
