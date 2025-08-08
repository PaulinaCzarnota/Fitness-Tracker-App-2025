/**
 * Repository for handling workout set-related data operations in the Fitness Tracker application.
 *
 * This repository provides a clean API for workout set data management, abstracting the
 * database layer and providing business logic for set operations. It handles
 * CRUD operations, performance analytics, personal record tracking, and progression analysis.
 *
 * Key Features:
 * - Complete CRUD operations for workout set entities
 * - Personal record detection and management
 * - Performance analytics and volume calculations
 * - Exercise progression tracking
 * - Rest time and RPE management
 */

package com.example.fitnesstrackerapp.repository

import com.example.fitnesstrackerapp.data.dao.WorkoutSetDao
import com.example.fitnesstrackerapp.data.entity.*
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Repository for handling workout set-related data operations.
 *
 * Provides a high-level interface for workout set data management, including
 * persistence operations, analytics, and performance tracking.
 * All operations are designed to be thread-safe and use coroutines for async execution.
 */
class WorkoutSetRepository(
    private val workoutSetDao: WorkoutSetDao,
) {

    /**
     * Inserts a new workout set into the database.
     * Automatically checks for personal records and updates the flag.
     *
     * @param workoutSet The workout set entity to be inserted
     * @return The auto-generated ID of the inserted workout set
     * @throws Exception if the insertion fails
     */
    suspend fun insertWorkoutSet(workoutSet: WorkoutSet): Long {
        // Check if this is a personal record
        val isPersonalRecord = if (workoutSet.isStrengthSet()) {
            workoutSetDao.isPersonalRecord(
                workoutSet.exerciseId,
                workoutSet.weight,
                workoutSet.repetitions
            )
        } else {
            false
        }

        val finalSet = workoutSet.copy(
            isPersonalRecord = isPersonalRecord,
            completedAt = if (workoutSet.isCompleted) Date() else null
        )

        return workoutSetDao.insertWorkoutSet(finalSet)
    }

    /**
     * Updates an existing workout set in the database.
     *
     * @param workoutSet The workout set entity with updated information
     * @throws Exception if the update fails or set doesn't exist
     */
    suspend fun updateWorkoutSet(workoutSet: WorkoutSet) {
        val updatedSet = workoutSet.copy(updatedAt = Date())
        workoutSetDao.updateWorkoutSet(updatedSet)
    }

    /**
     * Deletes a workout set from the database by its ID.
     *
     * @param workoutSetId The unique identifier of the workout set to delete
     * @throws Exception if the deletion fails
     */
    suspend fun deleteWorkoutSet(workoutSetId: Long) {
        workoutSetDao.deleteWorkoutSetById(workoutSetId)
    }

    /**
     * Gets a workout set by its ID.
     *
     * @param workoutSetId The unique identifier of the workout set
     * @return WorkoutSet entity or null if not found
     */
    suspend fun getWorkoutSetById(workoutSetId: Long): WorkoutSet? {
        return workoutSetDao.getWorkoutSetById(workoutSetId)
    }

    /**
     * Gets all sets for a specific workout as a reactive Flow.
     *
     * @param workoutId The unique identifier of the workout
     * @return Flow emitting list of workout sets ordered by set number
     */
    fun getSetsByWorkout(workoutId: Long): Flow<List<WorkoutSet>> {
        return workoutSetDao.getSetsByWorkout(workoutId)
    }

    /**
     * Gets all sets for a specific exercise within a workout.
     *
     * @param workoutId The workout ID
     * @param exerciseId The exercise ID
     * @return Flow emitting list of sets for the exercise
     */
    fun getSetsByWorkoutAndExercise(workoutId: Long, exerciseId: Long): Flow<List<WorkoutSet>> {
        return workoutSetDao.getSetsByWorkoutAndExercise(workoutId, exerciseId)
    }

    /**
     * Gets all sets for a specific exercise (across all workouts).
     *
     * @param exerciseId The exercise ID
     * @return Flow emitting list of sets ordered by date
     */
    fun getSetsByExercise(exerciseId: Long): Flow<List<WorkoutSet>> {
        return workoutSetDao.getSetsByExercise(exerciseId)
    }

    /**
     * Gets the most recent sets for an exercise to show previous performance.
     *
     * @param exerciseId The exercise ID
     * @param limit Maximum number of recent sets to return
     * @return Flow emitting recent sets for reference
     */
    fun getRecentSetsByExercise(exerciseId: Long, limit: Int = 5): Flow<List<WorkoutSet>> {
        return workoutSetDao.getRecentSetsByExercise(exerciseId, limit)
    }

    /**
     * Creates a new workout set with automatic set numbering.
     *
     * @param workoutId The workout ID
     * @param exerciseId The exercise ID
     * @param repetitions Number of repetitions performed
     * @param weight Weight used (optional for bodyweight exercises)
     * @param duration Duration in seconds (for time-based exercises)
     * @param distance Distance covered (for cardio exercises)
     * @param setType Type of set (default: NORMAL)
     * @param restTime Rest time after this set in seconds
     * @param rpe Rate of Perceived Exertion (1-10 scale)
     * @param notes Optional notes about the set
     * @return ID of the created workout set
     */
    suspend fun createWorkoutSet(
        workoutId: Long,
        exerciseId: Long,
        repetitions: Int = 0,
        weight: Float = 0f,
        duration: Int = 0,
        distance: Float = 0f,
        setType: SetType = SetType.NORMAL,
        restTime: Int = WorkoutSet.DEFAULT_REST_TIME,
        rpe: Int? = null,
        notes: String? = null,
    ): Long {
        val setNumber = workoutSetDao.getNextSetNumber(workoutId, exerciseId)

        val workoutSet = WorkoutSet(
            workoutId = workoutId,
            exerciseId = exerciseId,
            setNumber = setNumber,
            setType = setType,
            repetitions = repetitions,
            weight = weight,
            duration = duration,
            distance = distance,
            restTime = restTime,
            rpe = rpe,
            notes = notes
        )

        return insertWorkoutSet(workoutSet)
    }

    /**
     * Marks a set as completed.
     *
     * @param workoutSetId The workout set ID
     */
    suspend fun markSetCompleted(workoutSetId: Long) {
        workoutSetDao.markSetCompleted(workoutSetId, Date())
    }

    /**
     * Updates the RPE (Rate of Perceived Exertion) for a set.
     *
     * @param workoutSetId The workout set ID
     * @param rpe RPE value (1-10)
     */
    suspend fun updateSetRPE(workoutSetId: Long, rpe: Int) {
        if (rpe !in WorkoutSet.MIN_RPE..WorkoutSet.MAX_RPE) {
            throw IllegalArgumentException("RPE must be between ${WorkoutSet.MIN_RPE} and ${WorkoutSet.MAX_RPE}")
        }
        workoutSetDao.updateSetRPE(workoutSetId, rpe)
    }

    /**
     * Gets personal records for a specific exercise.
     *
     * @param exerciseId The exercise ID
     * @return Flow emitting list of personal record sets
     */
    fun getPersonalRecords(exerciseId: Long): Flow<List<WorkoutSet>> {
        return workoutSetDao.getPersonalRecordsByExercise(exerciseId)
    }

    /**
     * Gets performance statistics for an exercise.
     *
     * @param exerciseId The exercise ID
     * @return ExerciseStats containing various performance metrics
     */
    suspend fun getExerciseStats(exerciseId: Long): ExerciseStats {
        val totalSets = workoutSetDao.getTotalSetsForExercise(exerciseId)
        val totalReps = workoutSetDao.getTotalRepsForExercise(exerciseId) ?: 0
        val totalVolume = workoutSetDao.getTotalVolumeForExercise(exerciseId) ?: 0f
        val maxWeight = workoutSetDao.getMaxWeightForExercise(exerciseId) ?: 0f
        val maxReps = workoutSetDao.getMaxRepsForExercise(exerciseId) ?: 0
        val maxVolume = workoutSetDao.getMaxVolumeForExercise(exerciseId) ?: 0f
        val averageWeight = workoutSetDao.getAverageWeightForExercise(exerciseId) ?: 0f
        val averageRPE = workoutSetDao.getAverageRPEForExercise(exerciseId) ?: 0f

        return ExerciseStats(
            exerciseId = exerciseId,
            totalSets = totalSets,
            totalReps = totalReps,
            totalVolume = totalVolume,
            maxWeight = maxWeight,
            maxReps = maxReps,
            maxVolume = maxVolume,
            averageWeight = averageWeight,
            averageRPE = averageRPE
        )
    }

    /**
     * Gets workout summary with exercise details.
     *
     * @param workoutId The workout ID
     * @return List of workout set summaries grouped by exercise
     */
    suspend fun getWorkoutSummary(workoutId: Long): List<WorkoutSetSummary> {
        return workoutSetDao.getWorkoutSummary(workoutId)
    }

    /**
     * Gets total volume for a workout.
     *
     * @param workoutId The workout ID
     * @return Total volume (weight × reps) for the workout
     */
    suspend fun getWorkoutVolume(workoutId: Long): Float {
        return workoutSetDao.getTotalVolumeForWorkout(workoutId) ?: 0f
    }

    /**
     * Gets exercise progression over time.
     *
     * @param exerciseId The exercise ID
     * @param limit Number of recent workouts to include
     * @return Flow emitting sets showing progression
     */
    fun getExerciseProgress(exerciseId: Long, limit: Int = 20): Flow<List<WorkoutSet>> {
        return workoutSetDao.getExerciseProgress(exerciseId, limit)
    }

    /**
     * Gets sets within a date range.
     *
     * @param startDate Start date of range
     * @param endDate End date of range
     * @return Flow emitting sets within the date range
     */
    fun getSetsInDateRange(startDate: Date, endDate: Date): Flow<List<WorkoutSet>> {
        return workoutSetDao.getSetsInDateRange(startDate, endDate)
    }

    /**
     * Gets incomplete sets for an ongoing workout.
     *
     * @param workoutId The workout ID
     * @return Flow emitting incomplete sets
     */
    fun getIncompleteSets(workoutId: Long): Flow<List<WorkoutSet>> {
        return workoutSetDao.getIncompleteSets(workoutId)
    }

    /**
     * Duplicates a set (for creating similar sets quickly).
     *
     * @param originalSetId ID of the set to duplicate
     * @param newWeight Optional new weight (uses original if not provided)
     * @param newReps Optional new reps (uses original if not provided)
     * @return ID of the duplicated set
     */
    suspend fun duplicateSet(
        originalSetId: Long,
        newWeight: Float? = null,
        newReps: Int? = null,
    ): Long {
        val originalSet = workoutSetDao.getWorkoutSetById(originalSetId)
            ?: throw IllegalArgumentException("Original set not found")

        val newSetNumber = workoutSetDao.getNextSetNumber(
            originalSet.workoutId,
            originalSet.exerciseId
        )

        val duplicatedSet = originalSet.copy(
            id = 0, // Reset ID for new entity
            setNumber = newSetNumber,
            weight = newWeight ?: originalSet.weight,
            repetitions = newReps ?: originalSet.repetitions,
            isCompleted = false,
            completedAt = null,
            createdAt = Date(),
            updatedAt = Date()
        )

        return insertWorkoutSet(duplicatedSet)
    }

    /**
     * Bulk insert multiple workout sets.
     *
     * @param workoutSets List of workout sets to insert
     * @return List of inserted workout set IDs
     */
    suspend fun insertWorkoutSets(workoutSets: List<WorkoutSet>): List<Long> {
        return workoutSetDao.insertWorkoutSets(workoutSets)
    }

    /**
     * Deletes all sets for a workout.
     *
     * @param workoutId The workout ID
     */
    suspend fun deleteAllSetsForWorkout(workoutId: Long) {
        workoutSetDao.deleteSetsByWorkout(workoutId)
    }

    /**
     * Gets total rest time for a workout.
     *
     * @param workoutId The workout ID
     * @return Total rest time in seconds
     */
    suspend fun getTotalRestTime(workoutId: Long): Int {
        return workoutSetDao.getTotalRestTimeForWorkout(workoutId) ?: 0
    }
}

/**
 * Data class for exercise performance statistics.
 */
data class ExerciseStats(
    val exerciseId: Long,
    val totalSets: Int,
    val totalReps: Int,
    val totalVolume: Float,
    val maxWeight: Float,
    val maxReps: Int,
    val maxVolume: Float,
    val averageWeight: Float,
    val averageRPE: Float,
)
