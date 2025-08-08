/**
 * Workout entity and related classes for the Fitness Tracker application.
 *
 * This file contains the Workout entity which stores comprehensive workout session data
 * including timing, performance metrics, health data, and user preferences. The entity
 * uses Room database annotations for optimal storage and retrieval performance.
 *
 * Key Features:
 * - Comprehensive workout tracking with timing and performance data
 * - Heart rate monitoring and health metrics
 * - Environmental condition tracking (weather, temperature)
 * - User ratings and notes for workout sessions
 * - Calculated fields for pace, speed, and calories per minute
 * - Foreign key relationship with User entity for data integrity
 */

package com.example.fitnesstrackerapp.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entity representing a workout session in the Fitness Tracker application.
 *
 * This entity stores detailed workout information including workout type, timing data,
 * performance metrics, health monitoring data, and environmental conditions.
 * All workouts are associated with a specific user through foreign key relationship.
 *
 * Database Features:
 * - Indexed for efficient querying by user, workout type, and date
 * - Foreign key constraint ensures data integrity with User entity
 * - Cascading delete removes workouts when user is deleted
 */
@Entity(
    tableName = "workouts",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["workoutType"]),
        Index(value = ["startTime"]),
        Index(value = ["userId", "startTime"]),
    ],
)
data class Workout(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "userId")
    val userId: Long,

    @ColumnInfo(name = "workoutType")
    val workoutType: WorkoutType,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "startTime")
    val startTime: Date,

    @ColumnInfo(name = "endTime")
    val endTime: Date? = null,

    @ColumnInfo(name = "duration")
    val duration: Int = 0, // Duration in minutes

    @ColumnInfo(name = "distance")
    val distance: Float = 0f, // Distance in kilometers

    @ColumnInfo(name = "caloriesBurned")
    val caloriesBurned: Int = 0,

    @ColumnInfo(name = "steps")
    val steps: Int = 0,

    @ColumnInfo(name = "avgHeartRate")
    val avgHeartRate: Int? = null, // Average heart rate in BPM

    @ColumnInfo(name = "maxHeartRate")
    val maxHeartRate: Int? = null, // Maximum heart rate in BPM

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "rating")
    val rating: Int? = null, // Rating from 1-5

    @ColumnInfo(name = "weatherCondition")
    val weatherCondition: String? = null,

    @ColumnInfo(name = "temperature")
    val temperature: Float? = null, // Temperature in Celsius

    @ColumnInfo(name = "createdAt")
    val createdAt: Date = Date(),

    @ColumnInfo(name = "updatedAt")
    val updatedAt: Date = Date(),
) {
    /**
     * Get type as alias for compatibility
     */
    val type: WorkoutType
        get() = workoutType

    /**
     * Get calories as alias for compatibility
     */
    val calories: Int
        get() = caloriesBurned

    /**
     * Get date as alias for compatibility
     */
    val date: Date
        get() = startTime

    /**
     * Checks if this workout is completed (has an end time).
     *
     * @return true if the workout has been completed and ended, false otherwise
     */
    fun isCompleted(): Boolean = endTime != null

    /**
     * Gets the workout duration in minutes.
     *
     * @return Duration of the workout in minutes
     */
    fun getDurationMinutes(): Int = duration

    /**
     * Gets formatted duration as a human-readable string.
     *
     * @return Formatted duration string (e.g., "1h 30m" or "45m")
     */
    fun getFormattedDuration(): String {
        val hours = duration / 60
        val minutes = duration % 60
        return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }

    /**
     * Calculates calories burned per minute of workout.
     *
     * @return Calories burned per minute, or 0.0 if duration is zero
     */
    fun getCaloriesPerMinute(): Float {
        return if (duration > 0) caloriesBurned.toFloat() / duration else 0f
    }

    /**
     * Calculates average pace in minutes per kilometer.
     *
     * @return Average pace in minutes per km, or null if distance/duration is zero
     */
    fun getAveragePace(): Float? {
        return if (distance > 0 && duration > 0) duration / distance else null
    }

    /**
     * Calculates average speed in kilometers per hour.
     *
     * @return Average speed in km/h, or null if distance/duration is zero
     */
    fun getAverageSpeed(): Float? {
        return if (distance > 0 && duration > 0) (distance / duration) * 60f else null
    }

    /**
     * Gets workout duration in milliseconds based on start and end times.
     * This provides more precise duration calculation than the stored duration field.
     *
     * @return Duration in milliseconds, or 0 if workout is not completed
     */
    fun getDurationInMillis(): Long {
        return if (endTime != null) endTime.time - startTime.time else 0L
    }

    /**
     * Gets formatted pace string for display.
     *
     * @return Formatted pace string (e.g., "5:30 min/km") or null if cannot calculate
     */
    fun getFormattedPace(): String? {
        return getAveragePace()?.let { pace ->
            val minutes = pace.toInt()
            val seconds = ((pace - minutes) * 60).toInt()
            "$minutes:${seconds.toString().padStart(2, '0')} min/km"
        }
    }

    /**
     * Gets formatted speed string for display.
     *
     * @return Formatted speed string (e.g., "12.5 km/h") or null if cannot calculate
     */
    fun getFormattedSpeed(): String? {
        return getAverageSpeed()?.let { speed ->
            "%.1f km/h".format(speed)
        }
    }

    /**
     * Validates if the workout data is consistent and valid.
     *
     * @return true if workout data is valid, false otherwise
     */
    fun isValid(): Boolean {
        return duration >= 0 &&
            distance >= 0 &&
            caloriesBurned >= 0 &&
            steps >= 0 &&
            (rating == null || rating in MIN_RATING..MAX_RATING) &&
            (avgHeartRate == null || avgHeartRate in 30..220) &&
            (maxHeartRate == null || maxHeartRate in 30..220) &&
            (avgHeartRate == null || maxHeartRate == null || avgHeartRate <= maxHeartRate)
    }

    /**
     * Gets workout intensity based on duration and calories.
     *
     * @return Intensity level as string
     */
    fun getIntensity(): String {
        val caloriesPerMinute = getCaloriesPerMinute()
        return when {
            caloriesPerMinute >= 15 -> "High"
            caloriesPerMinute >= 8 -> "Moderate"
            caloriesPerMinute >= 4 -> "Light"
            else -> "Very Light"
        }
    }

    companion object {
        const val MIN_RATING = 1
        const val MAX_RATING = 5
        const val DEFAULT_DURATION = 0
        const val DEFAULT_DISTANCE = 0f
        const val DEFAULT_CALORIES = 0
    }
}
