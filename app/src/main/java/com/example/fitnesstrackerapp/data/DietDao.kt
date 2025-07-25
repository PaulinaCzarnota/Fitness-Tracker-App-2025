package com.example.fitnesstrackerapp.data

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * DietDao
 *
 * DAO interface for the 'diets' table in Room.
 * Provides operations to insert, update, delete, and observe food entries.
 */
@Dao
interface DietDao {

    /**
     * Inserts a new diet entry.
     * If the ID already exists, replaces the entry.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiet(diet: Diet)

    /**
     * Updates an existing diet record.
     */
    @Update
    suspend fun updateDiet(diet: Diet)

    /**
     * Deletes a specific diet record.
     */
    @Delete
    suspend fun deleteDiet(diet: Diet)

    /**
     * Returns all food logs ordered from most recent to oldest.
     */
    @Query("SELECT * FROM diets ORDER BY date DESC")
    fun getAllDiets(): LiveData<List<Diet>>

    /**
     * Removes all diet entries from the table.
     */
    @Query("DELETE FROM diets")
    suspend fun clearAll()
}
