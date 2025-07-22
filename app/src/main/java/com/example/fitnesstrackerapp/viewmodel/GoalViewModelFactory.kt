package com.example.fitnesstrackerapp.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fitnesstrackerapp.data.FitnessDatabase
import com.example.fitnesstrackerapp.viewmodel.GoalViewModel

/**
 * ViewModelProvider.Factory to create an instance of GoalViewModel with its required GoalDao.
 * This factory enables injection of the DAO dependency into the ViewModel constructor.
 */
class GoalViewModelFactory(application: Application) : ViewModelProvider.Factory {

    // Access the GoalDao instance from the FitnessDatabase singleton
    private val dao = FitnessDatabase.getDatabase(application).goalDao()

    // Provide the GoalViewModel with the GoalDao dependency
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GoalViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GoalViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
