/**
 * Step Tracking Worker
 *
 * Responsibilities:
 * - Periodically records and syncs step count data
 * - Updates daily step goals progress
 * - Manages step count persistence across app restarts
 */
package com.example.fitnesstrackerapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.fitnesstrackerapp.repository.StepRepository
import com.example.fitnesstrackerapp.data.entity.Step
import java.util.Date

class StepTrackingWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val stepRepository: StepRepository by lazy { com.example.fitnesstrackerapp.ServiceLocator.get(applicationContext).stepRepository }

    override suspend fun doWork(): Result {
        return try {
            // Get user ID from input data
            val userId = inputData.getLong("user_id", -1L)
            if (userId == -1L) return Result.failure()

            // Create a step entry for today
            val stepEntry = Step(
                userId = userId,
                count = 0, // This would be retrieved from sensor in a real implementation
                date = Date()
            )

            // Store step count in repository
            stepRepository.saveSteps(stepEntry)

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
