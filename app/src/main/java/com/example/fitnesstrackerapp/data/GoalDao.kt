package com.example.fitnesstrackerapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * GoalDao
 *
 * Data Access Object for interacting with the "goal_table" in the Room database.
 * Provides CRUD operations and supports reactive data streams via Flow.
 */
@Dao
interface GoalDao {

    /**
     * Inserts a new goal into the database.
     * If the goal already exists (same ID), it will be replaced.
     *
     * @param goal The goal to insert or replace.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal)

    /**
     * Updates an existing goal.
     *
     * @param goal The goal entity with updated values.
     */
    @Update
    suspend fun updateGoal(goal: Goal)

    /**
     * Deletes a specific goal from the database.
     *
     * @param goal The goal entity to remove.
     */
    @Delete
    suspend fun deleteGoal(goal: Goal)

    /**
     * Retrieves all goals from the database, ordered by descending ID
     * so the most recently added goals appear first.
     *
     * @return A Flow emitting a list of goals whenever the data changes.
     */
    @Query("SELECT * FROM goal_table ORDER BY id DESC")
    fun getAllGoals(): Flow<List<Goal>>

    /**
     * Deletes all goals from the database.
     * Useful for resetting or clearing user data.
     */
    @Query("DELETE FROM goal_table")
    suspend fun clearAll()
}
