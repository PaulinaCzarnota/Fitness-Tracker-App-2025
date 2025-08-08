/**
 * Workout Scheduler
 *
 * Responsibilities:
 * - Schedules workout reminders and notifications
 * - Manages background tasks for fitness tracking
 * - Coordinates with WorkManager for periodic tasks
 * - Handles goal reminder scheduling
 */

package com.example.fitnesstrackerapp.scheduler

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.fitnesstrackerapp.worker.GoalReminderWorker
import com.example.fitnesstrackerapp.worker.WorkoutReminderWorker
import java.util.concurrent.TimeUnit

class WorkoutScheduler(context: Context) {

    companion object {
        private const val WORKOUT_REMINDER_WORK = "workout_reminder_work"
        private const val GOAL_REMINDER_WORK = "goal_reminder_work"
        private const val DAILY_REMINDER_WORK = "daily_reminder_work"
    }

    private val workManager = WorkManager.getInstance(context)

    /**
     * Schedule daily workout reminders
     */
    fun scheduleWorkoutReminders(reminderTime: String = "18:00") {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(true)
            .build()

        val reminderRequest = PeriodicWorkRequestBuilder<WorkoutReminderWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .setInitialDelay(calculateDelayUntilReminderTime(reminderTime), TimeUnit.MILLISECONDS)
            .addTag(WORKOUT_REMINDER_WORK)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORKOUT_REMINDER_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            reminderRequest,
        )
    }

    /**
     * Schedule goal progress reminders
     */
    fun scheduleGoalReminders() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val goalReminderRequest = PeriodicWorkRequestBuilder<GoalReminderWorker>(12, TimeUnit.HOURS)
            .setConstraints(constraints)
            .addTag(GOAL_REMINDER_WORK)
            .build()

        workManager.enqueueUniquePeriodicWork(
            GOAL_REMINDER_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            goalReminderRequest,
        )
    }

    /**
     * Schedule motivational messages
     */
    fun scheduleMotivationalMessages() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val motivationalRequest = PeriodicWorkRequestBuilder<WorkoutReminderWorker>(2, TimeUnit.DAYS)
            .setConstraints(constraints)
            .addTag(DAILY_REMINDER_WORK)
            .build()

        workManager.enqueueUniquePeriodicWork(
            DAILY_REMINDER_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            motivationalRequest,
        )
    }

    /**
     * Cancel all scheduled reminders
     */
    fun cancelAllReminders() {
        workManager.cancelUniqueWork(WORKOUT_REMINDER_WORK)
        workManager.cancelUniqueWork(GOAL_REMINDER_WORK)
        workManager.cancelUniqueWork(DAILY_REMINDER_WORK)
    }

    /**
     * Cancel workout reminders
     */
    fun cancelWorkoutReminders() {
        workManager.cancelUniqueWork(WORKOUT_REMINDER_WORK)
    }

    /**
     * Calculate delay until reminder time
     */
    private fun calculateDelayUntilReminderTime(reminderTime: String): Long {
        try {
            val timeParts = reminderTime.split(":")
            val hour = timeParts[0].toInt()
            val minute = timeParts[1].toInt()

            val now = System.currentTimeMillis()
            val calendar = java.util.Calendar.getInstance().apply {
                timeInMillis = now
                set(java.util.Calendar.HOUR_OF_DAY, hour)
                set(java.util.Calendar.MINUTE, minute)
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)

                // If the time has already passed today, schedule for tomorrow
                if (timeInMillis <= now) {
                    add(java.util.Calendar.DAY_OF_YEAR, 1)
                }
            }

            return calendar.timeInMillis - now
        } catch (e: Exception) {
            // Default to 1 hour delay if parsing fails
            return TimeUnit.HOURS.toMillis(1)
        }
    }
}
