package com.example.fitnesstrackerapp.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entity representing step tracking data in the Fitness Tracker application.
 *
 * This entity stores daily step count information including:
 * - Daily step counting using phone sensors
 * - Distance and calorie calculations based on steps
 * - Historical step data for progress tracking
 * - Integration with workout sessions
 */
@Entity(
    tableName = "steps",
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
        Index(value = ["date"]),
        Index(value = ["userId", "date"], unique = true)
    ]
)
data class Step(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "userId")
    val userId: Long,

    val date: Date,

    @ColumnInfo(name = "stepCount")
    val stepCount: Int,

    @ColumnInfo(name = "distanceMeters")
    val distanceMeters: Float = 0f,

    @ColumnInfo(name = "caloriesBurned")
    val caloriesBurned: Float = 0f,

    @ColumnInfo(name = "goalSteps")
    val goalSteps: Int = 10000,

    @ColumnInfo(name = "createdAt")
    val createdAt: Date = Date(),

    @ColumnInfo(name = "updatedAt")
    val updatedAt: Date = Date()
) {
    /**
     * Checks if the daily step goal has been reached.
     */
    fun isGoalReached(): Boolean {
        return stepCount >= goalSteps
    }

    /**
     * Calculates the progress percentage towards the goal.
     */
    fun getProgressPercentage(): Float {
        return if (goalSteps > 0) {
            ((stepCount.toFloat() / goalSteps.toFloat()) * 100).coerceAtMost(100f)
        } else {
            0f
        }
    }

    /**
     * Gets the remaining steps needed to reach the goal.
     */
    fun getRemainingSteps(): Int {
        return (goalSteps - stepCount).coerceAtLeast(0)
    }

    /**
     * Estimates distance in kilometers based on step count.
     * Assumes average step length of 0.76 meters.
     */
    fun getEstimatedDistanceKm(): Float {
        return (stepCount * 0.76f) / 1000f
    }

    /**
     * Estimates calories burned based on step count.
     * Rough estimate: 0.04 calories per step for average adult.
     */
    fun getEstimatedCalories(): Float {
        return stepCount * 0.04f
    }

    companion object {
        const val STEP_LENGTH_METERS = 0.76f
    }
}
