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
 * A lifecycle-aware ViewModel responsible for managing workout data
 * by communicating with the WorkoutDao (Room).
 *
 * Provides functions to:
 * - Observe all workouts
 * - Add, delete, and clear workouts
 * - Query workouts between specific dates
 */
class WorkoutViewModel(
    private val workoutDao: WorkoutDao
) : ViewModel() {

    /**
     * A Flow that emits the list of all workouts in the database.
     * Use with collectAsStateWithLifecycle() in Composables for reactivity.
     */
    val allWorkouts: Flow<List<Workout>> = workoutDao.getAllWorkouts()

    /**
     * Inserts a new workout into the Room database.
     *
     * @param workout The Workout object to be inserted.
     */
    fun insertWorkout(workout: Workout) {
        viewModelScope.launch(Dispatchers.IO) {
            workoutDao.insertWorkout(workout)
        }
    }

    /**
     * Deletes a single workout entry from the database.
     * Trigger this from UI (e.g., button click) to remove a log.
     *
     * @param workout The Workout to delete.
     */
    fun deleteWorkout(workout: Workout) {
        viewModelScope.launch(Dispatchers.IO) {
            workoutDao.deleteWorkout(workout)
        }
    }

    /**
     * Clears all workout records.
     * Useful for reset or clear all functionality in UI.
     */
    fun clearAllWorkouts() {
        viewModelScope.launch(Dispatchers.IO) {
            workoutDao.clearAll()
        }
    }

    /**
     * Fetches workouts between two timestamps for reporting or filtering.
     *
     * @param start Start timestamp in milliseconds.
     * @param end End timestamp in milliseconds.
     * @return Flow emitting filtered workouts.
     */
    fun getWorkoutsBetween(start: Long, end: Long): Flow<List<Workout>> {
        return workoutDao.getWorkoutsBetween(start, end)
    }
}
