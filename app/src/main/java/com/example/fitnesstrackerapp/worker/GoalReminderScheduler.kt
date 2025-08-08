package com.example.fitnesstrackerapp.worker

import android.content.Context
import androidx.work.*
import com.example.fitnesstrackerapp.data.entity.Goal
import com.example.fitnesstrackerapp.settings.UserPreferences
import com.example.fitnesstrackerapp.util.AppConstants.FREQUENCY_DAILY
import com.example.fitnesstrackerapp.util.AppConstants.FREQUENCY_MONTHLY
import com.example.fitnesstrackerapp.util.AppConstants.FREQUENCY_WEEKLY
import com.example.fitnesstrackerapp.util.AppConstants.MIN_BACKOFF_MILLIS
import java.util.concurrent.TimeUnit
import kotlin.random.Random

/**
 * Schedules and manages workout and goal reminders using WorkManager.
 *
 * This class handles:
 * - Scheduling periodic workout reminders
 * - Setting up goal deadline notifications
 * - Managing reminder preferences
 * - Ensuring notifications don't become overwhelming
 */
class GoalReminderScheduler(
    private val context: Context,
    private val userPreferences: UserPreferences
) {
    companion object {
        private const val REMINDER_WORK_NAME = "goal_reminder_work"
        private const val MIN_REMINDER_INTERVAL = 6L // hours
        private const val DEFAULT_REMINDER_INTERVAL = 24L // hours
        private const val DEADLINE_REMINDER_ADVANCE = 48L // hours
    }

    /**
     * Schedules a reminder for a specific goal.
     */
    fun scheduleGoalReminder(goal: Goal) {
        val workManager = WorkManager.getInstance(context)

        // Create input data for the worker
        val inputData = workDataOf(
            "goal_id" to goal.id,
            "goal_title" to goal.title,
            "target_value" to goal.targetValue,
            "current_value" to goal.currentValue
        )

        // Calculate reminder timing based on goal deadline and type
        val reminderDelay = calculateReminderDelay(goal)

        // Create work request
        val reminderRequest = OneTimeWorkRequestBuilder<GoalReminderWorker>()
            .setInputData(inputData)
            .setInitialDelay(reminderDelay, TimeUnit.HOURS)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .addTag(goal.id.toString())
            .build()

        // Enqueue unique work to avoid duplicate reminders
        workManager.enqueueUniqueWork(
            "${REMINDER_WORK_NAME}_${goal.id}",
            ExistingWorkPolicy.REPLACE,
            reminderRequest
        )
    }

    /**
     * Schedules periodic workout reminders based on user preferences.
     */
    fun scheduleWorkoutReminders() {
        val workManager = WorkManager.getInstance(context)

        val reminderRequest = PeriodicWorkRequestBuilder<WorkoutReminderWorker>(
            DEFAULT_REMINDER_INTERVAL,
            TimeUnit.HOURS
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            REMINDER_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            reminderRequest
        )
    }

    /**
     * Cancels all reminders for a specific goal.
     */
    fun cancelGoalReminders(goalId: Long) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWorkByTag(goalId.toString())
    }

    /**
     * Cancels all workout reminders.
     */
    fun cancelWorkoutReminders() {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(REMINDER_WORK_NAME)
    }

    /**
     * Calculates appropriate reminder delay based on goal type and deadline.
     */
    private fun calculateReminderDelay(goal: Goal): Long {
        val now = System.currentTimeMillis()

        // If goal has a target date, schedule reminder before target date
        val timeToTarget = goal.targetDate.time - now
        if (timeToTarget > 0) {
            val hoursToTarget = TimeUnit.MILLISECONDS.toHours(timeToTarget)
            if (hoursToTarget <= DEADLINE_REMINDER_ADVANCE) {
                // Goal is close to target date, remind soon
                return MIN_REMINDER_INTERVAL
            }
            // Schedule reminder before target date
            return hoursToTarget - DEADLINE_REMINDER_ADVANCE
        }

        // For goals without target date or past target date
        return when (goal.reminderFrequency) {
            FREQUENCY_DAILY -> DEFAULT_REMINDER_INTERVAL
            FREQUENCY_WEEKLY -> DEFAULT_REMINDER_INTERVAL * 2
            FREQUENCY_MONTHLY -> DEFAULT_REMINDER_INTERVAL * 4
            else -> DEFAULT_REMINDER_INTERVAL
        }
    }

    /**
     * Updates reminder schedules based on user preferences.
     */
    suspend fun updateReminderPreferences() {
        val workManager = WorkManager.getInstance(context)

        // Check if reminders are enabled
        userPreferences.workoutRemindersEnabled.collect { enabled ->
            if (!enabled) {
                workManager.cancelUniqueWork(REMINDER_WORK_NAME)
                return@collect
            }
            // Re-schedule with updated preferences
            scheduleWorkoutReminders()
        }
    }
}
