/**
 * Enhanced StepRepository
 *
 * Purpose:
 * - Manages step data for the FitnessTrackerApp with enhanced functionality
 * - Provides integration with workouts and goals for comprehensive fitness tracking
 * - Supports batch operations for battery optimization
 * - Offers advanced analytics and statistics for step data
 */

package com.example.fitnesstrackerapp.repository

import com.example.fitnesstrackerapp.data.dao.StepDao
import com.example.fitnesstrackerapp.data.entity.Step
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Date

/**
 * Repository for managing step tracking data in the Fitness Tracker App.
 *
 * Handles upsert and query operations for step data, providing a clean API for step tracking features.
 * Designed for use with Kotlin coroutines and Flow for reactive data updates.
 *
 * @property stepDao The DAO for accessing step data in the Room database
 */
class StepRepository(private val stepDao: StepDao) {

    /**
     * Upserts a step entity (inserts or updates if existing).
     *
     * @param step The [Step] entity to save.
     */
    suspend fun upsertSteps(step: Step): Long {
        return stepDao.insert(step)
    }

    /**
     * Inserts a new step record.
     */
    suspend fun insertStep(step: Step): Long = stepDao.insert(step)

    /**
     * Inserts a list of step records.
     */
    suspend fun insertSteps(steps: List<Step>): List<Long> = stepDao.insertAll(steps)

    /**
     * Updates an existing step record.
     */
    suspend fun updateStep(step: Step) = stepDao.updateStep(step)

    /**
     * Deletes a step record.
     */
    suspend fun deleteStep(step: Step) = stepDao.deleteStep(step)

    /**
     * Retrieves today's step data for a user.
     *
     * @param userId The ID of the user.
     * @return A Flow emitting a [Step] object or null if no entry exists for today.
     */
    fun getTodaysSteps(userId: Long): Flow<Step?> = stepDao.getTodaysSteps(userId)

    /**
     * Retrieves the step entry for a specific date.
     * Returns only the first match if multiple entries exist for the date.
     */
    fun getStepsForDate(userId: Long, date: Date): Flow<Step?> {
        return stepDao.getStepsForDate(userId, date)
    }

    /**
     * Retrieves step data within a specific date range using millis values.
     */
    fun getStepsInDateRange(userId: Long, startDate: Long, endDate: Long): Flow<List<Step>> {
        return stepDao.getStepsInDateRange(userId, Date(startDate), Date(endDate))
    }

    /**
     * Retrieves step data within a specific date range using Date objects.
     */
    fun getStepsInDateRange(userId: Long, startDate: Date, endDate: Date): Flow<List<Step>> {
        return stepDao.getStepsInDateRange(userId, startDate, endDate)
    }

    /**
     * Retrieves steps by exact date as a one-shot list.
     */
    suspend fun getStepsByDate(userId: Long, date: Date): List<Step> {
        return stepDao.getStepsByUserId(userId).first()
    }

    /**
     * Saves a step entry and returns its ID.
     */
    suspend fun saveSteps(step: Step): Long = stepDao.insert(step)

    // Enhanced methods for battery optimization and analytics

    /**
     * Batch inserts or updates step records for better database performance.
     */
    suspend fun batchUpsertSteps(steps: List<Step>): List<Long> {
        return stepDao.insertAll(steps)
    }

    /**
     * Gets step statistics for analytics and progress tracking.
     */
    suspend fun getStepStatistics(userId: Long): StepAnalytics? {
        return try {
            val totalSteps = stepDao.getTotalStepsForUser(userId)
            val averageSteps = stepDao.getAverageStepsForUser(userId) ?: 0f
            val maxSteps = stepDao.getMaxStepsForUser(userId) ?: 0
            val goalAchievedDays = stepDao.getGoalAchievedDays(userId)
            val goalAchievementRate = stepDao.getGoalAchievementRate(userId) ?: 0f
            val totalCalories = stepDao.getTotalCaloriesBurnedFromSteps(userId) ?: 0f
            val totalDistance = stepDao.getTotalDistanceFromSteps(userId) ?: 0f
            val bestDay = stepDao.getBestStepDay(userId)

            StepAnalytics(
                totalSteps = totalSteps,
                averageSteps = averageSteps,
                maxSteps = maxSteps,
                goalAchievedDays = goalAchievedDays,
                goalAchievementRate = goalAchievementRate,
                totalCaloriesBurned = totalCalories,
                totalDistanceMeters = totalDistance,
                bestDay = bestDay,
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Gets weekly step statistics.
     */
    suspend fun getWeeklyStatistics(userId: Long, weekStart: Date, weekEnd: Date): WeeklyStepStats? {
        return try {
            val totalSteps = stepDao.getWeeklyTotalSteps(userId, weekStart, weekEnd) ?: 0
            val averageSteps = stepDao.getWeeklyAverageSteps(userId, weekStart, weekEnd) ?: 0f
            val goalAchievedDays = stepDao.getWeeklyGoalAchievedDays(userId, weekStart, weekEnd)

            WeeklyStepStats(
                totalSteps = totalSteps,
                averageSteps = averageSteps,
                goalAchievedDays = goalAchievedDays,
                weekStart = weekStart,
                weekEnd = weekEnd,
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Gets monthly step statistics.
     */
    suspend fun getMonthlyStatistics(userId: Long, year: String, month: String): MonthlyStepStats? {
        return try {
            val totalSteps = stepDao.getMonthlyTotalSteps(userId, year, month) ?: 0
            val averageSteps = stepDao.getMonthlyAverageSteps(userId, year, month) ?: 0f
            val goalAchievedDays = stepDao.getMonthlyGoalAchievedDays(userId, year, month)

            MonthlyStepStats(
                totalSteps = totalSteps,
                averageSteps = averageSteps,
                goalAchievedDays = goalAchievedDays,
                year = year,
                month = month,
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Gets recent step records with a limit.
     */
    fun getRecentSteps(userId: Long, limit: Int = 30): Flow<List<Step>> {
        return stepDao.getRecentSteps(userId, limit)
    }

    /**
     * Gets all steps where the goal was achieved.
     */
    fun getGoalAchievedSteps(userId: Long): Flow<List<Step>> {
        return stepDao.getStepsWithGoalAchieved(userId)
    }

    /**
     * Updates step count for today.
     */
    suspend fun updateTodaySteps(
        userId: Long,
        stepCount: Int,
        caloriesBurned: Float,
        distanceMeters: Float,
        goal: Int = 10000,
    ): Long {
        val today = getCurrentDateAtMidnight()
        val currentTime = Date()

        return stepDao.insertOrUpdateSteps(
            userId = userId,
            stepCount = stepCount,
            goal = goal,
            date = today,
            caloriesBurned = caloriesBurned,
            distanceMeters = distanceMeters,
            activeMinutes = estimateActiveMinutes(stepCount),
            createdAt = currentTime,
        ).let { today.time } // Return date as ID placeholder
    }

    /**
     * Cleans up old step records to maintain database performance.
     */
    suspend fun cleanupOldSteps(userId: Long, daysToKeep: Int = 365) {
        val cutoffDate = Date(System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L))
        stepDao.deleteOldSteps(userId, cutoffDate)
    }

    /**
     * Gets step record count for a user.
     */
    suspend fun getStepRecordCount(userId: Long): Int {
        return stepDao.getStepRecordCount(userId)
    }

    // Helper methods

    private fun getCurrentDateAtMidnight(): Date {
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return calendar.time
    }

    private fun estimateActiveMinutes(steps: Int): Int {
        return (steps / 100).coerceAtMost(1440)
    }
}

/**
 * Data class for comprehensive step analytics.
 */
data class StepAnalytics(
    val totalSteps: Int,
    val averageSteps: Float,
    val maxSteps: Int,
    val goalAchievedDays: Int,
    val goalAchievementRate: Float,
    val totalCaloriesBurned: Float,
    val totalDistanceMeters: Float,
    val bestDay: Step?,
)

/**
 * Data class for weekly step statistics.
 */
data class WeeklyStepStats(
    val totalSteps: Int,
    val averageSteps: Float,
    val goalAchievedDays: Int,
    val weekStart: Date,
    val weekEnd: Date,
)

/**
 * Data class for monthly step statistics.
 */
data class MonthlyStepStats(
    val totalSteps: Int,
    val averageSteps: Float,
    val goalAchievedDays: Int,
    val year: String,
    val month: String,
)
