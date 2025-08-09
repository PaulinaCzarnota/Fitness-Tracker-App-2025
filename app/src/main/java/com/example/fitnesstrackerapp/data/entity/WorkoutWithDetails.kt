package com.example.fitnesstrackerapp.data.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Data class representing a workout with additional calculated statistics.
 *
 * Uses Room's @Embedded annotation to include the base workout data and adds
 * computed fields for tracking progress and performance metrics.
 */
data class WorkoutWithDetails(
    @Embedded
    val workout: Workout,
    val workoutNumber: Int,
    val avgDuration: Float,
    val avgCalories: Float,
    val weeklyAverage: Float? = null,
    val monthlyAverage: Float? = null,
    @Relation(
        parentColumn = "userId",
        entityColumn = "userId",
        entity = User::class,
    )
    val user: User? = null,
)
