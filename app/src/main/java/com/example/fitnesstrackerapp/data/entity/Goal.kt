package com.example.fitnesstrackerapp.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entity representing a fitness goal in the Fitness Tracker application.
 *
 * This entity stores user fitness goals with tracking capabilities including:
 * - Support for different goal types (distance, workout frequency, weight targets, etc.)
 * - Progress tracking capabilities
 * - Target values and deadlines
 * - Achievement status and notifications
 */

@Entity(
    tableName = "goals",
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
        Index(value = ["goalType"]),
        Index(value = ["isActive"]),
        Index(value = ["targetDate"]),
        Index(value = ["userId", "isActive"])
    ]
)
data class Goal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "userId")
    val userId: Long,

    val title: String,

    val description: String? = null,

    @ColumnInfo(name = "goalType")
    val goalType: GoalType,

    @ColumnInfo(name = "targetValue")
    val targetValue: Float,

    @ColumnInfo(name = "currentValue")
    val currentValue: Float = 0f,

    @ColumnInfo(name = "unit")
    val unit: String, // steps, km, workouts, kg, etc.

    @ColumnInfo(name = "targetDate")
    val targetDate: Date,

    @ColumnInfo(name = "isActive")
    val isActive: Boolean = true,

    @ColumnInfo(name = "isAchieved")
    val isAchieved: Boolean = false,

    @ColumnInfo(name = "achievedDate")
    val achievedDate: Date? = null,

    @ColumnInfo(name = "reminderEnabled")
    val reminderEnabled: Boolean = true,

    @ColumnInfo(name = "reminderTime")
    val reminderTime: String? = null, // HH:mm format

    @ColumnInfo(name = "createdAt")
    val createdAt: Date = Date(),

    @ColumnInfo(name = "updatedAt")
    val updatedAt: Date = Date()
) {
    /**
     * Calculates the progress percentage towards the goal.
     * @return Progress percentage (0-100)
     */
    fun getProgressPercentage(): Float {
        return if (targetValue > 0) {
            ((currentValue / targetValue) * 100f).coerceAtMost(100f)
        } else {
            0f
        }
    }

    /**
     * Checks if the goal has been achieved.
     * @return true if current value meets or exceeds target value
     */
    fun isGoalAchieved(): Boolean {
        return currentValue >= targetValue
    }

    /**
     * Gets the remaining value needed to achieve the goal.
     * @return Remaining value needed, or 0 if goal is achieved
     */
    fun getRemainingValue(): Float {
        return (targetValue - currentValue).coerceAtLeast(0f)
    }

    /**
     * Checks if the goal is overdue.
     * @return true if target date has passed and goal is not achieved
     */
    fun isOverdue(): Boolean {
        return !isAchieved && targetDate.before(Date())
    }

    companion object {
        const val TYPE_DAILY_STEPS = "daily_steps"
        const val TYPE_WEEKLY_WORKOUTS = "weekly_workouts"
        const val TYPE_DISTANCE = "distance"
        const val TYPE_WEIGHT_LOSS = "weight_loss"
        const val TYPE_CALORIES = "calories"
        const val TYPE_EXERCISE_MINUTES = "exercise_minutes"
    }
}

/**
 * Enum representing different types of fitness goals
 */
enum class GoalType {
    DAILY_STEPS,
    WEEKLY_WORKOUTS,
    MONTHLY_DISTANCE,
    WEIGHT_TARGET,
    CALORIES_BURNED,
    EXERCISE_MINUTES
}
