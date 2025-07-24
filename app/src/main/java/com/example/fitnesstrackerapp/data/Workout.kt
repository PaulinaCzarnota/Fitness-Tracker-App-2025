package com.example.fitnesstrackerapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Workout
 *
 * Represents a workout entry in the Room database.
 * Defines the structure of the 'workouts' table.
 */
@Entity(tableName = "workouts")
data class Workout(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // Auto-generated unique ID for each workout

    val type: String,         // Type of workout (e.g., "Running", "Cycling")
    val duration: Int,        // Duration in minutes
    val distance: Double,     // Distance in kilometers
    val calories: Int,        // Calories burned
    val notes: String,        // Optional notes or comments
    val date: Long            // Timestamp of workout (milliseconds since epoch)
)
