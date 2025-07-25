package com.example.fitnesstrackerapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Workout
 *
 * Represents a workout entry in the Room database.
 * Each row stores details about an exercise session.
 */
@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,            // Auto-generated unique ID
    val type: String,           // Workout type (e.g., "Running", "Cycling")
    val duration: Int,          // Duration in minutes
    val distance: Double,       // Distance in kilometers
    val calories: Int,          // Calories burned
    val notes: String,          // Optional notes
    val date: Long              // Timestamp (epoch millis)
)
