package com.example.fitnesstrackerapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity that represents a workout entry in the local database.
 */
@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String,
    val duration: Int,     // Duration in minutes
    val distance: Double,  // Distance in kilometers
    val calories: Int,     // Calories burned
    val notes: String,     // Optional notes
    val date: Long         // Timestamp (System.currentTimeMillis())
)
