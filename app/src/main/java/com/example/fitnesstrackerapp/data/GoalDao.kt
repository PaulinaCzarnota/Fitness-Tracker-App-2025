package com.example.fitnesstrackerapp.data

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * GoalDao
 *
 * Data Access Object for interacting with the "goals" table in the Room database.
 * Supports basic CRUD operations and real-time observation via LiveData.
 */
@Dao
interface GoalDao {

    /**
     * Inserts a new goal into the database.
     * If a goal with the same ID already exists, it will be replaced.
     *
     * @param goal The Goal entity to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal)

    /**
     * Updates an existing goal's progress or status.
     *
     * @param goal The Goal entity with updated values.
     */
    @Update
    suspend fun updateGoal(goal: Goal)

    /**
     * Deletes a specific goal from the database.
     *
     * @param goal The Goal to remove.
     */
    @Delete
    suspend fun deleteGoal(goal: Goal)

    /**
     * Retrieves all stored goals, sorted by most recently added (descending ID).
     *
     * @return LiveData list of all Goal entities.
     */
    @Query("SELECT * FROM goals ORDER BY id DESC")
    fun getAllGoals(): LiveData<List<Goal>>

    /**
     * Removes all goals from the database.
     * Can be used for resetting the app or clearing data.
     */
    @Query("DELETE FROM goals")
    suspend fun clearAll()
}
