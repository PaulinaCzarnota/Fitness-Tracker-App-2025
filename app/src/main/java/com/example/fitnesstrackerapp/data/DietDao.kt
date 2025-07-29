package com.example.fitnesstrackerapp.data

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * DietDao
 *
 * Data Access Object for the "diets" table.
 * Defines SQL operations for inserting, updating, deleting, and retrieving diet entries.
 * Uses LiveData so UI components can react to changes automatically.
 */
@Dao
interface DietDao {

    /**
     * Inserts a new Diet entry into the database.
     * If an entry with the same primary key exists, it will be replaced.
     *
     * @param diet The Diet object to insert or replace.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiet(diet: Diet)

    /**
     * Updates an existing Diet entry.
     *
     * @param diet The Diet object containing updated fields.
     */
    @Update
    suspend fun updateDiet(diet: Diet)

    /**
     * Deletes a specific Diet entry.
     *
     * @param diet The Diet object to delete from the database.
     */
    @Delete
    suspend fun deleteDiet(diet: Diet)

    /**
     * Retrieves all diet entries ordered by newest first.
     *
     * @return A LiveData list of Diet objects (observed automatically by the UI).
     */
    @Query("SELECT * FROM diets ORDER BY date DESC")
    fun getAllDiets(): LiveData<List<Diet>>

    /**
     * Deletes all diet entries from the database.
     * This is useful for resetting logs or clearing all user diet data.
     */
    @Query("DELETE FROM diets")
    suspend fun clearAll()
}
