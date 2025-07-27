package com.example.fitnesstrackerapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * UserDao
 *
 * Data Access Object (DAO) for user authentication.
 * Handles inserting new users and verifying login credentials.
 */
@Dao
interface UserDao {

    /**
     * Inserts a new user into the "users" table.
     *
     * If a user with the same email already exists, insertion will fail (ABORT),
     * ensuring email uniqueness.
     *
     * @param user The User object containing email and password.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: User)

    /**
     * Authenticates a user during login by checking email and password.
     *
     * @param email Email entered by the user.
     * @param password Password entered by the user.
     * @return The matching User object if credentials are correct, otherwise null.
     *
     * ⚠️ WARNING: This approach stores passwords in plain text.
     * It is suitable for academic purposes only. Use encryption or hashing in production.
     */
    @Query("SELECT * FROM users WHERE email = :email AND password = :password")
    suspend fun authenticate(email: String, password: String): User?
}
