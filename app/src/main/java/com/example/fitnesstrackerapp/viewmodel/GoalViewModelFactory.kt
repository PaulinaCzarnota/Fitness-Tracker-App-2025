package com.example.fitnesstrackerapp.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fitnesstrackerapp.data.FitnessDatabase
import com.example.fitnesstrackerapp.viewmodel.GoalViewModel

/**
 * GoalViewModelFactory
 *
 * A custom ViewModelProvider.Factory that allows the GoalViewModel
 * to be constructed with its required dependency (GoalDao).
 *
 * This is needed because the default ViewModel constructor can't handle
 * parameters like Room DAO objects unless we use a Factory.
 */
class GoalViewModelFactory(application: Application) : ViewModelProvider.Factory {

    // Access the GoalDao from the singleton FitnessDatabase instance
    private val dao = FitnessDatabase.getDatabase(application).goalDao()

    /**
     * Creates a new instance of the GoalViewModel with the DAO injected.
     *
     * @param modelClass The ViewModel class being requested.
     * @return An instance of GoalViewModel if the type matches.
     * @throws IllegalArgumentException if the requested ViewModel is unknown.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GoalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GoalViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
