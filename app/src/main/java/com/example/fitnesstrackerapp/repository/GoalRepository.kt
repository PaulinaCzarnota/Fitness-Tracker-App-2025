package com.example.fitnesstrackerapp.repository

import com.example.fitnesstrackerapp.data.Goal
import com.example.fitnesstrackerapp.data.GoalDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository class for managing Goal data operations.
 * Provides a clean API to access goal entries from the data layer.
 */
@Singleton
class GoalRepository @Inject constructor(
    private val goalDao: GoalDao
) {

    /**
     * Retrieves all goal entries as a Flow stream.
     *
     * @return a Flow emitting lists of [Goal].
     */
    fun getAllGoals(): Flow<List<Goal>> = goalDao.getAllGoals()

    /**
     * Inserts a new goal entry into the database.
     *
     * @param goal the Goal entity to insert.
     */
    suspend fun insertGoal(goal: Goal) = goalDao.insertGoal(goal)

    /**
     * Updates an existing goal entry in the database.
     *
     * @param goal the Goal entity with updated fields.
     */
    suspend fun updateGoal(goal: Goal) = goalDao.updateGoal(goal)

    /**
     * Deletes a goal entry from the database.
     *
     * @param goal the Goal entity to delete.
     */
    suspend fun deleteGoal(goal: Goal) = goalDao.deleteGoal(goal)

    /**
     * Clears all goal entries from the database.
     */
    suspend fun clearAllGoals() = goalDao.clearAll()
}
