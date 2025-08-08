package com.example.fitnesstrackerapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.fitnesstrackerapp.data.entity.Goal
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Goal entity operations.
 *
 * Responsibilities:
 * - Insert, update, delete goals
 * - Query goals by user, type, and status
 * - Track goal progress and achievements
 * - Handle goal reminders and notifications
 */

@Dao
interface GoalDao {

    /**
     * Inserts a new goal into the database.
     *
     * @param goal Goal entity to insert
     * @return The ID of the inserted goal
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal): Long

    /**
     * Alternative insert method for compatibility
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: Goal): Long

    /**
     * Inserts multiple goals into the database.
     *
     * @param goals List of Goal entities to insert
     * @return List of IDs of the inserted goals
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(goals: List<Goal>): List<Long>

    /**
     * Updates an existing goal in the database.
     *
     * @param goal Goal entity with updated data
     */
    @Update
    suspend fun updateGoal(goal: Goal)

    /**
     * Alternative update method for compatibility
     */
    @Update
    suspend fun update(goal: Goal)

    /**
     * Deletes a goal from the database.
     *
     * @param goal Goal entity to delete
     */
    @Delete
    suspend fun deleteGoal(goal: Goal)

    /**
     * Alternative delete method for compatibility
     */
    @Delete
    suspend fun delete(goal: Goal)

    /**
     * Deletes a goal by its ID.
     *
     * @param goalId Goal ID to delete
     */
    @Query("DELETE FROM goals WHERE id = :goalId")
    suspend fun deleteGoalById(goalId: Long)

    /**
     * Deletes all goals by user ID.
     *
     * @param userId User ID
     */
    @Query("DELETE FROM goals WHERE user_id = :userId")
    suspend fun deleteAllByUser(userId: Long)

    /**
     * Gets a goal by its ID.
     *
     * @param goalId Goal ID to search for
     * @return Goal entity or null if not found
     */
    @Query("SELECT * FROM goals WHERE id = :goalId")
    suspend fun getGoalById(goalId: Long): Goal?

    /**
     * Alternative method names for compatibility with repository calls
     */
    @Query("SELECT * FROM goals WHERE id = :goalId")
    suspend fun getById(goalId: Long): Goal?

    /**
     * Gets all goals for a specific user.
     *
     * @param userId User ID
     * @return Flow of list of goals ordered by creation date
     */
    @Query("SELECT * FROM goals WHERE user_id = :userId ORDER BY created_at DESC")
    fun getGoalsByUser(userId: Long): Flow<List<Goal>>

    /**
     * Gets all goals by user ID.
     */
    @Query("SELECT * FROM goals WHERE user_id = :userId ORDER BY created_at DESC")
    fun getAllByUser(userId: Long): Flow<List<Goal>>

    /**
     * Gets active goals for a user.
     */
    @Query("SELECT * FROM goals WHERE user_id = :userId AND status = 'ACTIVE' ORDER BY target_date ASC")
    fun getActiveGoals(userId: Long): Flow<List<Goal>>

    /**
     * Gets completed goals for a user.
     */
    @Query("SELECT * FROM goals WHERE user_id = :userId AND status = 'COMPLETED' ORDER BY created_at DESC")
    fun getCompletedGoals(userId: Long): Flow<List<Goal>>

    /**
     * Gets goals by type for a user.
     */
    @Query("SELECT * FROM goals WHERE user_id = :userId AND goal_type = :goalType ORDER BY created_at DESC")
    fun getGoalsByType(userId: Long, goalType: String): Flow<List<Goal>>

    /**
     * Gets goals by status for a user.
     */
    @Query("SELECT * FROM goals WHERE user_id = :userId AND status = :status ORDER BY created_at DESC")
    fun getGoalsByStatus(userId: Long, status: String): Flow<List<Goal>>

    /**
     * Gets the count of active goals for a user.
     */
    @Query("SELECT COUNT(*) FROM goals WHERE user_id = :userId AND status = 'ACTIVE'")
    suspend fun getActiveGoalCount(userId: Long): Int

    /**
     * Gets goals due within a specified number of days.
     */
    @Query("SELECT * FROM goals WHERE user_id = :userId AND status = 'ACTIVE' AND target_date <= :endDate ORDER BY target_date ASC")
    fun getGoalsDueSoon(userId: Long, endDate: Long): Flow<List<Goal>>

    /**
     * Gets goals within a date range.
     */
    @Query("SELECT * FROM goals WHERE user_id = :userId AND target_date BETWEEN :startDate AND :endDate ORDER BY target_date ASC")
    fun getGoalsByDateRange(userId: Long, startDate: Long, endDate: Long): Flow<List<Goal>>

    /**
     * Updates a goal's progress.
     */
    @Query("UPDATE goals SET current_value = :progress, updated_at = :updatedAt WHERE id = :goalId")
    suspend fun updateGoalProgress(goalId: Long, progress: Double, updatedAt: Long)

    /**
     * Marks a goal as achieved.
     */
    @Query("UPDATE goals SET status = 'COMPLETED', updated_at = :updatedAt WHERE id = :goalId")
    suspend fun markGoalAsAchieved(goalId: Long, updatedAt: Long)

    /**
     * Updates goal status.
     */
    @Query("UPDATE goals SET status = :status, updated_at = :updatedAt WHERE id = :goalId")
    suspend fun updateGoalStatus(goalId: Long, status: String, updatedAt: Long)

    /**
     * Increments goal progress.
     */
    @Query("UPDATE goals SET current_value = current_value + :increment, updated_at = :updatedAt WHERE id = :goalId")
    suspend fun incrementGoalProgress(goalId: Long, increment: Double, updatedAt: Long)

    /**
     * Updates goal progress
     */
    @Query("UPDATE goals SET current_value = :currentValue, updated_at = :updatedAt WHERE id = :goalId")
    suspend fun updateProgress(goalId: Long, currentValue: Double, updatedAt: Long)

    /**
     * Updates goal completion status
     */
    @Query("UPDATE goals SET status = :status, updated_at = :updatedAt WHERE id = :goalId")
    suspend fun updateCompletionStatus(goalId: Long, status: String, updatedAt: Long)

    /**
     * Gets goals that need reminders.
     */
    @Query("SELECT * FROM goals WHERE user_id = :userId AND status = 'ACTIVE' AND reminder_enabled = 1")
    fun getGoalsNeedingReminders(userId: Long): Flow<List<Goal>>

    /**
     * Gets overdue goals.
     */
    @Query("SELECT * FROM goals WHERE user_id = :userId AND status = 'ACTIVE' AND target_date < :currentDate ORDER BY target_date ASC")
    fun getOverdueGoals(userId: Long, currentDate: Long): Flow<List<Goal>>

    /**
     * Gets goals by achievement status within date range.
     */
    @Query("SELECT * FROM goals WHERE user_id = :userId AND target_date BETWEEN :startDate AND :endDate AND status = :status")
    fun getGoalsByDateRangeAndStatus(userId: Long, startDate: Long, endDate: Long, status: String): Flow<List<Goal>>

    /**
     * Gets goal count by type.
     */
    @Query("SELECT COUNT(*) FROM goals WHERE user_id = :userId AND goal_type = :goalType")
    suspend fun getGoalCountByType(userId: Long, goalType: String): Int

    /**
     * Gets completed goal count.
     */
    @Query("SELECT COUNT(*) FROM goals WHERE user_id = :userId AND status = 'COMPLETED'")
    suspend fun getCompletedGoalCount(userId: Long): Int

    /**
     * Gets total goal count for user.
     */
    @Query("SELECT COUNT(*) FROM goals WHERE user_id = :userId")
    suspend fun getTotalGoalCount(userId: Long): Int

    /**
     * Data class for goal statistics.
     */
    data class GoalStats(
        val totalGoals: Int,
        val activeGoals: Int,
        val completedGoals: Int,
        val overdueGoals: Int
    )

    /**
     * Gets goal statistics for a user.
     */
    @Query("""
        SELECT 
            COUNT(*) as totalGoals,
            SUM(CASE WHEN status = 'ACTIVE' THEN 1 ELSE 0 END) as activeGoals,
            SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as completedGoals,
            SUM(CASE WHEN status = 'ACTIVE' AND target_date < :currentDate THEN 1 ELSE 0 END) as overdueGoals
        FROM goals WHERE user_id = :userId
    """)
    suspend fun getGoalStats(userId: Long, currentDate: Long): GoalStats

    /**
     * Deletes completed goals older than specified date.
     */
    @Query("DELETE FROM goals WHERE user_id = :userId AND status = 'COMPLETED' AND updated_at < :olderThan")
    suspend fun deleteOldCompletedGoals(userId: Long, olderThan: Long)

    /**
     * Gets goals for export/backup.
     */
    @Query("SELECT * FROM goals WHERE user_id = :userId ORDER BY created_at ASC")
    fun getAllGoalsForUser(userId: Long): Flow<List<Goal>>

    /**
     * Gets the most recent goal by type.
     */
    @Query("SELECT * FROM goals WHERE user_id = :userId AND goal_type = :goalType ORDER BY created_at DESC LIMIT 1")
    suspend fun getLatestGoalByType(userId: Long, goalType: String): Goal?

    /**
     * Searches goals by title for a user.
     *
     * @param userId User ID
     * @param searchQuery Search term
     * @return Flow of matching goals
     */
    @Query("SELECT * FROM goals WHERE user_id = :userId AND title LIKE '%' || :searchQuery || '%' ORDER BY target_date ASC")
    fun searchGoals(userId: Long, searchQuery: String): Flow<List<Goal>>

    /**
     * Gets most recent goal for a user.
     *
     * @param userId User ID
     * @return Most recently created goal
     */
    @Query("SELECT * FROM goals WHERE user_id = :userId ORDER BY created_at DESC LIMIT 1")
    suspend fun getMostRecentGoal(userId: Long): Goal?

    /**
     * Gets upcoming due goals within specified days.
     */
    @Query("SELECT * FROM goals WHERE user_id = :userId AND status = 'ACTIVE' AND target_date <= :dueDate ORDER BY target_date ASC")
    fun getUpcomingDueGoals(userId: Long, dueDate: Long): Flow<List<Goal>>

    /**
     * Counts goals by user.
     */
    @Query("SELECT COUNT(*) FROM goals WHERE user_id = :userId")
    suspend fun countGoalsByUser(userId: Long): Int

    /**
     * Gets achieved goals.
     */
    @Query("SELECT * FROM goals WHERE user_id = :userId AND status = 'COMPLETED' ORDER BY updated_at DESC")
    fun getAchievedGoals(userId: Long): Flow<List<Goal>>

    /**
     * Gets goals with reminders enabled.
     */
    @Query("SELECT * FROM goals WHERE user_id = :userId AND reminder_enabled = 1 AND status = 'ACTIVE'")
    fun getGoalsWithReminders(userId: Long): Flow<List<Goal>>
}


