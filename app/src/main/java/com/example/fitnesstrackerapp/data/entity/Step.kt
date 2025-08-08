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
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["date"]),
        Index(value = ["user_id", "date"], unique = true)
    ]
)
data class Step(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "user_id")
    val userId: Long,

    @ColumnInfo(name = "step_count")
    val count: Int,

    @ColumnInfo(name = "step_goal")
    val goal: Int = 10000,

    @ColumnInfo(name = "date")
    val date: Date,

    @ColumnInfo(name = "calories_burned")
    val caloriesBurned: Float = 0f,

    @ColumnInfo(name = "distance_meters")
    val distanceMeters: Float = 0f,

    @ColumnInfo(name = "active_minutes")
    val activeMinutes: Int = 0,

    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date()
) {
    /**
     * Calculate progress percentage towards daily goal.
     */
    fun getProgressPercentage(): Float {
        return if (goal > 0) (count.toFloat() / goal * 100).coerceIn(0f, 100f) else 0f
    }

    /**
     * Check if daily step goal is achieved.
     */
    fun isGoalAchieved(): Boolean {
        return count >= goal
    }

    /**
     * Get steps as alias for compatibility
     */
    val steps: Int
        get() = count

    /**
     * Calculate distance in kilometers.
     */
    fun getDistanceKm(): Float {
        return distanceMeters / 1000f
    }

    /**
     * Get calories as alias for compatibility
     */
    val calories: Float
        get() = caloriesBurned
}
