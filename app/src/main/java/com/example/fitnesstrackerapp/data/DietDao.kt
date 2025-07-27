package com.example.fitnesstrackerapp.data

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * DietDao
 *
 * Data Access Object for the "diets" table in the Room database.
 * Provides methods to insert, update, delete, retrieve, and clear diet entries.
 * Uses LiveData to observe diet logs in real-time.
 */
@Dao
interface DietDao {

    /**
     * Inserts a new diet entry into the database.
     * If an entry with the same ID exists, it will be replaced.
     *
     * @param diet The Diet object to insert or replace.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiet(diet: Diet)

    /**
     * Updates an existing diet entry in the database.
     *
     * @param diet The Diet object with updated fields.
     */
    @Update
    suspend fun updateDiet(diet: Diet)

    /**
     * Deletes a specific diet entry from the database.
     *
     * @param diet The Diet object to delete.
     */
    @Delete
    suspend fun deleteDiet(diet: Diet)

    /**
     * Retrieves all diet entries from the database, ordered by most recent date first.
     *
     * @return A LiveData list of Diet objects, so the UI observes updates automatically.
     */
    @Query("SELECT * FROM diets ORDER BY date DESC")
    fun getAllDiets(): LiveData<List<Diet>>

    /**
     * Deletes all diet entries from the database.
     * Useful for resetting the diet log or clearing user data.
     */
    @Query("DELETE FROM diets")
    suspend fun clearAll()
}
