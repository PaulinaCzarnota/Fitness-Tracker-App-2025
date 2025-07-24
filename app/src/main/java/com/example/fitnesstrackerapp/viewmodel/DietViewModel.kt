package com.example.fitnesstrackerapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstrackerapp.data.Diet
import com.example.fitnesstrackerapp.data.DietDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * DietViewModel.kt
 *
 * ViewModel that manages diet-related operations and provides data to the UI.
 * Acts as a bridge between the database and composable UI.
 *
 * Uses LiveData for observation and Kotlin coroutines for background execution.
 *
 * @param dietDao The DAO interface to access the 'diets' table.
 */
class DietViewModel(private val dietDao: DietDao) : ViewModel() {

    /**
     * Observable list of all diet entries.
     * Automatically updates the UI when the data changes.
     */
    val allDiets: LiveData<List<Diet>> = dietDao.getAllDiets()

    /**
     * Inserts a new diet record into the database.
     * Runs on a background thread to prevent UI blocking.
     *
     * @param diet The Diet object to be inserted.
     */
    fun addDiet(diet: Diet) = viewModelScope.launch(Dispatchers.IO) {
        dietDao.insertDiet(diet)
    }

    /**
     * Updates an existing diet entry.
     * Typically used to correct food info or calorie count.
     *
     * @param diet The updated Diet object.
     */
    fun updateDiet(diet: Diet) = viewModelScope.launch(Dispatchers.IO) {
        dietDao.updateDiet(diet)
    }

    /**
     * Deletes a specific diet entry from the database.
     *
     * @param diet The Diet object to be deleted.
     */
    fun deleteDiet(diet: Diet) = viewModelScope.launch(Dispatchers.IO) {
        dietDao.deleteDiet(diet)
    }

    /**
     * Deletes all diet records from the database.
     * Useful for data reset or testing.
     */
    fun clearAllDiets() = viewModelScope.launch(Dispatchers.IO) {
        dietDao.clearAll()
    }
}
