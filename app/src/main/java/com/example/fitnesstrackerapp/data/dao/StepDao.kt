package com.example.fitnesstrackerapp.data.dao

import androidx.room.*
import com.example.fitnesstrackerapp.data.entity.Step
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Date

@Dao
interface StepDao {
    @Insert
    suspend fun insert(step: Step): Long

    @Insert
    suspend fun insertAll(steps: List<Step>): List<Long>

    @Update
    suspend fun updateStep(step: Step)

    @Delete
    suspend fun deleteStep(step: Step)

    @Query("SELECT * FROM steps WHERE user_id = :userId AND date = :date LIMIT 1")
    fun getTodaysSteps(userId: Long, date: Date): Flow<Step?>

    /**
     * Gets today's steps with current date
     */
    @Query("SELECT * FROM steps WHERE user_id = :userId AND date = date('now') LIMIT 1")
    fun getTodaysSteps(userId: Long): Flow<Step?>

    @Query("SELECT * FROM steps WHERE user_id = :userId AND date BETWEEN :startDate AND :endDate")
    fun getStepsInDateRange(userId: Long, startDate: Date, endDate: Date): Flow<List<Step>>

    @Query("SELECT * FROM steps WHERE user_id = :userId ORDER BY date DESC")
    fun getStepsByUser(userId: Long): Flow<List<Step>>

    /**
     * Gets steps for a specific date
     */
    @Query("SELECT * FROM steps WHERE user_id = :userId AND date = :date")
    fun getStepsForDate(userId: Long, date: Date): Flow<List<Step>>

    /**
     * Gets steps for a date range
     */
    @Query("SELECT * FROM steps WHERE user_id = :userId AND date BETWEEN :startDate AND :endDate")
    fun getStepsForDateRange(userId: Long, startDate: Date, endDate: Date): Flow<List<Step>>

    /**
     * Gets steps by date range (alternative method name)
     */
    @Query("SELECT * FROM steps WHERE user_id = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getStepsByDateRange(userId: Long, startDate: Date, endDate: Date): Flow<List<Step>>

    /**
     * Gets steps by specific date
     */
    @Query("SELECT * FROM steps WHERE user_id = :userId AND date = :date")
    fun getStepsByDate(userId: Long, date: Date): Flow<List<Step>>

    /**
     * Gets steps by date with Long timestamp
     */
    @Query("SELECT * FROM steps WHERE user_id = :userId AND date = :date")
    fun getStepsByDate(userId: Long, date: Long): Flow<List<Step>>

    /**
     * Gets total steps for a user
     */
    @Query("SELECT COALESCE(SUM(step_count), 0) FROM steps WHERE user_id = :userId")
    suspend fun getTotalStepsForUser(userId: Long): Int

    /**
     * Gets average daily steps for a user
     */
    @Query("SELECT AVG(step_count) FROM steps WHERE user_id = :userId")
    suspend fun getAverageStepsForUser(userId: Long): Double?

    /**
     * Deletes steps older than specified date
     */
    @Query("DELETE FROM steps WHERE user_id = :userId AND date < :olderThan")
    suspend fun deleteOldSteps(userId: Long, olderThan: Date)

    /**
     * Updates step count for a specific date
     */
    @Query("UPDATE steps SET step_count = :stepCount WHERE user_id = :userId AND date = :date")
    suspend fun updateStepCount(userId: Long, date: Date, stepCount: Int)

    /**
     * Inserts or updates step count for today
     */
    @Query("INSERT OR REPLACE INTO steps (user_id, step_count, date, created_at) VALUES (:userId, :stepCount, :date, :createdAt)")
    suspend fun insertOrUpdateSteps(userId: Long, stepCount: Int, date: Date, createdAt: Date)

    /**
     * Saves step data
     */
    suspend fun saveSteps(step: Step): Long = insert(step)

    @Transaction
    suspend fun upsertSteps(step: Step) {
        val existingStep = getTodaysSteps(step.userId, step.date).first()
        if (existingStep != null) {
            updateStep(step.copy(id = existingStep.id))
        } else {
            insert(step)
        }
    }
}
