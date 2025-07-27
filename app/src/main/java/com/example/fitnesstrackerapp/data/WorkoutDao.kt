package com.example.fitnesstrackerapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * WorkoutDao
 *
 * Data Access Object (DAO) for managing operations related to the "workouts" table.
 * Provides suspend functions for inserting and deleting, and reactive queries using Flow.
 */
@Dao
interface WorkoutDao {

    /**
     * Inserts a new workout into the database.
     * If a workout with the same ID already exists, it will be replaced.
     *
     * @param workout The Workout entity to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: Workout)

    /**
     * Deletes a specific workout from the database.
     *
     * @param workout The Workout entity to be removed.
     */
    @Delete
    suspend fun deleteWorkout(workout: Workout)

    /**
     * Retrieves all workout entries from the database, ordered by date (newest first).
     *
     * @return A Flow that emits the current list of workouts when data changes.
     */
    @Query("SELECT * FROM workouts ORDER BY date DESC")
    fun getAllWorkouts(): Flow<List<Workout>>

    /**
     * Retrieves all workouts that occurred between two dates (inclusive).
     * Useful for progress tracking and reporting.
     *
     * @param start Start date in milliseconds (epoch time).
     * @param end End date in milliseconds (epoch time).
     * @return A Flow that emits the filtered list of workouts.
     */
    @Query("SELECT * FROM workouts WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun getWorkoutsBetween(start: Long, end: Long): Flow<List<Workout>>

    /**
     * Clears all workout records from the database.
     * Useful for resetting app state or debugging.
     */
    @Query("DELETE FROM workouts")
    suspend fun clearAll()
}
