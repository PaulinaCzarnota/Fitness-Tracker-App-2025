package com.example.fitnesstrackerapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity class representing a diet entry in the local Room database.
 * Each entry includes food name, calorie amount, and the date of intake.
 */
@Entity(tableName = "diets")
data class Diet(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,                 // Unique identifier (auto-generated)

    val food: String,                // Name of the food consumed

    val calories: Int,              // Caloric content of the food

    val date: Long = System.currentTimeMillis()  // Timestamp (stored as milliseconds since epoch)
)
