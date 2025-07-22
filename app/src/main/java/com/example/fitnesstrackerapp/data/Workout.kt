package com.example.fitnesstrackerapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity class representing a workout log entry in the local Room database.
 * This class defines the schema for the 'workouts' table.
 */
@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,              // Auto-generated unique ID for each workout

    val type: String,             // Type of workout (e.g., Running, Cycling, Swimming)

    val duration: Int,            // Duration in minutes

    val distance: Double,         // Distance in kilometers

    val calories: Int,            // Calories burned during the workout

    val notes: String,            // Optional user notes

    val date: Long                // Timestamp (milliseconds since epoch) used for filtering and sorting
)
