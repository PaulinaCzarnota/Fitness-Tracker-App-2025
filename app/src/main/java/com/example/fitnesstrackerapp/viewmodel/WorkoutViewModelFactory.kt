package com.example.fitnesstrackerapp.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fitnesstrackerapp.data.WorkoutDao
import com.example.fitnesstrackerapp.di.DatabaseModule

/**
 * WorkoutViewModelFactory
 *
 * Factory class for creating [WorkoutViewModel] instances with their required dependencies.
 * This factory ensures that the ViewModel receives the correct [WorkoutDao] from the database.
 *
 * Usage in Jetpack Compose:
 * ```kotlin
 * val viewModel: WorkoutViewModel = viewModel(
 *     factory = WorkoutViewModelFactory(application)
 * )
 * ```
 */
class WorkoutViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutViewModel::class.java)) {
            // Create database and DAO instances
            val database = DatabaseModule.provideDatabase(application)
            val workoutDao = database.workoutDao()
            
            return WorkoutViewModel(workoutDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
