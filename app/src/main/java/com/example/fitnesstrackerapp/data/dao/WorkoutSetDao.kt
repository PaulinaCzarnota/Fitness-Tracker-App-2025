/**
 * WorkoutSet Data Access Object for the Fitness Tracker application.
 *
 * This DAO provides comprehensive database operations for WorkoutSet entities including
 * set management, performance tracking, personal records, and workout analytics.
 * All operations are coroutine-based for optimal performance and UI responsiveness.
 *
 * Key Features:
 * - Workout set CRUD operations
 * - Performance analytics and volume calculations
 * - Personal record tracking and detection
 * - Exercise progression and history analysis
 * - Rest time and RPE tracking
 */

package com.example.fitnesstrackerapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.fitnesstrackerapp.data.entity.SetType
import com.example.fitnesstrackerapp.data.entity.WorkoutSet
import com.example.fitnesstrackerapp.data.entity.WorkoutSetSummary
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Data Access Object for WorkoutSet entity operations.
 *
 * Provides comprehensive database operations for workout set management including
 * performance tracking, analytics, and progression monitoring.
 * All operations are suspend functions for coroutine compatibility.
 */
@Dao
interface WorkoutSetDao {
    /**
     * Inserts a new workout set into the database.
     *
     * @param workoutSet WorkoutSet entity to insert
     * @return The ID of the inserted workout set
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutSet(workoutSet: WorkoutSet): Long

    /**
     * Inserts multiple workout sets in a single transaction.
     *
     * @param workoutSets List of workout sets to insert
     * @return List of inserted workout set IDs
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutSets(workoutSets: List<WorkoutSet>): List<Long>

    /**
     * Updates an existing workout set in the database.
     *
     * @param workoutSet WorkoutSet entity with updated data
     */
    @Update
    suspend fun updateWorkoutSet(workoutSet: WorkoutSet)

    /**
     * Deletes a workout set from the database.
     *
     * @param workoutSet WorkoutSet entity to delete
     */
    @Delete
    suspend fun deleteWorkoutSet(workoutSet: WorkoutSet)

    /**
     * Deletes a workout set by its ID.
     *
     * @param workoutSetId Workout set ID to delete
     */
    @Query("DELETE FROM workout_sets WHERE id = :workoutSetId")
    suspend fun deleteWorkoutSetById(workoutSetId: Long)

    /**
     * Gets a workout set by its ID.
     *
     * @param workoutSetId Workout set ID to search for
     * @return WorkoutSet entity or null if not found
     */
    @Query("SELECT * FROM workout_sets WHERE id = :workoutSetId LIMIT 1")
    suspend fun getWorkoutSetById(workoutSetId: Long): WorkoutSet?

    /**
     * Gets all sets for a specific workout.
     *
     * @param workoutId Workout ID
     * @return Flow of workout sets ordered by set number
     */
    @Query("SELECT * FROM workout_sets WHERE workoutId = :workoutId ORDER BY setNumber ASC")
    fun getSetsByWorkout(workoutId: Long): Flow<List<WorkoutSet>>

    /**
     * Gets all sets for multiple workouts.
     *
     * @param workoutIds List of workout IDs
     * @return List of workout sets for the specified workouts
     */
    @Query("SELECT * FROM workout_sets WHERE workoutId IN (:workoutIds) ORDER BY workoutId ASC, setNumber ASC")
    suspend fun getSetsByWorkoutIds(workoutIds: List<Long>): List<WorkoutSet>

    /**
     * Gets all sets for a specific exercise within a workout.
     *
     * @param workoutId Workout ID
     * @param exerciseId Exercise ID
     * @return Flow of workout sets for the specific exercise
     */
    @Query("SELECT * FROM workout_sets WHERE workoutId = :workoutId AND exerciseId = :exerciseId ORDER BY setNumber ASC")
    fun getSetsByWorkoutAndExercise(workoutId: Long, exerciseId: Long): Flow<List<WorkoutSet>>

    /**
     * Gets all sets for a specific exercise (across all workouts).
     *
     * @param exerciseId Exercise ID
     * @return Flow of workout sets ordered by creation date
     */
    @Query("SELECT * FROM workout_sets WHERE exerciseId = :exerciseId ORDER BY createdAt DESC")
    fun getSetsByExercise(exerciseId: Long): Flow<List<WorkoutSet>>

    /**
     * Gets the latest sets for an exercise to show previous performance.
     *
     * @param exerciseId Exercise ID
     * @param limit Maximum number of recent sets to return
     * @return Flow of recent sets for the exercise
     */
    @Query("SELECT * FROM workout_sets WHERE exerciseId = :exerciseId ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentSetsByExercise(exerciseId: Long, limit: Int): Flow<List<WorkoutSet>>

    /**
     * Gets personal record sets (highest weight, most reps, etc.).
     *
     * @param exerciseId Exercise ID
     * @return Flow of personal record sets
     */
    @Query("SELECT * FROM workout_sets WHERE exerciseId = :exerciseId AND isPersonalRecord = 1 ORDER BY createdAt DESC")
    fun getPersonalRecordsByExercise(exerciseId: Long): Flow<List<WorkoutSet>>

    /**
     * Gets the heaviest weight lifted for a specific exercise.
     *
     * @param exerciseId Exercise ID
     * @return Maximum weight lifted for the exercise
     */
    @Query("SELECT MAX(weight) FROM workout_sets WHERE exerciseId = :exerciseId")
    suspend fun getMaxWeightForExercise(exerciseId: Long): Float?

    /**
     * Gets the most repetitions performed for a specific exercise.
     *
     * @param exerciseId Exercise ID
     * @return Maximum repetitions performed for the exercise
     */
    @Query("SELECT MAX(repetitions) FROM workout_sets WHERE exerciseId = :exerciseId")
    suspend fun getMaxRepsForExercise(exerciseId: Long): Int?

    /**
     * Gets the highest volume (weight × reps) for a specific exercise.
     *
     * @param exerciseId Exercise ID
     * @return Maximum volume for the exercise
     */
    @Query("SELECT MAX(weight * repetitions) FROM workout_sets WHERE exerciseId = :exerciseId")
    suspend fun getMaxVolumeForExercise(exerciseId: Long): Float?

    /**
     * Gets total volume for a workout.
     *
     * @param workoutId Workout ID
     * @return Total volume for the workout
     */
    @Query("SELECT SUM(weight * repetitions) FROM workout_sets WHERE workoutId = :workoutId")
    suspend fun getTotalVolumeForWorkout(workoutId: Long): Float?

    /**
     * Gets total volume for an exercise across all workouts.
     *
     * @param exerciseId Exercise ID
     * @return Total lifetime volume for the exercise
     */
    @Query("SELECT SUM(weight * repetitions) FROM workout_sets WHERE exerciseId = :exerciseId")
    suspend fun getTotalVolumeForExercise(exerciseId: Long): Float?

    /**
     * Gets total number of sets performed for an exercise.
     *
     * @param exerciseId Exercise ID
     * @return Total number of sets performed
     */
    @Query("SELECT COUNT(*) FROM workout_sets WHERE exerciseId = :exerciseId")
    suspend fun getTotalSetsForExercise(exerciseId: Long): Int

    /**
     * Gets total number of repetitions performed for an exercise.
     *
     * @param exerciseId Exercise ID
     * @return Total repetitions performed
     */
    @Query("SELECT SUM(repetitions) FROM workout_sets WHERE exerciseId = :exerciseId")
    suspend fun getTotalRepsForExercise(exerciseId: Long): Int?

    /**
     * Gets average weight used for an exercise.
     *
     * @param exerciseId Exercise ID
     * @return Average weight used for the exercise
     */
    @Query("SELECT AVG(weight) FROM workout_sets WHERE exerciseId = :exerciseId AND weight > 0")
    suspend fun getAverageWeightForExercise(exerciseId: Long): Float?

    /**
     * Gets average RPE for an exercise.
     *
     * @param exerciseId Exercise ID
     * @return Average Rate of Perceived Exertion for the exercise
     */
    @Query("SELECT AVG(rpe) FROM workout_sets WHERE exerciseId = :exerciseId AND rpe IS NOT NULL")
    suspend fun getAverageRPEForExercise(exerciseId: Long): Float?

    /**
     * Gets sets within a date range.
     *
     * @param startDate Start date of range
     * @param endDate End date of range
     * @return Flow of sets within the date range
     */
    @Query("SELECT * FROM workout_sets WHERE createdAt BETWEEN :startDate AND :endDate ORDER BY createdAt DESC")
    fun getSetsInDateRange(startDate: Date, endDate: Date): Flow<List<WorkoutSet>>

    /**
     * Gets sets by type (normal, warm-up, drop set, etc.).
     *
     * @param setType Type of set
     * @return Flow of sets matching the specified type
     */
    @Query("SELECT * FROM workout_sets WHERE setType = :setType ORDER BY createdAt DESC")
    fun getSetsByType(setType: SetType): Flow<List<WorkoutSet>>

    /**
     * Gets incomplete sets (for tracking ongoing workouts).
     *
     * @param workoutId Workout ID
     * @return Flow of incomplete sets
     */
    @Query("SELECT * FROM workout_sets WHERE workoutId = :workoutId AND isCompleted = 0 ORDER BY setNumber ASC")
    fun getIncompleteSets(workoutId: Long): Flow<List<WorkoutSet>>

    /**
     * Marks a set as completed.
     *
     * @param workoutSetId Workout set ID
     * @param completedAt Completion timestamp
     */
    @Query("UPDATE workout_sets SET isCompleted = 1, completedAt = :completedAt WHERE id = :workoutSetId")
    suspend fun markSetCompleted(workoutSetId: Long, completedAt: Date)

    /**
     * Updates personal record status for a set.
     *
     * @param workoutSetId Workout set ID
     * @param isPersonalRecord Whether this is a personal record
     */
    @Query("UPDATE workout_sets SET isPersonalRecord = :isPersonalRecord WHERE id = :workoutSetId")
    suspend fun updatePersonalRecordStatus(workoutSetId: Long, isPersonalRecord: Boolean)

    /**
     * Gets workout summary with exercise details.
     *
     * @param workoutId Workout ID
     * @return List of workout set summaries grouped by exercise
     */
    @Query(
        """
        SELECT
            e.name as exerciseName,
            COUNT(*) as totalSets,
            SUM(ws.repetitions) as totalReps,
            SUM(ws.weight * ws.repetitions) as totalVolume,
            AVG(ws.weight) as averageWeight,
            MAX(ws.weight) as maxWeight,
            SUM(ws.duration) as totalDuration,
            SUM(CASE WHEN ws.isPersonalRecord = 1 THEN 1 ELSE 0 END) as personalRecords
        FROM workout_sets ws
        INNER JOIN exercises e ON ws.exerciseId = e.id
        WHERE ws.workoutId = :workoutId
        GROUP BY ws.exerciseId, e.name
        ORDER BY MIN(ws.setNumber)
    """,
    )
    suspend fun getWorkoutSummary(workoutId: Long): List<WorkoutSetSummary>

    /**
     * Gets exercise progress over time (last N workouts).
     *
     * @param exerciseId Exercise ID
     * @param limit Number of recent workouts to include
     * @return Flow of sets showing progress over time
     */
    @Query(
        """
        SELECT ws.*
        FROM workout_sets ws
        INNER JOIN workouts w ON ws.workoutId = w.id
        WHERE ws.exerciseId = :exerciseId
        ORDER BY w.startTime DESC, ws.setNumber ASC
        LIMIT :limit
    """,
    )
    fun getExerciseProgress(exerciseId: Long, limit: Int): Flow<List<WorkoutSet>>

    /**
     * Deletes all sets for a workout.
     *
     * @param workoutId Workout ID
     */
    @Query("DELETE FROM workout_sets WHERE workoutId = :workoutId")
    suspend fun deleteSetsByWorkout(workoutId: Long)

    /**
     * Deletes all sets for an exercise.
     *
     * @param exerciseId Exercise ID
     */
    @Query("DELETE FROM workout_sets WHERE exerciseId = :exerciseId")
    suspend fun deleteSetsByExercise(exerciseId: Long)

    /**
     * Deletes all workout sets (for testing purposes only).
     */
    @Query("DELETE FROM workout_sets")
    suspend fun deleteAllWorkoutSets()

    /**
     * Gets the next set number for an exercise within a workout.
     *
     * @param workoutId Workout ID
     * @param exerciseId Exercise ID
     * @return Next set number to use
     */
    @Query("SELECT COALESCE(MAX(setNumber), 0) + 1 FROM workout_sets WHERE workoutId = :workoutId AND exerciseId = :exerciseId")
    suspend fun getNextSetNumber(workoutId: Long, exerciseId: Long): Int

    /**
     * Gets total workout time based on rest periods.
     *
     * @param workoutId Workout ID
     * @return Total rest time in seconds
     */
    @Query("SELECT SUM(restTime) FROM workout_sets WHERE workoutId = :workoutId")
    suspend fun getTotalRestTimeForWorkout(workoutId: Long): Int?

    /**
     * Updates RPE for a set.
     *
     * @param workoutSetId Workout set ID
     * @param rpe Rate of Perceived Exertion (1-10)
     */
    @Query("UPDATE workout_sets SET rpe = :rpe WHERE id = :workoutSetId")
    suspend fun updateSetRPE(workoutSetId: Long, rpe: Int)

    /**
     * Checks if a new set is a personal record.
     *
     * @param exerciseId Exercise ID
     * @param weight Weight used
     * @param repetitions Repetitions performed
     * @return True if this is a personal record for the exercise
     */
    @Query(
        """
        SELECT NOT EXISTS(
            SELECT 1 FROM workout_sets
            WHERE exerciseId = :exerciseId
            AND (weight > :weight OR (weight = :weight AND repetitions >= :repetitions))
        )
    """,
    )
    suspend fun isPersonalRecord(exerciseId: Long, weight: Double, repetitions: Int): Boolean
}
