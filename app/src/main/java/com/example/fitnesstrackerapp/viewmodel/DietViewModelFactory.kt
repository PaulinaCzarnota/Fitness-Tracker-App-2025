package com.example.fitnesstrackerapp.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fitnesstrackerapp.data.FitnessDatabase

/**
 * ViewModelProvider.Factory implementation to provide DietViewModel
 * with a reference to the DietDao from the Room database.
 */
class DietViewModelFactory(application: Application) : ViewModelProvider.Factory {

    // Get an instance of DietDao from the singleton database
    private val dao = FitnessDatabase.getDatabase(application).dietDao()

    // Create and return the DietViewModel, passing the DAO
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DietViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DietViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
