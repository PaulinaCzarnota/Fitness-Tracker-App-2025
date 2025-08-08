/**
 * WorkoutSet entity and related classes for the Fitness Tracker application.
 *
 * This file contains the WorkoutSet entity which tracks individual sets within a workout.
 * Each set is associated with a specific exercise and workout, recording detailed performance
 * metrics such as repetitions, weight, duration, and personal records.
 *
 * Key Features:
 * - Detailed set tracking with reps, weight, and duration
 * - Rest period tracking between sets
 * - Personal record detection and tracking
 * - Support for different set types (normal, warm-up, drop sets, etc.)
 * - Performance metrics and progression tracking
 */

package com.example.fitnesstrackerapp.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entity representing a single set within a workout session.
 *
 * This entity tracks detailed information about individual sets performed during
 * a workout, including repetitions, weight, duration, and rest periods.
 * Each set is linked to both a workout and an exercise for complete tracking.
 *
 * Database Features:
 * - Foreign key relationships to Workout and Exercise entities
 * - Indexed for efficient querying by workout, exercise, and performance metrics
 * - Cascading delete removes sets when workout is deleted
 */
@Entity(
    tableName = "workout_sets",
    foreignKeys = [
        ForeignKey(
            entity = Workout::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["workoutId"]),
        Index(value = ["exerciseId"]),
        Index(value = ["workoutId", "exerciseId"]),
        Index(value = ["workoutId", "setNumber"]),
        Index(value = ["createdAt"]),
    ],
)
data class WorkoutSet(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "workoutId")
    val workoutId: Long,

    @ColumnInfo(name = "exerciseId")
    val exerciseId: Long,

    @ColumnInfo(name = "setNumber")
    val setNumber: Int, // Order of the set within the exercise (1, 2, 3, etc.)

    @ColumnInfo(name = "setType")
    val setType: SetType = SetType.NORMAL,

    @ColumnInfo(name = "repetitions")
    val repetitions: Int = 0, // Number of reps performed

    @ColumnInfo(name = "targetReps")
    val targetReps: Int? = null, // Planned number of reps

    @ColumnInfo(name = "weight")
    val weight: Double = 0f, // Weight used in kg

    @ColumnInfo(name = "duration")
    val duration: Int = 0, // Duration in seconds (for time-based exercises)

    @ColumnInfo(name = "distance")
    val distance: Float = 0f, // Distance covered in meters (for cardio)

    @ColumnInfo(name = "restTime")
    val restTime: Int = 0, // Rest time after this set in seconds

    @ColumnInfo(name = "rpe")
    val rpe: Int? = null, // Rate of Perceived Exertion (1-10 scale)

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "isPersonalRecord")
    val isPersonalRecord: Boolean = false,

    @ColumnInfo(name = "isCompleted")
    val isCompleted: Boolean = true,

    @ColumnInfo(name = "completedAt")
    val completedAt: Date? = null,

    @ColumnInfo(name = "createdAt")
    val createdAt: Date = Date(),

    @ColumnInfo(name = "updatedAt")
    val updatedAt: Date = Date(),
) {
    /**
     * Calculates the volume for this set (weight × reps).
     *
     * @return Volume in kg×reps, or 0 if weight or reps is zero
     */
    fun getVolume(): Float = weight * repetitions

    /**
     * Calculates the intensity as percentage of 1RM (if known).
     * This is a simplified calculation and would need actual 1RM data for accuracy.
     *
     * @param oneRepMax The user's one rep max for this exercise
     * @return Intensity as percentage (0-100)
     */
    fun getIntensity(oneRepMax: Float): Float {
        return if (oneRepMax > 0) (weight / oneRepMax) * 100f else 0f
    }

    /**
     * Gets formatted weight display string.
     *
     * @return Formatted weight string (e.g., "80.5 kg" or "Bodyweight")
     */
    fun getFormattedWeight(): String {
        return if (weight > 0) "$weight kg" else "Bodyweight"
    }

    /**
     * Gets formatted duration display string.
     *
     * @return Formatted duration string (e.g., "2:30" for 2 minutes 30 seconds)
     */
    fun getFormattedDuration(): String {
        if (duration == 0) return "0:00"
        val minutes = duration / 60
        val seconds = duration % 60
        return "$minutes:${seconds.toString().padStart(2, '0')}"
    }

    /**
     * Gets formatted rest time display string.
     *
     * @return Formatted rest time string
     */
    fun getFormattedRestTime(): String {
        if (restTime == 0) return "0:00"
        val minutes = restTime / 60
        val seconds = restTime % 60
        return "$minutes:${seconds.toString().padStart(2, '0')}"
    }

    /**
     * Validates if the set data is consistent and valid.
     *
     * @return true if set data is valid, false otherwise
     */
    fun isValid(): Boolean {
        return setNumber > 0 &&
            repetitions >= 0 &&
            weight >= 0 &&
            duration >= 0 &&
            distance >= 0 &&
            restTime >= 0 &&
            (rpe == null || rpe in 1..10)
    }

    /**
     * Checks if this is a strength-based set (has weight and reps).
     *
     * @return true if this is a strength set, false otherwise
     */
    fun isStrengthSet(): Boolean = weight > 0 && repetitions > 0

    /**
     * Checks if this is a cardio-based set (has duration or distance).
     *
     * @return true if this is a cardio set, false otherwise
     */
    fun isCardioSet(): Boolean = duration > 0 || distance > 0

    companion object {
        const val MIN_RPE = 1
        const val MAX_RPE = 10
        const val DEFAULT_REST_TIME = 60 // Default 1 minute rest
    }
}

/**
 * Enumeration of different types of sets.
 */
enum class SetType {
    NORMAL, // Regular working set
    WARM_UP, // Warm-up set with lighter weight
    DROP_SET, // Drop set (reducing weight mid-set)
    SUPER_SET, // Part of a superset
    REST_PAUSE, // Rest-pause set
    CLUSTER, // Cluster set with mini-rests
    AMRAP, // As Many Reps As Possible
    FAILURE, // Set taken to muscular failure
    BACK_OFF, // Lighter set after heavy work
    BURNOUT, // High-rep burnout set
}

/**
 * Data class for workout set summary and statistics.
 */
data class WorkoutSetSummary(
    val exerciseName: String,
    val totalSets: Int,
    val totalReps: Int,
    val totalVolume: Float,
    val averageWeight: Float,
    val maxWeight: Float,
    val totalDuration: Int,
    val personalRecords: Int,
)
