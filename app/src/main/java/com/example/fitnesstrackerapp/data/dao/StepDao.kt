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
    @Query("SELECT * FROM steps WHERE userId = :userId AND date = :date LIMIT 1")
    suspend fun getStepsByDate(userId: Long, date: Date): Step?

    /**
     * Gets step records within a date range.
     *
     * @param userId User ID
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return Flow of list of steps in date range
     */
    @Query("SELECT * FROM steps WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getStepsInDateRange(userId: Long, startDate: Date, endDate: Date): Flow<List<Step>>

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
     * Gets today's step record.
     *
     * @param userId User ID
     * @param today Today's date
     * @return Today's step record or null
     */
    @Query("SELECT * FROM steps WHERE userId = :userId AND date = :today LIMIT 1")
    suspend fun getTodaysSteps(userId: Long, today: Date): Step?

    /**
     * Gets total step count for a user.
     *
     * @param userId User ID
     * @return Total steps across all records
     */
    @Query("SELECT SUM(stepCount) FROM steps WHERE userId = :userId")
    suspend fun getTotalStepCount(userId: Long): Int?

    /**
     * Gets total distance for a user.
     *
     * @param userId User ID
     * @return Total distance in meters
     */
    @Query("SELECT SUM(distanceMeters) FROM steps WHERE userId = :userId")
    suspend fun getTotalDistance(userId: Long): Float?

    /**
     * Gets total calories burned for a user.
     *
     * @param userId User ID
     * @return Total calories burned
     */
    @Query("SELECT SUM(caloriesBurned) FROM steps WHERE userId = :userId")
    suspend fun getTotalCaloriesBurned(userId: Long): Float?

    /**
     * Gets average daily steps for a user.
     *
     * @param userId User ID
     * @return Average steps per day
     */
    @Query("SELECT AVG(stepCount) FROM steps WHERE userId = :userId")
    suspend fun getAverageDailySteps(userId: Long): Float?

    /**
     * Gets step count for the last N days.
     *
     * @param userId User ID
     * @param days Number of days to look back
     * @return Flow of list of steps for the period
     */
    @Query("SELECT * FROM steps WHERE userId = :userId AND date >= date('now', '-' || :days || ' days') ORDER BY date DESC")
    fun getStepsForLastDays(userId: Long, days: Int): Flow<List<Step>>

    /**
     * Gets days where step goal was achieved.
     *
     * @param userId User ID
     * @return Flow of list of steps where goal was reached
     */
    @Query("SELECT * FROM steps WHERE userId = :userId AND stepCount >= goalSteps ORDER BY date DESC")
    fun getDaysWithGoalAchieved(userId: Long): Flow<List<Step>>

    /**
     * Gets count of days where step goal was achieved.
     *
     * @param userId User ID
     * @return Number of days goal was achieved
     */
    @Query("SELECT COUNT(*) FROM steps WHERE userId = :userId AND stepCount >= goalSteps")
    suspend fun getGoalAchievedCount(userId: Long): Int

    /**
     * Gets highest step count day for a user.
     *
     * @param userId User ID
     * @return Step record with highest count
     */
    @Query("SELECT * FROM steps WHERE userId = :userId ORDER BY stepCount DESC LIMIT 1")
    suspend fun getHighestStepDay(userId: Long): Step?

    /**
     * Updates step count for a specific date.
     *
     * @param userId User ID
     * @param date Date to update
     * @param stepCount New step count
     * @param distanceMeters New distance
     * @param caloriesBurned New calories
     */
    @Query("""
        UPDATE steps 
        SET stepCount = :stepCount, 
            distanceMeters = :distanceMeters, 
            caloriesBurned = :caloriesBurned,
            updatedAt = :currentTime
        WHERE userId = :userId AND date = :date
    """)
    suspend fun updateStepCount(
        userId: Long,
        date: Date,
        stepCount: Int,
        distanceMeters: Float,
        caloriesBurned: Float,
        currentTime: Date
    )

    /**
     * Gets weekly step summary.
     *
     * @param userId User ID
     * @param weekStart Start of week
     * @param weekEnd End of week
     * @return Weekly step statistics
     */
    @Query("""
        SELECT 
            SUM(stepCount) as totalSteps,
            SUM(distanceMeters) as totalDistance,
            SUM(caloriesBurned) as totalCalories,
            AVG(stepCount) as avgSteps,
            COUNT(*) as activeDays
        FROM steps 
        WHERE userId = :userId AND date BETWEEN :weekStart AND :weekEnd
    """)
    suspend fun getWeeklySummary(userId: Long, weekStart: Date, weekEnd: Date): WeeklyStepSummary?

    /**
     * Gets monthly step summary.
     *
     * @param userId User ID
     * @param monthStart Start of month
     * @param monthEnd End of month
     * @return Monthly step statistics
     */
    @Query("""
        SELECT 
            SUM(stepCount) as totalSteps,
            SUM(distanceMeters) as totalDistance,
            SUM(caloriesBurned) as totalCalories,
            AVG(stepCount) as avgSteps,
            COUNT(*) as activeDays
        FROM steps 
        WHERE userId = :userId AND date BETWEEN :monthStart AND :monthEnd
    """)
    suspend fun getMonthlySummary(userId: Long, monthStart: Date, monthEnd: Date): MonthlyStepSummary?

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
