package com.example.fitnesstrackerapp.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fitnesstrackerapp.data.FitnessDatabase

/**
 * GoalViewModelFactory.kt
 *
 * ViewModelProvider.Factory used to instantiate the GoalViewModel
 * with the required GoalDao dependency.
 * This is necessary because GoalViewModel has a non-empty constructor.
 */
class GoalViewModelFactory(application: Application) : ViewModelProvider.Factory {

    // Reference to the GoalDao from the Room singleton database
    private val dao = FitnessDatabase.getDatabase(application).goalDao()

    /**
     * Creates an instance of the requested ViewModel (GoalViewModel).
     *
     * @param modelClass The class of the ViewModel requested.
     * @return The GoalViewModel instance with DAO injected.
     * @throws IllegalArgumentException If the ViewModel class is unknown.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GoalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GoalViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
