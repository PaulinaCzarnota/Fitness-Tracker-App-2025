package com.example.fitnesstrackerapp.data

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * Data Access Object (DAO) for managing fitness goals in the Room database.
 * Provides methods to insert, update, and retrieve goal records.
 */
@Dao
interface GoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal)

    @Update
    suspend fun updateGoal(goal: Goal)

    @Delete
    suspend fun deleteGoal(goal: Goal)

    @Query("SELECT * FROM goals ORDER BY id DESC")
    fun getAllGoals(): LiveData<List<Goal>>

    @Query("DELETE FROM goals")
    suspend fun clearAll() // This fixes the unresolved reference error
}
