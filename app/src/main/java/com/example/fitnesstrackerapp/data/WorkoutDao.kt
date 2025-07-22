package com.example.fitnesstrackerapp.data

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * Data Access Object (DAO) for managing Workout entries in the Room database.
 * Provides methods to insert, delete, and query workout data.
 */
@Dao
interface WorkoutDao {

    /**
     * Inserts a new workout into the database.
     * Replaces the entry if there's a conflict (e.g., duplicate ID).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: Workout)

    /**
     * Deletes a specific workout entry.
     */
    @Delete
    suspend fun deleteWorkout(workout: Workout)

    /**
     * Returns all workouts ordered by date (newest first).
     * Observed as LiveData for UI updates.
     */
    @Query("SELECT * FROM workouts ORDER BY date DESC")
    fun getAllWorkouts(): LiveData<List<Workout>>

    /**
     * Retrieves all workouts between the specified start and end timestamps.
     */
    @Query("SELECT * FROM workouts WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun getWorkoutsBetween(start: Long, end: Long): LiveData<List<Workout>>

    /**
     * Optional: Deletes all workouts from the database.
     */
    @Query("DELETE FROM workouts")
    suspend fun clearAll()
}
