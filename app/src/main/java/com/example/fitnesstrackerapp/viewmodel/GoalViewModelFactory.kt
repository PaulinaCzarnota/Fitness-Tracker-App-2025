package com.example.fitnesstrackerapp.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fitnesstrackerapp.data.GoalDao
import com.example.fitnesstrackerapp.di.DatabaseModule

/**
 * GoalViewModelFactory
 *
 * Factory class for creating [GoalViewModel] instances with their required dependencies.
 * This factory ensures that the ViewModel receives the correct [GoalDao] from the database.
 *
 * Usage in Jetpack Compose:
 * ```kotlin
 * val viewModel: GoalViewModel = viewModel(
 *     factory = GoalViewModelFactory(application)
 * )
 * ```
 */
class GoalViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GoalViewModel::class.java)) {
            // Create database and DAO instances
            val database = DatabaseModule.provideDatabase(application)
            val goalDao = database.goalDao()
            
            return GoalViewModel(goalDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
