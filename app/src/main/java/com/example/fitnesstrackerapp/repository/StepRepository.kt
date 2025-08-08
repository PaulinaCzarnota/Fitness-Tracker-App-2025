/**
 * StepRepository
 *
 * Purpose:
 * - Manages step data for the FitnessTrackerApp
 * - Provides a clean API for interacting with step data, abstracting the Room database
 */

package com.example.fitnesstrackerapp.repository

import com.example.fitnesstrackerapp.data.dao.StepDao
import com.example.fitnesstrackerapp.data.entity.Step
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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
    suspend fun upsertSteps(step: Step) {
        stepDao.upsertSteps(step)
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
        return stepDao.getStepsForDate(userId, date).map { list -> list.firstOrNull() }
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
        return stepDao.getStepsForDateRange(userId, startDate, endDate)
    }

    /**
     * Retrieves steps by exact date as a one-shot list.
     */
    suspend fun getStepsByDate(userId: Long, date: Date): List<Step> {
        return stepDao.getStepsByDate(userId, date).first()
    }

    /**
     * Saves a step entry and returns its ID.
     */
    suspend fun saveSteps(step: Step): Long = stepDao.saveSteps(step)
}
