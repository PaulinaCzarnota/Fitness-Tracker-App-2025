package com.example.fitnesstrackerapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstrackerapp.data.Workout
import com.example.fitnesstrackerapp.data.WorkoutDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel for managing workout data.
 * Connects the DAO (data layer) to the UI in a lifecycle-aware way.
 */
class WorkoutViewModel(private val workoutDao: WorkoutDao) : ViewModel() {

    /**
     * LiveData for observing all workouts in the database.
     * UI (Jetpack Compose) observes this for displaying workout history.
     */
    val allWorkouts: LiveData<List<Workout>> = workoutDao.getAllWorkouts()

    /**
     * Insert a workout record into the Room database.
     * Uses a coroutine on the IO dispatcher (background thread).
     */
    fun insertWorkout(workout: Workout) {
        viewModelScope.launch(Dispatchers.IO) {
            workoutDao.insertWorkout(workout)
        }
    }

    /**
     * Delete a workout entry from the database.
     * Triggered by user actions like swiping or tapping delete.
     */
    fun deleteWorkout(workout: Workout) {
        viewModelScope.launch(Dispatchers.IO) {
            workoutDao.deleteWorkout(workout)
        }
    }

    /**
     * Delete all workout records from the database.
     * Optional function if you need to clear data (e.g. for testing).
     */
    fun clearAllWorkouts() {
        viewModelScope.launch(Dispatchers.IO) {
            workoutDao.clearAll()
        }
    }

    /**
     * Fetch workouts within a date range (e.g., for progress reports).
     * Example use: View workouts from last 7 days.
     */
    fun getWorkoutsBetween(start: Long, end: Long): LiveData<List<Workout>> {
        return workoutDao.getWorkoutsBetween(start, end)
    }
}
