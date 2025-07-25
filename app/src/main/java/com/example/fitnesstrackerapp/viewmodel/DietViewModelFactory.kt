package com.example.fitnesstrackerapp.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fitnesstrackerapp.data.FitnessDatabase
import com.example.fitnesstrackerapp.viewmodel.DietViewModel

/**
 * DietViewModelFactory
 *
 * A custom ViewModelProvider.Factory implementation that enables
 * DietViewModel to receive a DietDao dependency from the Room database.
 *
 * This is required because DietViewModel has a non-empty constructor.
 */
class DietViewModelFactory(application: Application) : ViewModelProvider.Factory {

    // Access DietDao from the singleton Room database instance
    private val dao = FitnessDatabase.getDatabase(application).dietDao()

    /**
     * Creates a new instance of the requested ViewModel.
     *
     * @param modelClass The ViewModel class being requested.
     * @return The DietViewModel instance if the type matches.
     * @throws IllegalArgumentException if the ViewModel class is unknown.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DietViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DietViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
