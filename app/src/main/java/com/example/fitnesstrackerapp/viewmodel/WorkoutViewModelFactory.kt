package com.example.fitnesstrackerapp.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fitnesstrackerapp.data.FitnessDatabase
import com.example.fitnesstrackerapp.data.WorkoutDao

/**
 * Factory class for instantiating the WorkoutViewModel with a WorkoutDao dependency.
 * This is needed because ViewModels by default can only be created with no-arg constructors.
 *
 * This factory retrieves the DAO from the singleton Room database.
 */
class WorkoutViewModelFactory(application: Application) : ViewModelProvider.Factory {

    // Access the DAO from the Room database singleton instance
    private val dao: WorkoutDao = FitnessDatabase.getDatabase(application).workoutDao()

    /**
     * Creates and returns an instance of WorkoutViewModel if requested,
     * otherwise throws an exception for unknown ViewModel classes.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WorkoutViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
