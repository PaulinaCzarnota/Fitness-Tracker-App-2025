package com.example.fitnesstrackerapp.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fitnesstrackerapp.data.FitnessDatabase
import com.example.fitnesstrackerapp.data.WorkoutDao

/**
 * WorkoutViewModelFactory
 *
 * A custom implementation of ViewModelProvider.Factory used to instantiate
 * WorkoutViewModel with its required WorkoutDao dependency.
 *
 * Since ViewModel constructors typically have no parameters, we need this
 * factory to inject dependencies manually (like DAOs from Room).
 *
 * @param application The application context used to access the Room database.
 */
class WorkoutViewModelFactory(application: Application) : ViewModelProvider.Factory {

    // Access the WorkoutDao from the singleton Room database
    private val dao: WorkoutDao = FitnessDatabase.getDatabase(application).workoutDao()

    /**
     * Creates a new instance of WorkoutViewModel with the injected WorkoutDao.
     *
     * @param modelClass The ViewModel class being requested.
     * @return A WorkoutViewModel instance if the requested class matches.
     * @throws IllegalArgumentException If an unknown ViewModel class is requested.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WorkoutViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
