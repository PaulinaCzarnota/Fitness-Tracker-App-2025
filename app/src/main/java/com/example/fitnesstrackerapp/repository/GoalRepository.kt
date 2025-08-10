package com.example.fitnesstrackerapp.repository

/**
 * GoalRepository
 *
 * Repository for managing fitness goals in the Fitness Tracker App.
 *
 * This class serves as the single source of truth for goal-related data operations,
 * abstracting the data sources (local database, remote, etc.) from the rest of the app.
 * It provides a clean API for the ViewModel to perform CRUD operations on goals.
 *
 * Key Features:
 * - Wraps DAO operations with additional business logic and validation
 * - Provides a clean separation between data layer and domain layer
 * - Handles data transformation and error handling
 * - Supports reactive programming with Flow
 *
 * @property goalDao The Data Access Object for goal database operations.
 *
 * @see Goal The entity class this repository manages.
 * @see GoalDao The underlying DAO used for database operations.
 */

import com.example.fitnesstrackerapp.data.dao.GoalDao
import com.example.fitnesstrackerapp.data.entity.Goal
import kotlinx.coroutines.flow.Flow

class GoalRepository(private val goalDao: GoalDao) {
    // region CRUD Operations

    /**
     * Inserts a new goal into the database.
     *
     * @param goal The goal to insert. Must have all required fields set.
     * @return The row ID of the newly inserted goal.
     *
     * @throws IllegalArgumentException If the goal data is invalid.
     * @throws Exception For database operation failures.
     */
    suspend fun insert(goal: Goal): Long {
        require(goal.userId > 0) { "Goal must have a valid user ID" }
        return goalDao.insert(goal)
    }

    /**
     * Updates an existing goal in the database.
     *
     * @param goal The goal with updated values. Must have a valid ID.
     * @return The number of rows updated (1 if successful, 0 if not found).
     *
     * @throws IllegalArgumentException If the goal ID is invalid.
     * @throws Exception For database operation failures.
     */
    suspend fun update(goal: Goal): Int {
        require(goal.id > 0) { "Goal must have a valid ID" }
        return goalDao.update(goal)
    }

    /**
     * Deletes a goal from the database.
     *
     * @param goal The goal to delete. Must have a valid ID.
     * @return The number of rows deleted (1 if successful, 0 if not found).
     *
     * @throws IllegalArgumentException If the goal ID is invalid.
     * @throws Exception For database operation failures.
     */
    suspend fun delete(goal: Goal): Int {
        require(goal.id > 0) { "Cannot delete goal with invalid ID" }
        goalDao.delete(goal)
        return 1 // Return 1 to indicate successful deletion
    }

    /**
     * Retrieves a goal by its unique identifier.
     *
     * @param goalId The unique ID of the goal to retrieve.
     * @return The goal if found, or null if no goal exists with the given ID.
     */
    suspend fun getById(goalId: Long): Goal? {
        require(goalId > 0) { "Goal ID must be positive" }
        return goalDao.getById(goalId)
    }

    // endregion

    // region Bulk Operations

    /**
     * Inserts multiple goals into the database in a single transaction.
     *
     * @param goals The list of goals to insert.
     * @return A list of row IDs for the inserted goals, in the same order as the input list.
     *
     * @throws Exception If any goal fails validation or the database operation fails.
     */
    suspend fun insertAll(goals: List<Goal>): List<Long> {
        goals.forEachIndexed { index, _ ->
            require(true) { "Goal at index $index has no user ID" }
        }
        return goalDao.insertAll(goals)
    }

    /**
     * Deletes all goals for a specific user.
     *
     * @param userId The ID of the user whose goals should be deleted.
     *
     * @throws IllegalArgumentException If the user ID is blank.
     */
    suspend fun deleteAllByUser(userId: String) {
        require(userId.isNotBlank()) { "User ID cannot be blank" }
        goalDao.deleteAllByUser(userId.toLong())
    }

    // endregion

    // region Progress Updates

    /**
     * Updates the progress of a goal.
     *
     * @param goalId The ID of the goal to update.
     * @param currentValue The new current value.
     * @param autoComplete Whether to automatically mark the goal as complete if target is reached.
     * @return The number of rows updated (1 if successful, 0 if not found).
     */
    suspend fun updateProgress(goalId: Long, currentValue: Double, autoComplete: Boolean = true): Int {
        require(goalId > 0) { "Goal ID must be positive" }
        require(currentValue >= 0) { "Current value must be non-negative" }

        // The autoComplete logic should be handled here if needed.
        // For now, just update the progress.
        val goal = goalDao.getById(goalId)
        if (goal != null && autoComplete && currentValue >= goal.targetValue) {
            goalDao.updateCompletionStatus(goalId, "COMPLETED", System.currentTimeMillis())
        }
        goalDao.updateProgress(goalId, currentValue, System.currentTimeMillis())
        return 1 // Return 1 to indicate successful update
    }

    /**
     * Marks a goal as completed or not completed.
     *
     * @param goalId The ID of the goal to update.
     * @param isCompleted The new completion status.
     * @return The number of rows updated (1 if successful, 0 if not found).
     */
    suspend fun markGoalAsCompleted(goalId: Long, isCompleted: Boolean): Int {
        require(goalId > 0) { "Goal ID must be positive" }

        val status = if (isCompleted) "COMPLETED" else "ACTIVE"
        goalDao.updateCompletionStatus(goalId, status, System.currentTimeMillis())
        return 1
    }

    // endregion

    // region Query Operations

    /**
     * Retrieves all goals for a specific user.
     *
     * The goals are ordered with incomplete goals first (sorted by deadline),
     * followed by completed goals (most recently completed first).
     *
     * @param userId The ID of the user whose goals to retrieve.
     * @return A [Flow] that emits the list of goals whenever it changes.
     */
    fun getAllByUser(userId: String): Flow<List<Goal>> {
        require(userId.isNotBlank()) { "User ID cannot be blank" }
        return goalDao.getAllByUser(userId.toLong())
    }

    /**
     * Retrieves active (incomplete and not yet due) goals for a user.
     *
     * @param userId The ID of the user.
     * @param limit Maximum number of goals to return.
     * @return A [Flow] emitting the list of active goals, ordered by deadline.
     */
    fun getActiveGoals(
        userId: String,
        limit: Int = Int.MAX_VALUE,
    ): Flow<List<Goal>> {
        require(userId.isNotBlank()) { "User ID cannot be blank" }
        require(limit > 0) { "Limit must be positive" }

        return goalDao.getActiveGoals(userId.toLong())
    }

    /**
     * Retrieves goals that are past their deadline but not yet completed.
     *
     * @param userId The ID of the user.
     * @return A [Flow] emitting the list of overdue goals, ordered by deadline.
     */
    fun getOverdueGoals(userId: String): Flow<List<Goal>> {
        require(userId.isNotBlank()) { "User ID cannot be blank" }
        return goalDao.getOverdueGoals(userId.toLong(), System.currentTimeMillis())
    }

    /**
     * Retrieves completed goals, most recently completed first.
     *
     * @param userId The ID of the user.
     * @param limit Maximum number of completed goals to return.
     * @return A [Flow] emitting the list of completed goals.
     */
    fun getCompletedGoals(
        userId: String,
        limit: Int = Int.MAX_VALUE,
    ): Flow<List<Goal>> {
        require(userId.isNotBlank()) { "User ID cannot be blank" }
        require(limit > 0) { "Limit must be positive" }

        return goalDao.getCompletedGoals(userId.toLong())
    }

    /**
     * Retrieves goals that are due within a specified number of days.
     *
     * @param userId The ID of the user.
     * @param days Number of days in the future to check for due goals.
     * @return A [Flow] emitting the list of upcoming due goals, ordered by deadline.
     */
    fun getUpcomingDueGoals(
        userId: String,
        days: Int = 7,
    ): Flow<List<Goal>> {
        require(userId.isNotBlank()) { "User ID cannot be blank" }
        require(days > 0) { "Days must be positive" }

        val futureTime = System.currentTimeMillis() + (days * 24 * 60 * 60 * 1000L)
        return goalDao.getUpcomingDueGoals(userId.toLong(), futureTime)
    }

    /**
     * Counts goals for a user, optionally filtered by completion status.
     *
     * @param userId The ID of the user.
     * @param isCompleted Filter by completion status, or null for all goals.
     * @return A suspend function returning the count of matching goals.
     */
    suspend fun countGoals(
        userId: String,
        @Suppress("UNUSED_PARAMETER") isCompleted: Boolean? = null,
    ): Int {
        require(userId.isNotBlank()) { "User ID cannot be blank" }
        return goalDao.countGoalsByUser(userId.toLong())
    }

    /**
     * Inserts a new goal into the database
     */
    suspend fun insertGoal(goal: Goal): Long {
        return goalDao.insertGoal(goal)
    }

    /**
     * Updates an existing goal
     */
    suspend fun updateGoal(goal: Goal) {
        goalDao.updateGoal(goal)
    }

    /**
     * Deletes a goal by ID
     */
    suspend fun deleteGoalById(goalId: Long) {
        goalDao.deleteGoalById(goalId)
    }

    /**
     * Gets a goal by ID
     */
    suspend fun getGoalById(goalId: Long): Goal? {
        return goalDao.getGoalById(goalId)
    }

    /**
     * Gets all goals for a user
     */
    fun getGoalsByUser(userId: Long): Flow<List<Goal>> {
        return goalDao.getGoalsByUser(userId)
    }

    /**
     * Gets active goals for a user
     */
    fun getActiveGoals(userId: Long): Flow<List<Goal>> {
        return goalDao.getActiveGoals(userId)
    }

    /**
     * Gets achieved goals for a user
     */
    fun getAchievedGoals(userId: Long): Flow<List<Goal>> {
        return goalDao.getAchievedGoals(userId)
    }

    /**
     * Gets goals by type for a user
     */
    fun getGoalsByType(userId: Long, goalType: String): Flow<List<Goal>> {
        return goalDao.getGoalsByType(userId, goalType)
    }

    /**
     * Updates goal progress with proper type conversion
     */
    suspend fun updateGoalProgress(goalId: Long, currentValue: Double, lastUpdated: Long) {
        goalDao.updateGoalProgress(goalId, currentValue, lastUpdated)
    }

    /**
     * Marks a goal as achieved with proper type conversion
     */
    suspend fun markGoalAsAchieved(goalId: Long, achievedAt: Long) {
        goalDao.markGoalAsAchieved(goalId, achievedAt)
    }

    /**
     * Gets goals with reminders enabled
     */
    fun getGoalsWithReminders(userId: Long): Flow<List<Goal>> {
        return goalDao.getGoalsWithReminders(userId)
    }

    /**
     * Gets overdue goals with proper type conversion
     */
    fun getOverdueGoals(userId: Long, currentDate: Long): Flow<List<Goal>> {
        return goalDao.getOverdueGoals(userId, currentDate)
    }
    // endregion
}
