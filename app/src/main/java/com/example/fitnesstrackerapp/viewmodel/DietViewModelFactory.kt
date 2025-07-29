package com.example.fitnesstrackerapp.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fitnesstrackerapp.data.DietDao
import com.example.fitnesstrackerapp.di.DatabaseModule

/**
 * DietViewModelFactory
 *
 * Factory class for creating [DietViewModel] instances with their required dependencies.
 * This factory ensures that the ViewModel receives the correct [DietDao] from the database.
 *
 * Usage in Jetpack Compose:
 * ```kotlin
 * val viewModel: DietViewModel = viewModel(
 *     factory = DietViewModelFactory(application)
 * )
 * ```
 */
class DietViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DietViewModel::class.java)) {
            // Create database and DAO instances
            val database = DatabaseModule.provideDatabase(application)
            val dietDao = database.dietDao()
            
            return DietViewModel(dietDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
