package com.example.fitnesstrackerapp.data.dao

import androidx.room.*
import com.example.fitnesstrackerapp.data.entity.Step
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Step Data Access Object for the Fitness Tracker application.
 *
 * This DAO provides comprehensive database operations for Step entities including
 * daily step tracking, goal monitoring, historical data analysis, and statistics.
 * All operations are coroutine-based for optimal performance and UI responsiveness.
 *
 * Key Features:
 * - Daily step count tracking and updates
 * - Goal progress monitoring and achievement tracking
 * - Historical step data for analytics and trends
 * - Date range queries for progress visualization
 * - Statistics calculation for performance insights
 * - Automatic data cleanup and maintenance
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
     * Alternative insert method for compatibility.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(step: Step): Long

    /**
     * Inserts multiple step records into the database.
     *
     * @param steps List of Step entities to insert
     * @return List of IDs of the inserted step records
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(steps: List<Step>): List<Long>

    /**
     * Updates an existing step record in the database.
     *
     * @param step Step entity with updated data
     */
    @Update
    suspend fun updateStep(step: Step)

    /**
     * Updates step count for a specific date.
     *
     * @param userId User ID
     * @param date Date to update
     * @param stepCount New step count
     * @param updatedAt Update timestamp
     */
    @Query("UPDATE steps SET step_count = :stepCount, updated_at = :updatedAt WHERE user_id = :userId AND date = :date")
    suspend fun updateStepCount(userId: Long, date: Date, stepCount: Int, updatedAt: Date)

    /**
     * Inserts or updates step record for a specific date.
     *
     * @param userId User ID
     * @param stepCount Step count
     * @param date Date
     * @param goal Daily step goal
     * @param caloriesBurned Calories burned from steps
     * @param distanceMeters Distance in meters
     * @param activeMinutes Active minutes
     * @param createdAt Creation timestamp
     */
    @Query("""
        INSERT OR REPLACE INTO steps 
        (user_id, step_count, step_goal, date, calories_burned, distance_meters, active_minutes, created_at, updated_at) 
        VALUES (:userId, :stepCount, :goal, :date, :caloriesBurned, :distanceMeters, :activeMinutes, :createdAt, :createdAt)
    """)
    suspend fun insertOrUpdateSteps(
        userId: Long,
        stepCount: Int,
        goal: Int,
        date: Date,
        caloriesBurned: Float,
        distanceMeters: Float,
        activeMinutes: Int,
        createdAt: Date
    )

    /**
     * Deletes a step record from the database.
     *
     * @param step Step entity to delete
     */
    @Delete
    suspend fun deleteStep(step: Step)

    /**
     * Gets today's step record for a user on a specific date.
     *
     * @param userId User ID
     * @param date Specific date
     * @return Flow of Step entity or null if not found
     */
    @Query("SELECT * FROM steps WHERE user_id = :userId AND date = :date LIMIT 1")
    fun getStepsForDate(userId: Long, date: Date): Flow<Step?>

    /**
     * Gets all step records for a specific user.
     *
     * @param userId User ID
     * @return Flow of list of step records ordered by date descending
     */
    @Query("SELECT * FROM steps WHERE user_id = :userId ORDER BY date DESC")
    fun getStepsByUserId(userId: Long): Flow<List<Step>>

    /**
     * Gets step records within a date range for a user.
     *
     * @param userId User ID
     * @param startDate Start date of range
     * @param endDate End date of range
     * @return Flow of list of step records in date range
     */
    @Query("SELECT * FROM steps WHERE user_id = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getStepsInDateRange(userId: Long, startDate: Date, endDate: Date): Flow<List<Step>>

    /**
     * Gets recent step records with limit.
     *
     * @param userId User ID
     * @param limit Maximum number of records to return
     * @return Flow of recent step records
     */
    @Query("SELECT * FROM steps WHERE user_id = :userId ORDER BY date DESC LIMIT :limit")
    fun getRecentSteps(userId: Long, limit: Int): Flow<List<Step>>

    /**
     * Gets total step count for a user across all records.
     *
     * @param userId User ID
     * @return Total step count
     */
    @Query("SELECT COALESCE(SUM(step_count), 0) FROM steps WHERE user_id = :userId")
    suspend fun getTotalStepsForUser(userId: Long): Int

    /**
     * Gets average daily steps for a user.
     *
     * @param userId User ID
     * @return Average daily step count
     */
    @Query("SELECT AVG(step_count) FROM steps WHERE user_id = :userId")
    suspend fun getAverageStepsForUser(userId: Long): Float?

    /**
     * Gets maximum daily steps for a user.
     *
     * @param userId User ID
     * @return Maximum daily step count
     */
    @Query("SELECT MAX(step_count) FROM steps WHERE user_id = :userId")
    suspend fun getMaxStepsForUser(userId: Long): Int?

    /**
     * Gets total steps for a user.
     *
     * @param userId User ID
     * @return Total step count
     */
    @Query("SELECT SUM(step_count) FROM steps WHERE user_id = :userId")
    suspend fun getTotalStepsCount(userId: Long): Int?

    /**
     * Gets number of days user achieved their step goal.
     *
     * @param userId User ID
     * @return Count of days with goal achieved
     */
    @Query("SELECT COUNT(*) FROM steps WHERE user_id = :userId AND step_count >= step_goal")
    suspend fun getGoalAchievedDays(userId: Long): Int

    /**
     * Gets step goal achievement rate.
     *
     * @param userId User ID
     * @return Achievement rate as percentage
     */
    @Query("SELECT (COUNT(CASE WHEN step_count >= step_goal THEN 1 END) * 100.0 / COUNT(*)) FROM steps WHERE user_id = :userId")
    suspend fun getGoalAchievementRate(userId: Long): Float?

    /**
     * Gets weekly total steps.
     *
     * @param userId User ID
     * @param weekStart Start of the week
     * @param weekEnd End of the week
     * @return Total steps for the week
     */
    @Query("""
        SELECT SUM(step_count)
        FROM steps 
        WHERE user_id = :userId 
        AND date BETWEEN :weekStart AND :weekEnd
    """)
    suspend fun getWeeklyTotalSteps(userId: Long, weekStart: Date, weekEnd: Date): Int?

    /**
     * Gets weekly average steps.
     *
     * @param userId User ID
     * @param weekStart Start of the week
     * @param weekEnd End of the week
     * @return Average steps for the week
     */
    @Query("""
        SELECT AVG(step_count)
        FROM steps 
        WHERE user_id = :userId 
        AND date BETWEEN :weekStart AND :weekEnd
    """)
    suspend fun getWeeklyAverageSteps(userId: Long, weekStart: Date, weekEnd: Date): Float?

    /**
     * Gets weekly goal achieved days.
     *
     * @param userId User ID
     * @param weekStart Start of the week
     * @param weekEnd End of the week
     * @return Days with goal achieved in the week
     */
    @Query("""
        SELECT COUNT(CASE WHEN step_count >= step_goal THEN 1 END)
        FROM steps 
        WHERE user_id = :userId 
        AND date BETWEEN :weekStart AND :weekEnd
    """)
    suspend fun getWeeklyGoalAchievedDays(userId: Long, weekStart: Date, weekEnd: Date): Int

    /**
     * Gets monthly total steps.
     *
     * @param userId User ID
     * @param year Year
     * @param month Month (1-12)
     * @return Total steps for the month
     */
    @Query("""
        SELECT SUM(step_count)
        FROM steps 
        WHERE user_id = :userId 
        AND strftime('%Y', date/1000, 'unixepoch') = :year 
        AND strftime('%m', date/1000, 'unixepoch') = :month
    """)
    suspend fun getMonthlyTotalSteps(userId: Long, year: String, month: String): Int?

    /**
     * Gets monthly average steps.
     *
     * @param userId User ID
     * @param year Year
     * @param month Month (1-12)
     * @return Average steps for the month
     */
    @Query("""
        SELECT AVG(step_count)
        FROM steps 
        WHERE user_id = :userId 
        AND strftime('%Y', date/1000, 'unixepoch') = :year 
        AND strftime('%m', date/1000, 'unixepoch') = :month
    """)
    suspend fun getMonthlyAverageSteps(userId: Long, year: String, month: String): Float?

    /**
     * Gets monthly goal achieved days.
     *
     * @param userId User ID
     * @param year Year
     * @param month Month (1-12)
     * @return Days with goal achieved in the month
     */
    @Query("""
        SELECT COUNT(CASE WHEN step_count >= step_goal THEN 1 END)
        FROM steps 
        WHERE user_id = :userId 
        AND strftime('%Y', date/1000, 'unixepoch') = :year 
        AND strftime('%m', date/1000, 'unixepoch') = :month
    """)
    suspend fun getMonthlyGoalAchievedDays(userId: Long, year: String, month: String): Int

    /**
     * Gets total calories burned from steps.
     *
     * @param userId User ID
     * @return Total calories burned
     */
    @Query("SELECT SUM(calories_burned) FROM steps WHERE user_id = :userId")
    suspend fun getTotalCaloriesBurnedFromSteps(userId: Long): Float?

    /**
     * Gets total distance covered from steps.
     *
     * @param userId User ID
     * @return Total distance in meters
     */
    @Query("SELECT SUM(distance_meters) FROM steps WHERE user_id = :userId")
    suspend fun getTotalDistanceFromSteps(userId: Long): Float?

    /**
     * Gets step records where goal was achieved.
     *
     * @param userId User ID
     * @return Flow of step records with goal achieved
     */
    @Query("SELECT * FROM steps WHERE user_id = :userId AND step_count >= step_goal ORDER BY date DESC")
    fun getStepsWithGoalAchieved(userId: Long): Flow<List<Step>>

    /**
     * Gets the best step day (highest count).
     *
     * @param userId User ID
     * @return Step record with highest count
     */
    @Query("SELECT * FROM steps WHERE user_id = :userId ORDER BY step_count DESC LIMIT 1")
    suspend fun getBestStepDay(userId: Long): Step?

    /**
     * Deletes step records older than specified date.
     *
     * @param userId User ID
     * @param olderThan Cutoff date
     */
    @Query("DELETE FROM steps WHERE user_id = :userId AND date < :olderThan")
    suspend fun deleteOldSteps(userId: Long, olderThan: Date)

    /**
     * Deletes all step records for a user.
     *
     * @param userId User ID
     */
    @Query("DELETE FROM steps WHERE user_id = :userId")
    suspend fun deleteAllStepsForUser(userId: Long)

    /**
     * Deletes all step records (for testing purposes only).
     */
    @Query("DELETE FROM steps")
    suspend fun deleteAllSteps()

    /**
     * Gets count of total step records for a user.
     *
     * @param userId User ID
     * @return Total number of step records
     */
    @Query("SELECT COUNT(*) FROM steps WHERE user_id = :userId")
    suspend fun getStepRecordCount(userId: Long): Int
}
