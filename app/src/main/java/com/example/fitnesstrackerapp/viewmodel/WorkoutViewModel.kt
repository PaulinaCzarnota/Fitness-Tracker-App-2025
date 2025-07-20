package com.example.fitnesstrackerapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstrackerapp.data.Workout
import com.example.fitnesstrackerapp.data.WorkoutDao
import kotlinx.coroutines.launch

class WorkoutViewModel(private val dao: WorkoutDao) : ViewModel() {

    // Expose the list of workouts as LiveData
    val allWorkouts: LiveData<List<Workout>> = dao.getAllWorkouts()

    // Method to insert a workout asynchronously
    fun insertWorkout(workout: Workout) {
        viewModelScope.launch {
            dao.insertWorkout(workout)
        }
    }

    fun addWorkout(workout: Workout) {

    }
}
