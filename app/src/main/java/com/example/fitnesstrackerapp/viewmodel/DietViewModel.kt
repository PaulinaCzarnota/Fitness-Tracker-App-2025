package com.example.fitnesstrackerapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstrackerapp.data.Diet
import com.example.fitnesstrackerapp.data.DietDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * DietViewModel
 *
 * A lifecycle-aware ViewModel responsible for handling all data operations
 * related to diet entries. It exposes LiveData for real-time UI updates
 * and runs Room database operations using coroutines.
 *
 * @param dietDao DAO interface for interacting with the 'diet' table in Room.
 */
class DietViewModel(private val dietDao: DietDao) : ViewModel() {

    /**
     * LiveData list of all diet records in the database.
     * Observed by the Compose UI to update whenever data changes.
     */
    val allDiets: LiveData<List<Diet>> = dietDao.getAllDiets()

    /**
     * Inserts a new diet entry into the database.
     * Typically triggered by the user adding a food item.
     *
     * @param diet The new Diet object to insert.
     */
    fun addDiet(diet: Diet) {
        viewModelScope.launch(Dispatchers.IO) {
            dietDao.insertDiet(diet)
        }
    }

    /**
     * Updates an existing diet record in the database.
     * Useful when editing calorie counts or food name.
     *
     * @param diet The modified Diet object.
     */
    fun updateDiet(diet: Diet) {
        viewModelScope.launch(Dispatchers.IO) {
            dietDao.updateDiet(diet)
        }
    }

    /**
     * Deletes a specific diet entry from the database.
     * Called when the user removes a previously added food item.
     *
     * @param diet The Diet object to delete.
     */
    fun deleteDiet(diet: Diet) {
        viewModelScope.launch(Dispatchers.IO) {
            dietDao.deleteDiet(diet)
        }
    }

    /**
     * Clears all diet records from the database.
     * Typically used in reset functionality or for testing.
     */
    fun clearAllDiets() {
        viewModelScope.launch(Dispatchers.IO) {
            dietDao.clearAll()
        }
    }
}
