package com.example.fitnesstrackerapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * User
 *
 * Represents a registered user in the local Room database.
 * Used for login authentication and account management.
 *
 * Security:
 * - Email is normalized before saving (trimmed + lowercase).
 * - Password must be stored as a SHA-256 hash, not plaintext.
 */
@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)] // Unique constraint on email
)
data class User(

    /**
     * Auto-generated primary key for the user.
     */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    /**
     * User's login email (normalized).
     */
    @ColumnInfo(name = "email")
    val email: String,

    /**
     * SHA-256 hashed password in hexadecimal string format.
     */
    @ColumnInfo(name = "password_hash")
    val passwordHash: String
)
