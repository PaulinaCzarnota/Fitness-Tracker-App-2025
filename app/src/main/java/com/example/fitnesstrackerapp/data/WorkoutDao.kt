package com.example.fitnesstrackerapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * WorkoutDao
 *
 * Data Access Object for the Workout entity.
 * Handles CRUD operations and provides reactive streams using Flow.
 */
@Dao
interface WorkoutDao {

    /**
     * Inserts or replaces a workout record.
     *
     * @param workout Workout object to add or update.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: Workout)

    /**
     * Deletes a workout from the database.
     *
     * @param workout The workout to remove.
     */
    @Delete
    suspend fun deleteWorkout(workout: Workout)

    /**
     * Retrieves all workouts ordered from newest to oldest.
     *
     * @return Flow list of workouts for reactive Compose UIs.
     */
    @Query("SELECT * FROM workouts ORDER BY date DESC")
    fun getAllWorkouts(): Flow<List<Workout>>

    /**
     * Retrieves workouts between two timestamps (inclusive).
     *
     * @param start Start date in milliseconds.
     * @param end End date in milliseconds.
     * @return Flow list of filtered workouts.
     */
    @Query("SELECT * FROM workouts WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun getWorkoutsBetween(start: Long, end: Long): Flow<List<Workout>>

    /**
     * Deletes all workout entries from the database.
     */
    @Query("DELETE FROM workouts")
    suspend fun clearAll()
}
