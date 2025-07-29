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
 * ViewModel that mediates between the Diet UI and DietDao (data layer).
 * Handles adding, editing, deleting, and observing diet entries using coroutines.
 *
 * All operations are executed on a background thread (Dispatchers.IO).
 * The UI observes LiveData returned by this ViewModel.
 *
 * @param dietDao DAO interface for interacting with the diet table in Room DB.
 */
class DietViewModel(private val dietDao: DietDao) : ViewModel() {

    /**
     * All diet entries, ordered by most recent.
     * Automatically updates the UI through LiveData observation.
     */
    val allDiets: LiveData<List<Diet>> = dietDao.getAllDiets()

    /**
     * Adds a new Diet entry to the Room database.
     *
     * @param diet The Diet object to insert.
     */
    fun addDiet(diet: Diet) {
        viewModelScope.launch(Dispatchers.IO) {
            dietDao.insertDiet(diet)
        }
    }

    /**
     * Updates an existing Diet entry in the Room database.
     *
     * @param diet The modified Diet object to update.
     */
    fun editDiet(diet: Diet) {
        viewModelScope.launch(Dispatchers.IO) {
            dietDao.updateDiet(diet)
        }
    }

    /**
     * Deletes a Diet entry from the Room database.
     *
     * @param diet The Diet object to remove.
     */
    fun removeDiet(diet: Diet) {
        viewModelScope.launch(Dispatchers.IO) {
            dietDao.deleteDiet(diet)
        }
    }

    /**
     * Deletes all Diet entries from the database.
     * Typically used to clear logs — irreversible.
     */
    fun resetAllDiets() {
        viewModelScope.launch(Dispatchers.IO) {
            dietDao.clearAll()
        }
    }
}
