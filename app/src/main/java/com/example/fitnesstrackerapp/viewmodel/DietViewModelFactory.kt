package com.example.fitnesstrackerapp.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fitnesstrackerapp.data.FitnessDatabase

/**
 * DietViewModelFactory
 *
 * A ViewModelProvider.Factory responsible for constructing the DietViewModel
 * with its required DietDao dependency from the Room database.
 *
 * This is necessary because DietViewModel has a non-default constructor.
 */
class DietViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {

    /**
     * Creates a new instance of the given ViewModel class.
     *
     * @param modelClass The class of the ViewModel to create.
     * @return A properly constructed DietViewModel instance with DietDao injected.
     *
     * @throws IllegalArgumentException if the ViewModel is not DietViewModel.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DietViewModel::class.java) -> {
                val dietDao = FitnessDatabase
                    .getDatabase(application)
                    .dietDao()

                @Suppress("UNCHECKED_CAST")
                DietViewModel(dietDao) as T
            }

            else -> throw IllegalArgumentException(
                "Unknown ViewModel class: ${modelClass.name}"
            )
        }
    }
}
