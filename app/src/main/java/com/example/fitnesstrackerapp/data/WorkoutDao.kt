package com.example.fitnesstrackerapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * WorkoutDao
 *
 * Data Access Object (DAO) for managing workout entries in the "workouts" table.
 * Provides methods to insert, delete, retrieve, and filter workout records.
 * Supports reactive updates using Kotlin Flow.
 */
@Dao
interface WorkoutDao {

    /**
     * Inserts a new [Workout] into the database.
     * If a record with the same ID already exists, it will be replaced.
     *
     * @param workout The workout to insert or update.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: Workout)

    /**
     * Deletes a specific [Workout] from the database.
     *
     * @param workout The workout to remove.
     */
    @Delete
    suspend fun deleteWorkout(workout: Workout)

    /**
     * Retrieves all workouts ordered by date (most recent first).
     *
     * @return A [Flow] emitting the list of workouts, auto-updated on DB change.
     */
    @Query("SELECT * FROM workouts ORDER BY date DESC")
    fun getAllWorkouts(): Flow<List<Workout>>

    /**
     * Retrieves all workouts that occurred between two dates (inclusive).
     * Dates must be in Unix time milliseconds.
     *
     * @param start Start date/time in epoch milliseconds.
     * @param end End date/time in epoch milliseconds.
     * @return A [Flow] emitting the filtered list of workouts.
     */
    @Query("SELECT * FROM workouts WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun getWorkoutsBetween(start: Long, end: Long): Flow<List<Workout>>

    /**
     * Deletes all workouts from the database.
     * This operation clears the "workouts" table entirely.
     */
    @Query("DELETE FROM workouts")
    suspend fun clearAll()
}
