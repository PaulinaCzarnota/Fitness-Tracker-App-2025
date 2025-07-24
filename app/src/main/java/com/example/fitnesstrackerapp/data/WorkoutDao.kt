package com.example.fitnesstrackerapp.data

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * WorkoutDao
 *
 * Data Access Object for the Workout entity.
 * Defines all the database interactions for workouts using Room.
 */
@Dao
interface WorkoutDao {

    /**
     * Inserts a workout into the database.
     * Replaces the existing entry if a conflict occurs (e.g., same ID).
     *
     * @param workout The Workout object to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: Workout)

    /**
     * Deletes a workout from the database.
     *
     * @param workout The Workout object to delete.
     */
    @Delete
    suspend fun deleteWorkout(workout: Workout)

    /**
     * Returns all workouts ordered by date (newest first).
     *
     * @return LiveData list of all workouts.
     */
    @Query("SELECT * FROM workouts ORDER BY date DESC")
    fun getAllWorkouts(): LiveData<List<Workout>>

    /**
     * Returns workouts within a specific date range.
     *
     * @param start Start timestamp (milliseconds)
     * @param end End timestamp (milliseconds)
     * @return LiveData list of workouts in the specified range.
     */
    @Query("SELECT * FROM workouts WHERE date BETWEEN :start AND :end ORDER BY date DESC")
    fun getWorkoutsBetween(start: Long, end: Long): LiveData<List<Workout>>

    /**
     * Deletes all workout records from the database.
     * Useful for clearing test data or resetting the database.
     */
    @Query("DELETE FROM workouts")
    suspend fun clearAll()
}
