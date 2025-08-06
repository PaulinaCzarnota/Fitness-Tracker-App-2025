package com.example.fitnesstrackerapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.fitnesstrackerapp.data.entity.Step
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Data Access Object for Step entity operations.
 *
 * Responsibilities:
 * - Insert, update, delete step records
 * - Query steps by user and date
 * - Provide step statistics and daily summaries
 * - Handle step goal tracking
 */
@Dao
interface StepDao {

    /**
     * Inserts a new step record into the database.
     *
     * @param step Step entity to insert
     * @return The ID of the inserted step record
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStep(step: Step): Long

    /**
     * Updates an existing step record in the database.
     *
     * @param step Step entity with updated data
     */
    @Update
    suspend fun updateStep(step: Step)

    /**
     * Deletes a step record from the database.
     *
     * @param step Step entity to delete
     */
    @Delete
    suspend fun deleteStep(step: Step)

    /**
     * Gets a step record by its ID.
     *
     * @param stepId Step ID to search for
     * @return Step entity or null if not found
     */
    @Query("SELECT * FROM steps WHERE id = :stepId LIMIT 1")
    suspend fun getStepById(stepId: Long): Step?

    /**
     * Gets all step records for a specific user.
     *
     * @param userId User ID
     * @return Flow of list of steps ordered by date descending
     */
    @Query("SELECT * FROM steps WHERE userId = :userId ORDER BY date DESC")
    fun getStepsByUser(userId: Long): Flow<List<Step>>

    /**
     * Gets step records for a specific date.
     *
     * @param userId User ID
     * @param date Date to search for
     * @return Step record or null if not found
     */
    @Query("SELECT * FROM steps WHERE userId = :userId AND DATE(date) = DATE(:date)")
    fun getStepsByDate(userId: Long, date: Date): Flow<List<Step>>

    /**
     * Gets step records within a date range.
     *
     * @param userId User ID
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return Flow of list of steps in date range
     */
    @Query("SELECT * FROM steps WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getStepsByDateRange(userId: Long, startDate: Date, endDate: Date): Flow<List<Step>>

    /**
     * Gets total steps for a user on a specific date.
     *
     * @param userId User ID
     * @param date Date to calculate total steps
     * @return Total steps for the date
     */
    @Query("SELECT COALESCE(SUM(stepCount), 0) FROM steps WHERE userId = :userId AND DATE(date) = DATE(:date)")
    suspend fun getTotalStepsForDate(userId: Long, date: Date): Int

    /**
     * Gets today's step count for a user.
     *
     * @param userId User ID
     * @return Today's step count
     */
    @Query("SELECT COALESCE(SUM(stepCount), 0) FROM steps WHERE userId = :userId AND DATE(date) = DATE('now')")
    suspend fun getTodayStepCount(userId: Long): Int

    /**
     * Gets recent step records with limit.
     *
     * @param userId User ID
     * @param limit Maximum number of records to return
     * @return Flow of list of recent steps
     */
    @Query("SELECT * FROM steps WHERE userId = :userId ORDER BY date DESC LIMIT :limit")
    fun getRecentSteps(userId: Long, limit: Int): Flow<List<Step>>

    /**
     * Deletes all step records for a user.
     *
     * @param userId User ID
     */
    @Query("DELETE FROM steps WHERE userId = :userId")
    suspend fun deleteAllUserSteps(userId: Long)

    /**
     * Gets step records count for a user.
     *
     * @param userId User ID
     * @return Number of step records
     */
    @Query("SELECT COUNT(*) FROM steps WHERE userId = :userId")
    suspend fun getStepRecordCount(userId: Long): Int
}

/**
 * Data class for weekly step summary.
 */
data class WeeklyStepSummary(
    val totalSteps: Int,
    val totalDistance: Float,
    val totalCalories: Float,
    val avgSteps: Float,
    val activeDays: Int
)

/**
 * Data class for monthly step summary.
 */
data class MonthlyStepSummary(
    val totalSteps: Int,
    val totalDistance: Float,
    val totalCalories: Float,
    val avgSteps: Float,
    val activeDays: Int
)
