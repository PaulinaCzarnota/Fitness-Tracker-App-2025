package com.example.fitnesstrackerapp.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fitnesstrackerapp.data.FitnessDatabase
import com.example.fitnesstrackerapp.data.WorkoutDao

/**
 * WorkoutViewModelFactory
 *
 * Provides an instance of WorkoutViewModel with a custom constructor (WorkoutDao).
 * Required because Android's default ViewModelProvider cannot pass parameters.
 *
 * @param application The application context used to retrieve the singleton Room database.
 */
class WorkoutViewModelFactory(application: Application) : ViewModelProvider.Factory {

    // DAO retrieved from singleton database instance
    private val dao: WorkoutDao = FitnessDatabase.getDatabase(application).workoutDao()

    /**
     * Creates a new instance of the requested ViewModel.
     *
     * @param modelClass The class of the ViewModel to create.
     * @return An instance of WorkoutViewModel with the DAO injected.
     * @throws IllegalArgumentException If an unsupported ViewModel class is requested.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WorkoutViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
