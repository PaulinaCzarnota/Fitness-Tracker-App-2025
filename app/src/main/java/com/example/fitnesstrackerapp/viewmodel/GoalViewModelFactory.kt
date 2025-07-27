package com.example.fitnesstrackerapp.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fitnesstrackerapp.data.FitnessDatabase

/**
 * GoalViewModelFactory
 *
 * A ViewModelProvider.Factory that supplies a GoalViewModel instance with
 * a required GoalDao dependency from the Room database.
 *
 * This is essential because GoalViewModel does not have a default constructor.
 */
class GoalViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    /**
     * Creates and returns an instance of GoalViewModel with injected DAO.
     *
     * @param modelClass The class of the ViewModel requested.
     * @return A GoalViewModel instance if requested, otherwise throws exception.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(GoalViewModel::class.java) -> {
                val goalDao = FitnessDatabase.getDatabase(application).goalDao()
                @Suppress("UNCHECKED_CAST")
                GoalViewModel(goalDao) as T
            }

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
