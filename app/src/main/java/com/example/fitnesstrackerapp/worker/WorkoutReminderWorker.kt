/**
 * Workout Reminder Worker
 *
 * Responsibilities:
 * - Sends reminders to users about their workout schedule
 * - Checks for inactive periods and motivates users
 * - Runs periodically via WorkManager
 */
package com.example.fitnesstrackerapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.fitnesstrackerapp.notifications.NotificationHelper
import com.example.fitnesstrackerapp.repository.WorkoutRepository
import java.util.*

class WorkoutReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val workoutRepository: WorkoutRepository by lazy { com.example.fitnesstrackerapp.ServiceLocator.get(applicationContext).workoutRepository }
    private val notificationHelper = NotificationHelper(context)

    override suspend fun doWork(): Result {
        return try {
            val userId = inputData.getLong("user_id", -1L)
            if (userId == -1L) return Result.failure()

            // Send workout reminder notification
            notificationHelper.sendWorkoutReminder()

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
