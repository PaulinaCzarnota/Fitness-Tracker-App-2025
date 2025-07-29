package com.example.fitnesstrackerapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstrackerapp.data.Workout
import com.example.fitnesstrackerapp.data.WorkoutDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * WorkoutViewModel
 *
 * ViewModel responsible for managing workout data.
 * Acts as a bridge between the UI layer and [WorkoutDao].
 *
 * Exposes flows for reactive updates and uses coroutines for database operations.
 *
 * @param workoutDao DAO interface for workout-related Room database operations.
 */
class WorkoutViewModel(
    private val workoutDao: WorkoutDao
) : ViewModel() {

    /**
     * Emits a live list of all workouts from the database.
     * Use collectAsStateWithLifecycle() in Composables for observing changes.
     */
    val allWorkouts: Flow<List<Workout>> = workoutDao.getAllWorkouts()

    /**
     * Inserts a new workout into the database.
     * Can be triggered when the user logs a new session.
     *
     * @param workout The workout entry to save.
     */
    fun insertWorkout(workout: Workout) {
        viewModelScope.launch(Dispatchers.IO) {
            workoutDao.insertWorkout(workout)
        }
    }

    /**
     * Deletes a specific workout from the database.
     * Called when user removes an entry (e.g., via swipe/delete button).
     *
     * @param workout The workout to be removed.
     */
    fun deleteWorkout(workout: Workout) {
        viewModelScope.launch(Dispatchers.IO) {
            workoutDao.deleteWorkout(workout)
        }
    }

    /**
     * Clears all workouts from the database.
     * Use this when the user chooses to reset workout logs.
     */
    fun clearAllWorkouts() {
        viewModelScope.launch(Dispatchers.IO) {
            workoutDao.clearAll()
        }
    }

    /**
     * Queries workouts between two dates (start and end timestamps).
     * Ideal for displaying filtered reports or weekly activity.
     *
     * @param start Start timestamp (in millis).
     * @param end End timestamp (in millis).
     * @return A Flow of filtered workouts between the dates.
     */
    fun getWorkoutsBetween(start: Long, end: Long): Flow<List<Workout>> {
        return workoutDao.getWorkoutsBetween(start, end)
    }
}
