package com.example.fitnesstrackerapp.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fitnesstrackerapp.data.FitnessDatabase
import com.example.fitnesstrackerapp.data.WorkoutDao

/**
 * Factory class for creating instances of WorkoutViewModel with a required DAO.
 * This is necessary because WorkoutViewModel requires a parameter (WorkoutDao),
 * which can't be passed directly using the default ViewModelProvider.
 */
class WorkoutViewModelFactory(application: Application) : ViewModelProvider.Factory {

    // Retrieve the DAO instance from the singleton FitnessDatabase
    private val dao: WorkoutDao = FitnessDatabase.getDatabase(application).workoutDao()

    // Create and return an instance of WorkoutViewModel
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WorkoutViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
