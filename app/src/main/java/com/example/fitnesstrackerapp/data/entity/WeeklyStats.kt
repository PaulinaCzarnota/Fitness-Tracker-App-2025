package com.example.fitnesstrackerapp.data.entity

/**
 * Weekly Statistics Data Class
 *
 * Represents aggregated weekly fitness data
 *
 * Data class representing weekly workout statistics
 *
 * @property workoutCount Total number of workouts in the week
 * @property totalDuration Total workout duration in minutes
 * @property totalCalories Total calories burned during workouts
 * @property totalDistance Total distance covered in kilometers
 */
data class WeeklyStats(
    val workoutCount: Int = 0,
    val totalDuration: Int = 0,
    val totalCalories: Int = 0,
    val totalDistance: Float = 0f,
)
