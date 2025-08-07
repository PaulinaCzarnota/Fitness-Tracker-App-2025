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

    @Query("SELECT * FROM steps WHERE userId = :userId AND date = :date LIMIT 1")
    fun getTodaysSteps(userId: Long, date: Date): Flow<Step?>

    /**
     * Gets today's steps with current date
     */
    @Query("SELECT * FROM steps WHERE userId = :userId AND date = date('now') LIMIT 1")
    fun getTodaysSteps(userId: Long): Flow<Step?>

    @Query("SELECT * FROM steps WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    fun getStepsInDateRange(userId: Long, startDate: Date, endDate: Date): Flow<List<Step>>

    @Query("SELECT * FROM steps WHERE userId = :userId ORDER BY date DESC")
    fun getStepsByUser(userId: Long): Flow<List<Step>>

    /**
     * Gets steps for a specific date
     */
    @Query("SELECT * FROM steps WHERE userId = :userId AND date = :date")
    fun getStepsForDate(userId: Long, date: Date): Flow<List<Step>>

    /**
     * Gets steps for a date range
     */
    @Query("SELECT * FROM steps WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    fun getStepsForDateRange(userId: Long, startDate: Date, endDate: Date): Flow<List<Step>>

    /**
     * Gets steps by date range (alternative method name)
     */
    @Query("SELECT * FROM steps WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    fun getStepsByDateRange(userId: Long, startDate: Date, endDate: Date): Flow<List<Step>>

    @Query("SELECT SUM(count) FROM steps WHERE userId = :userId AND date = :date")
    suspend fun getTotalStepsForDate(userId: Long, date: Date): Int

    @Query("SELECT SUM(count) FROM steps WHERE userId = :userId AND date = date('now')")
    suspend fun getTodayStepCount(userId: Long): Int

    @Query("SELECT * FROM steps WHERE userId = :userId ORDER BY date DESC LIMIT :limit")
    fun getRecentSteps(userId: Long, limit: Int): Flow<List<Step>>

    @Query("DELETE FROM steps WHERE userId = :userId")
    suspend fun deleteAllUserSteps(userId: Long)

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
