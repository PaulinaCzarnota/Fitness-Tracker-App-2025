package com.example.fitnesstrackerapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstrackerapp.data.Workout
import com.example.fitnesstrackerapp.data.WorkoutDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel that manages workout-related operations and provides observable data to the UI.
 * This separates the UI from data-handling logic and ensures lifecycle awareness.
 */
class WorkoutViewModel(private val workoutDao: WorkoutDao) : ViewModel() {

    // Observable list of all workouts from the database
    val allWorkouts: LiveData<List<Workout>> = workoutDao.getAllWorkouts()

    /**
     * Insert a new workout entry into the database.
     * This runs in the IO thread to avoid blocking the UI.
     */
    fun insertWorkout(workout: Workout) {
        viewModelScope.launch(Dispatchers.IO) {
            workoutDao.insertWorkout(workout)
        }
    }

    /**
     * Delete a specific workout from the database.
     */
    fun deleteWorkout(workout: Workout) {
        viewModelScope.launch(Dispatchers.IO) {
            workoutDao.deleteWorkout(workout)
        }
    }

    /**
     * Optional: Clear all workouts (used for testing or reset).
     */
    fun clearAllWorkouts() {
        viewModelScope.launch(Dispatchers.IO) {
            workoutDao.clearAll()
        }
    }
}
