package com.example.fitnesstrackerapp.repository

import androidx.lifecycle.LiveData
import com.example.fitnesstrackerapp.data.Diet
import com.example.fitnesstrackerapp.data.DietDao
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository class for managing Diet data operations.
 * Provides a clean API to access diet entries from the data layer.
 */
@Singleton
class DietRepository @Inject constructor(
    private val dietDao: DietDao
) {

    /**
     * Retrieves all diet entries as a LiveData stream.
     *
     * @return a LiveData emitting lists of [Diet].
     */
    fun getAllDiets(): LiveData<List<Diet>> = dietDao.getAllDiets()

    /**
     * Inserts a new diet entry into the database.
     *
     * @param diet the Diet entity to insert.
     */
    suspend fun insertDiet(diet: Diet) = dietDao.insertDiet(diet)

    /**
     * Updates an existing diet entry in the database.
     *
     * @param diet the Diet entity with updated fields.
     */
    suspend fun updateDiet(diet: Diet) = dietDao.updateDiet(diet)

    /**
     * Deletes a diet entry from the database.
     *
     * @param diet the Diet entity to delete.
     */
    suspend fun deleteDiet(diet: Diet) = dietDao.deleteDiet(diet)

    /**
     * Clears all diet entries from the database.
     */
    suspend fun clearAllDiets() = dietDao.clearAll()
}
