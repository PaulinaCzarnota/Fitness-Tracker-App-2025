/**
 * Goal entity and related classes for the Fitness Tracker application.
 *
 * This file contains the Goal entity which stores comprehensive fitness goal data
 * including goal types, progress tracking, target dates, and reminder settings.
 * The entity uses Room database annotations for optimal storage and retrieval performance.
 *
 * Key Features:
 * - Multiple goal types (weight, distance, frequency, calories, steps)
 * - Progress tracking with percentage calculations
 * - Target date management and deadline tracking
 * - Reminder system with customizable frequency
 * - Status management (active, completed, paused, cancelled)
 * - Foreign key relationship with User entity for data integrity
 */

package com.example.fitnesstrackerapp.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Entity representing a fitness goal in the Fitness Tracker application.
 *
 * This entity stores comprehensive goal information including target values,
 * current progress, deadlines, and reminder settings. All goals are associated
 * with a specific user through foreign key relationship.
 *
 * Database Features:
 * - Indexed for efficient querying by user, goal type, status, and target date
 * - Foreign key constraint ensures data integrity with User entity
 * - Cascading delete removes goals when user is deleted
 */
@Entity(
    tableName = "goals",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["goal_type"]),
        Index(value = ["status"]),
        Index(value = ["target_date"]),
        Index(value = ["user_id", "status"]),
    ],
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
    val updatedAt: Date = Date(),
) {
    /**
     * Calculates the progress percentage towards the goal.
     * @return Progress percentage from 0 to 100
     */
    fun getProgressPercentage(): Float {
        return if (targetValue > 0) {
            (currentValue / targetValue * 100).toFloat().coerceIn(0f, 100f)
        } else {
            0f
        }
    }

    /**
     * Checks if the goal is completed (moved to GoalExtensions.kt for enhanced logic)
     * @deprecated Use the extension function in GoalExtensions.kt for improved completion logic
     * @return true if current value meets or exceeds target value or status is completed
     */
    @Deprecated(
        "Use isCompleted() extension function for enhanced goal-type specific completion logic",
        ReplaceWith("this.isCompleted()", "com.example.fitnesstrackerapp.data.entity.isCompleted")
    )
    fun isCompletedLegacy(): Boolean {
        return currentValue >= targetValue || status == GoalStatus.COMPLETED
    }

    /**
     * Checks if the goal is achieved (alias for isCompleted for compatibility).
     * @return true if goal is achieved
     */
    val isAchieved: Boolean
        get() = isCompleted()

    /**
     * Checks if goal is active (alias for compatibility).
     * @return true if goal status is active
     */
    val isActive: Boolean
        get() = status == GoalStatus.ACTIVE

    /**
     * Gets remaining value to reach the goal.
     * @return Remaining value needed to complete the goal (0 if already completed)
     */
    fun getRemainingValue(): Double {
        return (targetValue - currentValue).coerceAtLeast(0.0)
    }

    /**
     * Gets the number of days remaining until target date.
     * @return Days remaining (negative if overdue)
     */
    fun getDaysRemaining(): Long {
        val now = Date()
        val diffInMillis = targetDate.time - now.time
        return TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS)
    }

    /**
     * Checks if the goal is overdue.
     * @return true if target date has passed and goal is not completed
     */
    fun isOverdue(): Boolean {
        return !isCompleted() && getDaysRemaining() < 0
    }

    /**
     * Gets formatted target date string.
     * @return Formatted date string
     */
    fun getFormattedTargetDate(): String {
        val calendar = Calendar.getInstance()
        calendar.time = targetDate
        return "${calendar.get(
            Calendar.DAY_OF_MONTH,
        )}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}"
    }

    /**
     * Gets formatted progress string for display.
     * @return Progress string (e.g., "75 / 100 kg (75%)")
     */
    fun getFormattedProgress(): String {
        val percentage = getProgressPercentage().toInt()
        return "${currentValue.toInt()} / ${targetValue.toInt()} $unit ($percentage%)"
    }

    /**
     * Gets goal urgency level based on remaining time and progress.
     * @return Urgency level as string
     */
    fun getUrgencyLevel(): String {
        val daysRemaining = getDaysRemaining()
        val progressPercentage = getProgressPercentage()

        return when {
            isCompleted() -> "Completed"
            isOverdue() -> "Overdue"
            daysRemaining <= 3 && progressPercentage < 80 -> "Critical"
            daysRemaining <= 7 && progressPercentage < 60 -> "High"
            daysRemaining <= 14 && progressPercentage < 40 -> "Medium"
            else -> "Low"
        }
    }

    /**
     * Calculates required daily progress to meet goal on time.
     * @return Daily progress needed, or null if goal is completed/overdue
     */
    fun getRequiredDailyProgress(): Double? {
        val daysRemaining = getDaysRemaining()
        val remainingValue = getRemainingValue()

        return if (daysRemaining > 0 && remainingValue > 0) {
            remainingValue / daysRemaining
        } else {
            null
        }
    }

    /**
     * Gets goal duration in days.
     * @return Total duration from start date to target date
     */
    fun getGoalDurationDays(): Long {
        val diffInMillis = targetDate.time - startDate.time
        return TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS)
    }

    /**
     * Validates if the goal data is consistent and valid.
     * @return true if goal data is valid, false otherwise
     */
    fun isValid(): Boolean {
        return targetValue > 0 &&
            currentValue >= 0 &&
            targetDate.after(startDate) &&
            title.isNotBlank() &&
            unit.isNotBlank()
    }

    companion object {
        const val DEFAULT_TARGET_VALUE = 0.0
        const val DEFAULT_CURRENT_VALUE = 0.0
        const val MIN_PROGRESS_PERCENTAGE = 0f
        const val MAX_PROGRESS_PERCENTAGE = 100f
    }
}
