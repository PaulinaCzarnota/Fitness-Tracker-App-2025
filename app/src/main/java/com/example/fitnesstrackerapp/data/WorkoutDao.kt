package com.example.fitnesstrackerapp.data

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * Data Access Object for the Workout table.
 * Defines methods for database interactions using Room.
 */
@Dao
interface WorkoutDao {

    // Insert a workout. Replace if there’s a conflict (e.g., same ID)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: Workout)

    // Delete a specific workout
    @Delete
    suspend fun deleteWorkout(workout: Workout)

    // Get all workouts ordered by most recent date
    @Query("SELECT * FROM workouts ORDER BY date DESC")
    fun getAllWorkouts(): LiveData<List<Workout>>

    // Get workouts within a specific date range
    @Query("SELECT * FROM workouts WHERE date BETWEEN :start AND :end")
    fun getWorkoutsBetween(start: Long, end: Long): LiveData<List<Workout>>
}
