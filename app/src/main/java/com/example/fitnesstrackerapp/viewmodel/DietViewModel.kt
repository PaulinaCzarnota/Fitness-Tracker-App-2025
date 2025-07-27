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
 * ViewModel responsible for interacting with the DietDao.
 * Exposes diet data to the UI and performs add/edit/delete operations using coroutines.
 * All database operations are dispatched to the IO thread to prevent UI blocking.
 */
class DietViewModel(private val dietDao: DietDao) : ViewModel() {

    /**
     * Exposes all diet entries to the UI, automatically updated via LiveData.
     * Sorted by date descending in the DAO.
     */
    val allDiets: LiveData<List<Diet>> = dietDao.getAllDiets()

    /**
     * Adds a new diet entry.
     * This method is called from the UI when the user submits a new food log.
     *
     * @param diet The Diet object to insert.
     */
    fun addDiet(diet: Diet) {
        viewModelScope.launch(Dispatchers.IO) {
            dietDao.insertDiet(diet)
        }
    }

    /**
     * Updates an existing diet entry.
     * This is an internal method only used by [editDiet] to simplify public API.
     *
     * @param diet The Diet object with updated data.
     */
    private fun updateDiet(diet: Diet) {
        viewModelScope.launch(Dispatchers.IO) {
            dietDao.updateDiet(diet)
        }
    }

    /**
     * Deletes a specific diet entry.
     * Used internally by [removeDiet].
     *
     * @param diet The Diet object to remove.
     */
    private fun deleteDiet(diet: Diet) {
        viewModelScope.launch(Dispatchers.IO) {
            dietDao.deleteDiet(diet)
        }
    }

    /**
     * Clears all diet entries from the database.
     * Used internally by [resetAllDiets].
     */
    private fun clearAllDiets() {
        viewModelScope.launch(Dispatchers.IO) {
            dietDao.clearAll()
        }
    }

    /**
     * Public function exposed to UI to update a diet entry.
     * Wraps the private [updateDiet] function.
     *
     * @param diet The Diet object with edited values.
     */
    fun editDiet(diet: Diet) {
        updateDiet(diet)
    }

    /**
     * Public function exposed to UI to delete a specific diet.
     * Wraps the private [deleteDiet] function.
     *
     * @param diet The Diet object to delete.
     */
    fun removeDiet(diet: Diet) {
        deleteDiet(diet)
    }

    /**
     * Public function exposed to UI to delete all diet entries.
     * Wraps the private [clearAllDiets] function.
     */
    fun resetAllDiets() {
        clearAllDiets()
    }
}
