/**
 * Step entity and related classes for the Fitness Tracker application.
 *
 * This file contains the Step entity which stores comprehensive daily step tracking data
 * including step counts, distance calculations, calorie estimates, and goal progress.
 * The entity uses Room database annotations for optimal storage and retrieval performance.
 *
 * Key Features:
 * - Daily step counting using phone sensors
 * - Distance and calorie calculations based on steps
 * - Goal progress tracking with percentage calculations
 * - Historical step data for analytics and progress visualization
 * - Integration with workout sessions and activity tracking
 * - Foreign key relationship with User entity for data integrity
 */

package com.example.fitnesstrackerapp.data.entity

import android.annotation.SuppressLint
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Calendar
import java.util.Date

/**
 * Entity representing daily step tracking data in the Fitness Tracker application.
 *
 * This entity stores comprehensive step information including daily counts,
 * distance calculations, calorie estimates, and goal tracking. All step records
 * are associated with a specific user and date through foreign key relationship.
 *
 * Database Features:
 * - Indexed for efficient querying by user, date, and user-date combinations
 * - Unique constraint on user_id and date to prevent duplicate daily records
 * - Foreign key constraint ensures data integrity with User entity
 * - Cascading delete removes step data when user is deleted
 */
@Entity(
    tableName = "steps",
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
        Index(value = ["date"]),
        Index(value = ["user_id", "date"], unique = true),
    ],
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
    val updatedAt: Date = Date(),
) {
    /**
     * Calculates progress percentage towards daily step goal.
     * @return Progress percentage from 0 to 100
     */
    fun getProgressPercentage(): Float {
        return if (goal > 0) (count.toFloat() / goal * 100).coerceIn(0f, 100f) else 0f
    }

    /**
     * Checks if daily step goal is achieved.
     * @return true if step count meets or exceeds goal
     */
    fun isGoalAchieved(): Boolean {
        return count >= goal
    }

    /**
     * Gets steps as alias for compatibility.
     */
    val steps: Int
        get() = count

    /**
     * Calculates distance in kilometers.
     * @return Distance in kilometers
     */
    fun getDistanceKm(): Float {
        return distanceMeters / 1000f
    }

    /**
     * Gets calories as alias for compatibility.
     */
    val calories: Float
        get() = caloriesBurned

    /**
     * Gets formatted date string for display.
     * @return Formatted date string (DD/MM/YYYY)
     */
    fun getFormattedDate(): String {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return "${calendar.get(
            Calendar.DAY_OF_MONTH,
        )}/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}"
    }

    /**
     * Gets formatted distance string for display.
     * @return Formatted distance string with unit
     */
    fun getFormattedDistance(): String {
        val km = getDistanceKm()
        return if (km >= 1.0f) {
            "%.2f km".format(km)
        } else {
            "%.0f m".format(distanceMeters)
        }
    }

    /**
     * Gets formatted step progress string for display.
     * @return Progress string (e.g., "8,500 / 10,000 steps (85%)")
     */
    @SuppressLint("DefaultLocale")
    fun getFormattedProgress(): String {
        val percentage = getProgressPercentage().toInt()
        return "${String.format("%,d", count)} / ${String.format("%,d", goal)} steps ($percentage%)"
    }

    /**
     * Calculates remaining steps to reach goal.
     * @return Steps remaining to goal (0 if goal is achieved)
     */
    fun getRemainingSteps(): Int {
        return (goal - count).coerceAtLeast(0)
    }

    /**
     * Estimates calories burned per step based on user data.
     * @param userWeight User weight in kg (default: 70kg)
     * @return Estimated calories per step
     */
    fun getCaloriesPerStep(userWeight: Float = 70f): Float {
        // Basic formula: calories per step = (weight in kg * 0.57) / 1000
        return (userWeight * 0.57f) / 1000f
    }

    /**
     * Estimates distance per step based on user height.
     * @param userHeight User height in cm (default: 170cm)
     * @return Estimated distance per step in meters
     */
    fun getDistancePerStep(userHeight: Float = 170f): Float {
        // Basic formula: step length = height * 0.414
        return (userHeight * 0.414f) / 100f
    }

    /**
     * Gets activity level based on step count.
     * @return Activity level as string
     */
    fun getActivityLevel(): String {
        return when {
            count >= 12000 -> "Highly Active"
            count >= 10000 -> "Active"
            count >= 7500 -> "Somewhat Active"
            count >= 5000 -> "Low Active"
            else -> "Sedentary"
        }
    }

    /**
     * Validates if the step data is consistent and valid.
     * @return true if step data is valid, false otherwise
     */
    fun isValid(): Boolean {
        return count >= 0 &&
            goal > 0 &&
            caloriesBurned >= 0 &&
            distanceMeters >= 0 &&
            activeMinutes >= 0
    }

    companion object {
        const val DEFAULT_STEP_GOAL = 10000
        const val MIN_STEP_COUNT = 0
        const val MAX_REALISTIC_STEPS = 50000 // For validation
        const val AVERAGE_STEP_LENGTH_CM = 70f // Average step length
        const val CALORIES_PER_STEP_AVERAGE = 0.04f // Average calories per step
    }
}
