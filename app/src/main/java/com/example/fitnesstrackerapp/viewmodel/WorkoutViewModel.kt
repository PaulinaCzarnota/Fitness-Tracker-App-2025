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
 * ViewModel exposing workout data as Flow and supporting async operations.
 */
class WorkoutViewModel(
    private val workoutDao: WorkoutDao
) : ViewModel() {

    /**
     * All workouts from the database, exposed as Flow for Compose UI.
     */
    val allWorkouts: Flow<List<Workout>> = workoutDao.getAllWorkouts()

    /**
     * Insert a new workout entry in the background.
     */
    fun insertWorkout(workout: Workout) {
        viewModelScope.launch(Dispatchers.IO) {
            workoutDao.insertWorkout(workout)
        }
    }

    /**
     * Delete a specific workout entry in the background.
     */
    fun deleteWorkout(workout: Workout) {
        viewModelScope.launch(Dispatchers.IO) {
            workoutDao.deleteWorkout(workout)
        }
    }

    /**
     * Clear all workouts (used for resets or testing).
     */
    fun clearAllWorkouts() {
        viewModelScope.launch(Dispatchers.IO) {
            workoutDao.clearAll()
        }
    }

    /**
     * Get workouts between two timestamps for filtering.
     *
     * @param start Start timestamp (inclusive)
     * @param end End timestamp (inclusive)
     * @return Flow of filtered workouts
     */
    fun getWorkoutsBetween(start: Long, end: Long): Flow<List<Workout>> {
        return workoutDao.getWorkoutsBetween(start, end)
    }
}
