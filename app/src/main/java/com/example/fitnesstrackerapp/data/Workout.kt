package com.example.fitnesstrackerapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Workout
 *
 * Data class that represents a single workout entry in the Room database.
 * Used to log workouts such as running, cycling, yoga, etc.
 */
@Entity(tableName = "workouts")
data class Workout(

    /**
     * Unique ID for each workout.
     * Room auto-generates this value.
     */
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    /**
     * Type of workout (e.g., "Running", "Cycling", "Yoga").
     */
    val type: String,

    /**
     * Duration of the workout in minutes.
     */
    val duration: Int,

    /**
     * Distance covered during the workout (in kilometers).
     */
    val distance: Double,

    /**
     * Calories burned during the workout.
     */
    val calories: Int,

    /**
     * Optional notes entered by the user (e.g., "Evening run", "Felt strong").
     * Default is an empty string.
     */
    val notes: String = "",

    /**
     * Date and time the workout was logged (in Unix time milliseconds).
     * Defaults to current system time.
     */
    val date: Long = System.currentTimeMillis()
)
