package com.example.fitnesstrackerapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.fitnesstrackerapp.repository.GoalRepository
import com.example.fitnesstrackerapp.notifications.NotificationHelper
import java.util.*

class NotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val goalRepository: GoalRepository by lazy { com.example.fitnesstrackerapp.ServiceLocator.get(applicationContext).goalRepository }
    private val notificationHelper = NotificationHelper(appContext)

    override suspend fun doWork(): Result {
        return try {
            // Get goals that need reminders
            val userId = inputData.getLong("user_id", -1L)
            if (userId == -1L) return Result.failure()

            // Send daily motivation notification
            notificationHelper.sendDailyReminder()

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
