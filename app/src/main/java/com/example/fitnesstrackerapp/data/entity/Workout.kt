package com.example.fitnesstrackerapp.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Enum representing different types of workouts
 */
enum class WorkoutType {
    RUNNING, CYCLING, WEIGHTLIFTING
}

/**
 * Entity representing a workout session in the Fitness Tracker application.
 *
 * This entity stores detailed workout information including:
 * - Workout type and basic information
 * - Duration, distance, and calories burned
 * - Heart rate data and steps count
 * - Personal notes and ratings
 * - Environmental conditions
 */
@Entity(
    tableName = "workouts",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["workoutType"]),
        Index(value = ["startTime"]),
        Index(value = ["userId", "startTime"])
    ]
)
data class Workout(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "userId")
    val userId: Long,

    @ColumnInfo(name = "workoutType")
    val workoutType: WorkoutType,

    @ColumnInfo(name = "startTime")
    val startTime: LocalDateTime,

    @ColumnInfo(name = "endTime")
    val endTime: LocalDateTime? = null,

    @ColumnInfo(name = "duration")
    val duration: Int = 0, // in minutes

    @ColumnInfo(name = "distance")
    val distance: Float = 0f, // in kilometers

    @ColumnInfo(name = "caloriesBurned")
    val caloriesBurned: Int = 0,

    @ColumnInfo(name = "stepCount")
    val stepCount: Int = 0,

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "createdAt")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @ColumnInfo(name = "updatedAt")
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * Extension function to calculate estimated calories burned based on workout type and duration
     */
    fun getEstimatedCalories(): Int {
        return when (workoutType) {
            WorkoutType.RUNNING -> (duration * 10.0).toInt()
            WorkoutType.CYCLING -> (duration * 7.0).toInt()
            WorkoutType.WEIGHTLIFTING -> (duration * 5.0).toInt()
        }
    }
}
