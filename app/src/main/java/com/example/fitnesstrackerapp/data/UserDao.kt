package com.example.fitnesstrackerapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * UserDao
 *
 * Data Access Object for the "users" table using Room.
 * Provides methods for user registration, login, and fetching user info.
 */
@Dao
interface UserDao {

    /**
     * Inserts a new user into the database.
     * Will fail if the email already exists due to the unique constraint.
     *
     * @param user The user entity to insert.
     * @return The newly inserted row ID.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: User): Long

    /**
     * Retrieves a user by their normalized email (case-insensitive).
     *
     * @param email The email to search for.
     * @return The matching User object, or null if not found.
     */
    @Query("""
        SELECT * FROM users
        WHERE LOWER(email) = LOWER(:email)
        LIMIT 1
    """)
    suspend fun getUserByEmail(email: String): User?

    /**
     * Authenticates the user by matching email and hashed password.
     *
     * @param email The login email (not case-sensitive).
     * @param hash The password hash (SHA-256 hex string).
     * @return The authenticated User if credentials match, or null.
     */
    @Query("""
        SELECT * FROM users
        WHERE LOWER(email) = LOWER(:email)
          AND password_hash = :hash
        LIMIT 1
    """)
    suspend fun authenticate(email: String, hash: String): User?

    /**
     * Retrieves a user by their unique ID.
     *
     * @param userId The primary key ID.
     * @return The corresponding User, or null if not found.
     */
    @Query("""
        SELECT * FROM users
        WHERE id = :userId
        LIMIT 1
    """)
    suspend fun getUserById(userId: Int): User?
}
