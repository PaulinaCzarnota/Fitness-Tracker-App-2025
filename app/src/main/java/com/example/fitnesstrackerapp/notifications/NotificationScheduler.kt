/**
 * Notification scheduler for the Fitness Tracker App.
 *
 * Handles scheduling of daily reminders, workout notifications, and goal reminders.
 * Uses AlarmManager for precise timing and WorkManager for background processing.
 * Implements proper notification channels and handles Android version compatibility.
 */

package com.example.fitnesstrackerapp.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.fitnesstrackerapp.worker.GoalReminderWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Manages scheduling of various types of notifications for the fitness app.
 *
 * This class provides methods to schedule different types of notifications:
 * - Daily workout reminders
 * - Goal deadline reminders
 * - Motivational messages
 * - Progress updates
 *
 * @property context The application context for accessing system services.
 *
 * @constructor Creates a new NotificationScheduler instance.
 * @param context The application context.
 */
class NotificationScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val workManager = WorkManager.getInstance(context)

    companion object {
        private const val DAILY_REMINDER_REQUEST_CODE = 1001
        private const val GOAL_REMINDER_WORK_NAME = "goal_reminder_work"
        
        // Default notification times
        private const val DEFAULT_REMINDER_HOUR = 9  // 9 AM
        private const val DEFAULT_REMINDER_MINUTE = 0
    }

    /**
     * Schedules a daily workout reminder notification.
     *
     * Sets up a repeating alarm that triggers every day at the specified time.
     * The notification will remind users to log their workouts and stay active.
     *
     * @param hour The hour of day (0-23) when the reminder should trigger. Defaults to 9 AM.
     * @param minute The minute of hour (0-59) when the reminder should trigger. Defaults to 0.
     */
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    fun scheduleDailyReminder(
        hour: Int = DEFAULT_REMINDER_HOUR,
        minute: Int = DEFAULT_REMINDER_MINUTE
    ) {
        try {
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = NotificationReceiver.ACTION_DAILY_REMINDER
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                DAILY_REMINDER_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Calculate the time for the next notification
            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                // If the time has already passed today, schedule for tomorrow
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }

            // Schedule the repeating alarm
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )

            logDebug("Daily reminder scheduled for ${calendar.time}")
        } catch (e: Exception) {
            logError("Failed to schedule daily reminder", e)
        }
    }

    /**
     * Schedules goal reminder notifications using WorkManager.
     *
     * Sets up periodic work to check for goals approaching their deadlines
     * and sends appropriate reminder notifications.
     *
     * @param intervalHours How often to check for goal reminders (in hours). Defaults to 24 hours.
     */
    fun scheduleGoalReminders(intervalHours: Long = 24) {
        try {
            val goalReminderWork = PeriodicWorkRequestBuilder<GoalReminderWorker>(
                intervalHours, TimeUnit.HOURS
            )
                .setInitialDelay(1, TimeUnit.HOURS) // Start checking after 1 hour
                .build()

            workManager.enqueueUniquePeriodicWork(
                GOAL_REMINDER_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                goalReminderWork
            )

            logDebug("Goal reminders scheduled with $intervalHours hour interval")
        } catch (e: Exception) {
            logError("Failed to schedule goal reminders", e)
        }
    }

    /**
     * Schedules a one-time notification for a specific goal deadline.
     *
     * @param goalId The unique identifier of the goal.
     * @param goalTitle The title of the goal.
     * @param deadlineMillis The deadline timestamp in milliseconds.
     * @param reminderOffsetHours How many hours before the deadline to send the reminder.
     */
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    fun scheduleGoalDeadlineReminder(
        goalId: String,
        goalTitle: String,
        deadlineMillis: Long,
        reminderOffsetHours: Int = 24
    ) {
        try {
            val reminderTime = deadlineMillis - (reminderOffsetHours * 60 * 60 * 1000)
            
            // Only schedule if the reminder time is in the future
            if (reminderTime > System.currentTimeMillis()) {
                val intent = Intent(context, NotificationReceiver::class.java).apply {
                    action = NotificationReceiver.ACTION_GOAL_REMINDER
                    putExtra(NotificationReceiver.EXTRA_GOAL_ID, goalId)
                    putExtra(NotificationReceiver.EXTRA_GOAL_TITLE, goalTitle)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    goalId.hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
                )

                logDebug("Goal deadline reminder scheduled for goal: $goalTitle")
            }
        } catch (e: Exception) {
            logError("Failed to schedule goal deadline reminder", e)
        }
    }

    /**
     * Cancels the daily workout reminder.
     */
    fun cancelDailyReminder() {
        try {
            val intent = Intent(context, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                DAILY_REMINDER_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.cancel(pendingIntent)
            logDebug("Daily reminder cancelled")
        } catch (e: Exception) {
            logError("Failed to cancel daily reminder", e)
        }
    }

    /**
     * Cancels all goal reminders.
     */
    fun cancelGoalReminders() {
        try {
            workManager.cancelUniqueWork(GOAL_REMINDER_WORK_NAME)
            logDebug("Goal reminders cancelled")
        } catch (e: Exception) {
            logError("Failed to cancel goal reminders", e)
        }
    }

    /**
     * Cancels a specific goal deadline reminder.
     *
     * @param goalId The unique identifier of the goal.
     */
    fun cancelGoalDeadlineReminder(goalId: String) {
        try {
            val intent = Intent(context, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                goalId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.cancel(pendingIntent)
            logDebug("Goal deadline reminder cancelled for goal: $goalId")
        } catch (e: Exception) {
            logError("Failed to cancel goal deadline reminder", e)
        }
    }

    /**
     * Cancels all scheduled notifications.
     */
    fun cancelAllNotifications() {
        cancelDailyReminder()
        cancelGoalReminders()
        logDebug("All notifications cancelled")
    }

    // region Logging Methods

    /**
     * Logs debug messages for notification scheduling.
     *
     * @param message The debug message to log.
     */
    private fun logDebug(message: String) {
        android.util.Log.d("NotificationScheduler", message)
    }

    /**
     * Logs error messages for notification scheduling.
     *
     * @param message The error message to log.
     * @param throwable Optional throwable for additional error details.
     */
    private fun logError(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            android.util.Log.e("NotificationScheduler", message, throwable)
        } else {
            android.util.Log.e("NotificationScheduler", message)
        }
    }

    // endregion
}
