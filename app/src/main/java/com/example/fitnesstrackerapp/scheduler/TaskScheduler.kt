/**
 * Task Scheduler for Background Operations
 *
 * Responsibilities:
 * - Schedule periodic workout reminders
 * - Manage background data sync operations
 * - Handle notification scheduling
 */
package com.example.fitnesstrackerapp.scheduler

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.fitnesstrackerapp.worker.GoalReminderWorker
import com.example.fitnesstrackerapp.worker.WorkoutReminderWorker
import java.util.concurrent.TimeUnit

/**
 * Manages scheduling of background tasks using WorkManager
 */
class TaskScheduler(context: Context) {

    private val workManager = WorkManager.getInstance(context)

    /**
     * Schedules all periodic background tasks
     */
    fun schedulePeriodicTasks() {
        // scheduleDataSync() // Disabled for now
        scheduleWorkoutReminders()
        scheduleGoalReminders()
    }

    /**
     * Schedules periodic data synchronization
     * TODO: Implement DataSyncWorker class
     */
    private fun scheduleDataSync() {
        // TODO: Implement data sync worker
        // This would handle periodic data synchronization
    }

    /**
     * Schedules workout reminder notifications
     */
    private fun scheduleWorkoutReminders() {
        val workoutReminderRequest = PeriodicWorkRequestBuilder<WorkoutReminderWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS,
        ).build()

        workManager.enqueueUniquePeriodicWork(
            "WorkoutReminderWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workoutReminderRequest,
        )
    }

    /**
     * Schedules goal reminder notifications
     */
    private fun scheduleGoalReminders() {
        val goalReminderRequest = PeriodicWorkRequestBuilder<GoalReminderWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS,
        ).build()

        workManager.enqueueUniquePeriodicWork(
            "GoalReminderWork",
            ExistingPeriodicWorkPolicy.KEEP,
            goalReminderRequest,
        )
    }

    /**
     * Cancels all scheduled tasks
     */
    fun cancelAllTasks() {
        workManager.cancelAllWork()
    }
}
