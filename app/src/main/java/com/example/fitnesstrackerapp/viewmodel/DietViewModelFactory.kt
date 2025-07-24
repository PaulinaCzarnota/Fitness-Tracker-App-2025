package com.example.fitnesstrackerapp.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fitnesstrackerapp.data.FitnessDatabase

/**
 * DietViewModelFactory.kt
 *
 * Custom ViewModelProvider.Factory for instantiating DietViewModel with its required DietDao.
 * This enables proper dependency injection in the MVVM architecture.
 */
class DietViewModelFactory(application: Application) : ViewModelProvider.Factory {

    // Retrieve the DietDao from the singleton instance of the Room database
    private val dao = FitnessDatabase.getDatabase(application).dietDao()

    /**
     * Creates an instance of the requested ViewModel (DietViewModel).
     *
     * @param modelClass The class of the ViewModel to create.
     * @return The DietViewModel instance with DietDao injected.
     * @throws IllegalArgumentException If the ViewModel class is not supported.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DietViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DietViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
