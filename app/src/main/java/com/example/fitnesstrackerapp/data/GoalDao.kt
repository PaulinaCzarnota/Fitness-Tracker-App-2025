package com.example.fitnesstrackerapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * GoalDao
 *
 * DAO (Data Access Object) for the "goal_table".
 * Provides suspend functions for inserting, updating, deleting, and retrieving fitness goals.
 * Supports reactive updates using Kotlin Flow to keep the UI in sync.
 */
@Dao
interface GoalDao {

    /**
     * Inserts a new goal or replaces an existing goal with the same ID.
     *
     * @param goal The [Goal] entity to insert or replace.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal)

    /**
     * Updates an existing goal in the database.
     *
     * @param goal The [Goal] entity with updated fields.
     */
    @Update
    suspend fun updateGoal(goal: Goal)

    /**
     * Deletes a specific goal from the database.
     *
     * @param goal The [Goal] entity to delete.
     */
    @Delete
    suspend fun deleteGoal(goal: Goal)

    /**
     * Retrieves all goals stored in the database, ordered by newest first.
     * Automatically emits updates using Kotlin Flow when the data changes.
     *
     * @return A Flow list of [Goal]s, ordered by ID descending.
     */
    @Query("SELECT * FROM goal_table ORDER BY id DESC")
    fun getAllGoals(): Flow<List<Goal>>

    /**
     * Deletes all goal entries from the database.
     * Use with caution — this clears all goal progress and history.
     */
    @Query("DELETE FROM goal_table")
    suspend fun clearAll()
}
