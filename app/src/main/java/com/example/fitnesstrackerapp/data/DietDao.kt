package com.example.fitnesstrackerapp.data

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * DAO (Data Access Object) for handling database operations related to diet entries.
 */
@Dao
interface DietDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiet(diet: Diet)

    @Update
    suspend fun updateDiet(diet: Diet)

    @Delete
    suspend fun deleteDiet(diet: Diet)

    @Query("SELECT * FROM diets ORDER BY date DESC")
    fun getAllDiets(): LiveData<List<Diet>>

    @Query("DELETE FROM diets")
    suspend fun clearAll() // Required for clearAllDiets()
}
