package com.example.fitnesstrackerapp.data

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * DietDao
 *
 * DAO interface for managing diet entries in the Room database.
 * Provides methods for inserting, updating, deleting, and querying diet logs.
 */
@Dao
interface DietDao {

    /**
     * Inserts a new diet entry into the database.
     * If a conflict occurs on the primary key, the existing entry is replaced.
     *
     * @param diet The Diet entity to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiet(diet: Diet)

    /**
     * Updates an existing diet entry.
     *
     * @param diet The modified Diet object.
     */
    @Update
    suspend fun updateDiet(diet: Diet)

    /**
     * Deletes a specific diet entry from the database.
     *
     * @param diet The Diet object to remove.
     */
    @Delete
    suspend fun deleteDiet(diet: Diet)

    /**
     * Retrieves all diet entries sorted by date in descending order.
     *
     * @return A LiveData list of all diet entries for observation by the UI.
     */
    @Query("SELECT * FROM diets ORDER BY date DESC")
    fun getAllDiets(): LiveData<List<Diet>>

    /**
     * Deletes all entries from the diets table.
     */
    @Query("DELETE FROM diets")
    suspend fun clearAll()
}
