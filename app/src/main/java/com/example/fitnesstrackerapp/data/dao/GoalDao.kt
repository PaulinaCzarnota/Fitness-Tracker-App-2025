package com.example.fitnesstrackerapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.fitnesstrackerapp.data.entity.Goal
import com.example.fitnesstrackerapp.data.entity.GoalType
import kotlinx.coroutines.flow.Flow
import java.util.Date

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
     * Updates an existing goal in the database.
     *
     * @param goal Goal entity with updated data
     */
    @Update
    suspend fun updateGoal(goal: Goal)

    /**
     * Deletes a goal from the database.
     *
     * @param goal Goal entity to delete
     */
    @Delete
    suspend fun deleteGoal(goal: Goal)

    /**
     * Deletes a goal by its ID.
     *
     * @param goalId Goal ID to delete
     */
    @Query("DELETE FROM goals WHERE id = :goalId")
    suspend fun deleteGoalById(goalId: Long)

    /**
     * Gets a goal by its ID.
     *
     * @param goalId Goal ID to search for
     * @return Goal entity or null if not found
     */
    @Query("SELECT * FROM goals WHERE id = :goalId")
    suspend fun getGoalById(goalId: Long): Goal?

    /**
     * Gets all goals for a specific user.
     *
     * @param userId User ID
     * @return Flow of list of goals ordered by creation date
     */
    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY createdAt DESC")
    fun getGoalsByUser(userId: Long): Flow<List<Goal>>

    /**
     * Gets active goals for a specific user.
     *
     * @param userId User ID
     * @return Flow of list of active goals
     */
    @Query("SELECT * FROM goals WHERE userId = :userId AND isActive = 1 AND isAchieved = 0 ORDER BY targetDate ASC")
    fun getActiveGoals(userId: Long): Flow<List<Goal>>

    /**
     * Gets achieved goals for a specific user.
     *
     * @param userId User ID
     * @return Flow of list of achieved goals
     */
    @Query("SELECT * FROM goals WHERE userId = :userId AND isAchieved = 1 ORDER BY achievedDate DESC")
    fun getAchievedGoals(userId: Long): Flow<List<Goal>>

    /**
     * Gets goals by type for a user.
     *
     * @param userId User ID
     * @param goalType Type of goal
     * @return Flow of list of goals of specified type
     */
    @Query("SELECT * FROM goals WHERE userId = :userId AND goalType = :goalType ORDER BY createdAt DESC")
    fun getGoalsByType(userId: Long, goalType: GoalType): Flow<List<Goal>>

    /**
     * Updates goal progress.
     *
     * @param goalId Goal ID
     * @param currentValue New current value
     * @param lastUpdated Update timestamp
     */
    @Query("UPDATE goals SET currentValue = :currentValue, updatedAt = :lastUpdated WHERE id = :goalId")
    suspend fun updateGoalProgress(goalId: Long, currentValue: Float, lastUpdated: Date)

    /**
     * Marks a goal as achieved.
     *
     * @param goalId Goal ID
     * @param achievedAt Achievement date
     */
    @Query("UPDATE goals SET isAchieved = 1, achievedDate = :achievedAt WHERE id = :goalId")
    suspend fun markGoalAsAchieved(goalId: Long, achievedAt: Date)

    /**
     * Gets goals with reminders enabled.
     *
     * @param userId User ID
     * @return Flow of goals with reminders
     */
    @Query("SELECT * FROM goals WHERE userId = :userId AND reminderEnabled = 1 AND isActive = 1 AND isAchieved = 0")
    fun getGoalsWithReminders(userId: Long): Flow<List<Goal>>

    /**
     * Gets overdue goals.
     *
     * @param userId User ID
     * @param currentDate Current date
     * @return Flow of list of overdue goals
     */
    @Query("SELECT * FROM goals WHERE userId = :userId AND targetDate < :currentDate AND isAchieved = 0 AND isActive = 1")
    fun getOverdueGoals(userId: Long, currentDate: Date): Flow<List<Goal>>

    /**
     * Gets goals due soon (within specified days).
     *
     * @param userId User ID
     * @param startDate Start date (today)
     * @param endDate End date (future cutoff)
     * @return Flow of list of goals due soon
     */
    @Query("SELECT * FROM goals WHERE userId = :userId AND targetDate BETWEEN :startDate AND :endDate AND isAchieved = 0 AND isActive = 1 ORDER BY targetDate ASC")
    fun getGoalsDueSoon(userId: Long, startDate: Date, endDate: Date): Flow<List<Goal>>

    /**
     * Gets goal count for a user.
     *
     * @param userId User ID
     * @return Total number of goals
     */
    @Query("SELECT COUNT(*) FROM goals WHERE userId = :userId")
    suspend fun getGoalCount(userId: Long): Int

    /**
     * Gets active goal count for a user.
     *
     * @param userId User ID
     * @return Number of active goals
     */
    @Query("SELECT COUNT(*) FROM goals WHERE userId = :userId AND isActive = 1")
    suspend fun getActiveGoalCount(userId: Long): Int

    /**
     * Gets achieved goal count for a user.
     *
     * @param userId User ID
     * @return Number of achieved goals
     */
    @Query("SELECT COUNT(*) FROM goals WHERE userId = :userId AND isAchieved = 1")
    suspend fun getAchievedGoalCount(userId: Long): Int

    /**
     * Gets goal statistics for a user.
     *
     * @param userId User ID
     * @return Goal statistics summary
     */
    @Query("""
        SELECT 
            COUNT(*) as totalGoals,
            SUM(CASE WHEN isActive = 1 THEN 1 ELSE 0 END) as activeGoals,
            SUM(CASE WHEN isAchieved = 1 THEN 1 ELSE 0 END) as achievedGoals,
            AVG(CASE WHEN isAchieved = 1 THEN (currentValue * 100.0 / targetValue) ELSE NULL END) as avgCompletionRate
        FROM goals 
        WHERE userId = :userId
    """)
    suspend fun getGoalStats(userId: Long): GoalStats?

    /**
     * Deletes all goals for a user.
     *
     * @param userId User ID
     */
    @Query("DELETE FROM goals WHERE userId = :userId")
    suspend fun deleteAllUserGoals(userId: Long)

    /**
     * Searches goals by title.
     *
     * @param userId User ID
     * @param searchQuery Search term
     * @return Flow of matching goals
     */
    @Query("SELECT * FROM goals WHERE userId = :userId AND title LIKE '%' || :searchQuery || '%' ORDER BY targetDate ASC")
    fun searchGoals(userId: Long, searchQuery: String): Flow<List<Goal>>

    /**
     * Gets most recent goal for a user.
     *
     * @param userId User ID
     * @return Most recently created goal
     */
    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getMostRecentGoal(userId: Long): Goal?
}

/**
 * Data class for goal statistics.
 */
data class GoalStats(
    val totalGoals: Int,
    val activeGoals: Int,
    val achievedGoals: Int,
    val avgCompletionRate: Float?
)
