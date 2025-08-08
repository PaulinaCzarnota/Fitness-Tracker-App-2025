/**
 * Entity representing a fitness goal in the application.
 *
 * Goals can be various types: weight loss, distance running, workout frequency, etc.
 * Progress is tracked against the target value.
 */

package com.example.fitnesstrackerapp.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Goal entity for tracking user fitness objectives.
 */
@Entity(
    tableName = "goals",
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
        Index(value = ["goal_type"]),
        Index(value = ["status"]),
        Index(value = ["target_date"])
    ]
)
data class Goal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "user_id")
    val userId: Long,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "goal_type")
    val goalType: GoalType,

    @ColumnInfo(name = "target_value")
    val targetValue: Double,

    @ColumnInfo(name = "current_value")
    val currentValue: Double = 0.0,

    @ColumnInfo(name = "unit")
    val unit: String, // kg, km, times, calories, steps, etc.

    @ColumnInfo(name = "target_date")
    val targetDate: Date,

    @ColumnInfo(name = "start_date")
    val startDate: Date = Date(),

    @ColumnInfo(name = "status")
    val status: GoalStatus = GoalStatus.ACTIVE,

    @ColumnInfo(name = "reminder_enabled")
    val reminderEnabled: Boolean = true,

    @ColumnInfo(name = "reminder_frequency")
    val reminderFrequency: String? = null, // daily, weekly, etc.

    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date()
) {
    /**
     * Calculate the progress percentage towards the goal.
     */
    fun getProgressPercentage(): Float {
        return if (targetValue > 0) {
            (currentValue / targetValue * 100).toFloat().coerceIn(0f, 100f)
        } else {
            0f
        }
    }

    /**
     * Check if the goal is completed.
     */
    fun isCompleted(): Boolean {
        return currentValue >= targetValue || status == GoalStatus.COMPLETED
    }

    /**
     * Check if the goal is achieved (alias for isCompleted for compatibility).
     */
    val isAchieved: Boolean
        get() = isCompleted()

    /**
     * Check if goal is active (alias for compatibility)
     */
    val isActive: Boolean
        get() = status == GoalStatus.ACTIVE

    /**
     * Get remaining value to reach the goal.
     */
    fun getRemainingValue(): Double {
        return (targetValue - currentValue).coerceAtLeast(0.0)
    }
}
