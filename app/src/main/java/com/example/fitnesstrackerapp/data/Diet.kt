package com.example.fitnesstrackerapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Diet.kt
 *
 * Entity class representing a food intake entry for diet and nutrition tracking.
 * Defines the schema for the 'diets' table in the Room database.
 */
@Entity(tableName = "diets")
data class Diet(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // Unique identifier for each diet entry (auto-incremented)

    val food: String, // Description of the food item consumed

    val calories: Int, // Caloric value of the item

    val date: Long = System.currentTimeMillis() // Timestamp of consumption (defaults to now)
)
