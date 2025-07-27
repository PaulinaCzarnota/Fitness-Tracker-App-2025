package com.example.fitnesstrackerapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * User
 *
 * Represents a registered user of the Fitness Tracker App.
 * This data class defines a Room entity stored in the "users" table.
 *
 * @property email The unique email address of the user, used as the primary key.
 * @property password The user's password (stored in plain text here for simplicity).
 */
@Entity(tableName = "users")
data class User(

    /**
     * Primary key: The user's unique email address.
     * Room will enforce uniqueness and use this to identify users.
     */
    @PrimaryKey
    val email: String,

    /**
     * The user's password.
     * WARNING: Stored in plain text for assignment/demo purposes.
     * In production apps, always hash and salt passwords before storing them.
     */
    val password: String
)
