package com.example.fitnesstrackerapp.repository

import com.example.fitnesstrackerapp.data.Workout
import com.example.fitnesstrackerapp.data.WorkoutDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository class for managing Workout data operations.
 * Provides a clean API to access workout entries from the data layer.
 */
@Singleton
class WorkoutRepository @Inject constructor(
    private val workoutDao: WorkoutDao
) {

    /**
     * Retrieves all workout entries as a Flow stream.
     *
     * @return a Flow emitting lists of [Workout].
     */
    fun getAllWorkouts(): Flow<List<Workout>> = workoutDao.getAllWorkouts()

    /**
     * Retrieves workouts between two dates as a Flow stream.
     *
     * @param start Start date/time in epoch milliseconds.
     * @param end End date/time in epoch milliseconds.
     * @return a Flow emitting lists of [Workout] for the given date range.
     */
    fun getWorkoutsBetween(start: Long, end: Long): Flow<List<Workout>> = 
        workoutDao.getWorkoutsBetween(start, end)

    /**
     * Inserts a new workout entry into the database.
     *
     * @param workout the Workout entity to insert.
     */
    suspend fun insertWorkout(workout: Workout) = workoutDao.insertWorkout(workout)

    /**
     * Deletes a workout entry from the database.
     *
     * @param workout the Workout entity to delete.
     */
    suspend fun deleteWorkout(workout: Workout) = workoutDao.deleteWorkout(workout)

    /**
     * Clears all workout entries from the database.
     */
    suspend fun clearAllWorkouts() = workoutDao.clearAll()
}
