package com.example.fitnesstrackerapp.viewmodel

import androidx.lifecycle.*
import com.example.fitnesstrackerapp.data.Diet
import com.example.fitnesstrackerapp.data.DietDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel for managing diet data between the UI and Room database.
 * It provides lifecycle-aware operations and LiveData observation.
 */
class DietViewModel(private val dietDao: DietDao) : ViewModel() {

    // LiveData that observes all diet entries from the database
    val allDiets: LiveData<List<Diet>> = dietDao.getAllDiets()

    /**
     * Insert a new diet entry in the background.
     */
    fun addDiet(diet: Diet) = viewModelScope.launch(Dispatchers.IO) {
        dietDao.insertDiet(diet)
    }

    /**
     * Update an existing diet entry.
     */
    fun updateDiet(diet: Diet) = viewModelScope.launch(Dispatchers.IO) {
        dietDao.updateDiet(diet)
    }

    /**
     * Delete a specific diet entry.
     */
    fun deleteDiet(diet: Diet) = viewModelScope.launch(Dispatchers.IO) {
        dietDao.deleteDiet(diet)
    }

    /**
     * Clear all diet entries from the database.
     */
    fun clearAllDiets() = viewModelScope.launch(Dispatchers.IO) {
        dietDao.clearAll()
    }
}
