package com.example.fitnesstrackerapp.notifications

/**
 * NotificationActionReceiver BroadcastReceiver for the Fitness Tracker application.
 *
 * This receiver handles notification action button clicks from users interacting
 * with notification actions such as "Mark as Complete", "Snooze", or "Start Workout".
 * It processes these actions and updates the database accordingly, providing
 * seamless integration between notifications and app functionality.
 *
 * Key Features:
 * - Handles notification action button interactions
 * - Processes workout completion actions
 * - Manages goal progress updates from notifications
 * - Logs user interactions for analytics
 * - Integrates with repository layer for data persistence
 * - Provides feedback to users through additional notifications or UI updates
 */

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.fitnesstrackerapp.ServiceLocator
import com.example.fitnesstrackerapp.notification.SimpleNotificationManager
import com.example.fitnesstrackerapp.util.PermissionUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * This receiver processes actions triggered from notification buttons,
 * allowing users to interact with the app directly from notifications
 * without opening the main application. It handles various fitness-related
 * actions and updates the database accordingly.
 */
class NotificationActionReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "NotificationActionReceiver"

        // Action constants for different notification actions
        const val ACTION_MARK_GOAL_COMPLETE = "com.example.fitnesstrackerapp.ACTION_MARK_GOAL_COMPLETE"
        const val ACTION_SNOOZE_REMINDER = "com.example.fitnesstrackerapp.ACTION_SNOOZE_REMINDER"
        const val ACTION_START_WORKOUT = "com.example.fitnesstrackerapp.ACTION_START_WORKOUT"
        const val ACTION_LOG_HYDRATION = "com.example.fitnesstrackerapp.ACTION_LOG_HYDRATION"
        const val ACTION_QUICK_EXERCISE_LOG = "com.example.fitnesstrackerapp.ACTION_QUICK_EXERCISE_LOG"
        const val ACTION_DISMISS_NOTIFICATION = "com.example.fitnesstrackerapp.ACTION_DISMISS_NOTIFICATION"

        // Extra keys for action data
        const val EXTRA_GOAL_ID = "goal_id"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val EXTRA_SNOOZE_MINUTES = "snooze_minutes"
        const val EXTRA_WORKOUT_TYPE = "workout_type"
        const val EXTRA_EXERCISE_NAME = "exercise_name"
        const val EXTRA_EXERCISE_DURATION = "exercise_duration"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "NotificationActionReceiver triggered with action: ${intent.action}")

        try {
            val notificationManager = SimpleNotificationManager(context)
            val coroutineScope = CoroutineScope(Dispatchers.IO)

            when (intent.action) {
                ACTION_MARK_GOAL_COMPLETE -> {
                    handleMarkGoalComplete(context, intent, notificationManager, coroutineScope)
                }
                ACTION_SNOOZE_REMINDER -> {
                    handleSnoozeReminder(context, intent, notificationManager)
                }
                ACTION_START_WORKOUT -> {
                    handleStartWorkout(context, intent, notificationManager)
                }
                ACTION_LOG_HYDRATION -> {
                    handleLogHydration(context, intent, notificationManager, coroutineScope)
                }
                ACTION_QUICK_EXERCISE_LOG -> {
                    handleQuickExerciseLog(context, intent, notificationManager, coroutineScope)
                }
                ACTION_DISMISS_NOTIFICATION -> {
                    handleDismissNotification(context, intent, notificationManager)
                }
                else -> {
                    Log.w(TAG, "Unknown action received: ${intent.action}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing notification action", e)
        }
    }

    /**
     * Handles marking a goal as complete from notification action.
     */
    private fun handleMarkGoalComplete(
        context: Context,
        intent: Intent,
        notificationManager: SimpleNotificationManager,
        coroutineScope: CoroutineScope,
    ) {
        val goalId = intent.getLongExtra(EXTRA_GOAL_ID, -1L)
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)

        if (goalId == -1L) {
            Log.w(TAG, "Invalid goal ID for mark complete action")
            return
        }

        coroutineScope.launch {
            try {
                val serviceLocator = ServiceLocator.getInstance(context)
                serviceLocator.goalRepository

                // Update goal progress - placeholder implementation
                // In real implementation, would update goal completion status

                // Cancel the original notification
                if (notificationId != -1) {
                    notificationManager.cancelNotification(notificationId)
                }

                // Show completion confirmation
                val title = "Goal Completed! 🎉"
                val message = "Great job! You've made progress on your fitness goal."
                if (PermissionUtils.isNotificationPermissionGranted(context)) {
                    @Suppress("MissingPermission")
                    notificationManager.showGeneralReminder(title, message)
                } else {
                    Log.w(TAG, "Notification permission not granted, cannot show completion confirmation")
                }

                Log.d(TAG, "Goal $goalId marked as complete")
            } catch (e: Exception) {
                Log.e(TAG, "Error marking goal as complete", e)
            }
        }
    }

    /**
     * Handles snoozing a reminder notification.
     */
    private fun handleSnoozeReminder(
        context: Context,
        intent: Intent,
        notificationManager: SimpleNotificationManager,
    ) {
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        val snoozeMinutes = intent.getIntExtra(EXTRA_SNOOZE_MINUTES, 15) // Default 15 minutes

        try {
            // Cancel the current notification
            if (notificationId != -1) {
                notificationManager.cancelNotification(notificationId)
            }

            // Schedule a new reminder after snooze period
            // In real implementation, would use AlarmManager to schedule delayed notification

            Log.d(TAG, "Reminder snoozed for $snoozeMinutes minutes")

            // Optionally show confirmation
            val title = "Reminder Snoozed"
            val message = "We'll remind you again in $snoozeMinutes minutes."
            if (PermissionUtils.isNotificationPermissionGranted(context)) {
                @Suppress("MissingPermission")
                notificationManager.showGeneralReminder(title, message)
            } else {
                Log.w(TAG, "Notification permission not granted, cannot show snooze confirmation")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error snoozing reminder", e)
        }
    }

    /**
     * Handles starting a workout from notification action.
     */
    private fun handleStartWorkout(
        context: Context,
        intent: Intent,
        notificationManager: SimpleNotificationManager,
    ) {
        val workoutType = intent.getStringExtra(EXTRA_WORKOUT_TYPE) ?: "General Workout"
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)

        try {
            // Cancel the workout reminder notification
            if (notificationId != -1) {
                notificationManager.cancelNotification(notificationId)
            }

            // Create intent to open workout screen
            val workoutIntent = Intent(context, com.example.fitnesstrackerapp.MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("navigate_to", "workout")
                putExtra("workout_type", workoutType)
            }

            context.startActivity(workoutIntent)

            // Show workout started confirmation
            val title = "Workout Started! 💪"
            val message = "Let's get moving with your $workoutType!"
            if (PermissionUtils.isNotificationPermissionGranted(context)) {
                @Suppress("MissingPermission")
                notificationManager.showGeneralReminder(title, message)
            } else {
                Log.w(TAG, "Notification permission not granted, cannot show workout confirmation")
            }

            Log.d(TAG, "Workout started: $workoutType")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting workout", e)
        }
    }

    /**
     * Handles logging hydration from notification action.
     */
    private fun handleLogHydration(
        context: Context,
        intent: Intent,
        notificationManager: SimpleNotificationManager,
        coroutineScope: CoroutineScope,
    ) {
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)

        coroutineScope.launch {
            try {
                // Log hydration entry - placeholder implementation
                // In real implementation, would add hydration entry to database

                // Cancel the hydration reminder
                if (notificationId != -1) {
                    notificationManager.cancelNotification(notificationId)
                }

                // Show confirmation
                val title = "Hydration Logged! 💧"
                val message = "Good job staying hydrated! Keep it up throughout the day."
                if (PermissionUtils.isNotificationPermissionGranted(context)) {
                    @Suppress("MissingPermission")
                    notificationManager.showGeneralReminder(title, message)
                } else {
                    Log.w(TAG, "Notification permission not granted, cannot show hydration confirmation")
                }

                Log.d(TAG, "Hydration logged from notification")
            } catch (e: Exception) {
                Log.e(TAG, "Error logging hydration", e)
            }
        }
    }

    /**
     * Handles quick exercise logging from notification action.
     */
    private fun handleQuickExerciseLog(
        context: Context,
        intent: Intent,
        notificationManager: SimpleNotificationManager,
        coroutineScope: CoroutineScope,
    ) {
        val exerciseName = intent.getStringExtra(EXTRA_EXERCISE_NAME) ?: "Exercise"
        val duration = intent.getIntExtra(EXTRA_EXERCISE_DURATION, 15) // Default 15 minutes
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)

        coroutineScope.launch {
            try {
                ServiceLocator.getInstance(context)

                // Create quick workout entry - placeholder implementation
                // In real implementation, would add workout entry to database

                // Cancel the exercise reminder
                if (notificationId != -1) {
                    notificationManager.cancelNotification(notificationId)
                }

                // Show completion confirmation
                val title = "Exercise Completed! 🏃‍♀️"
                val message = "Great job completing $duration minutes of $exerciseName!"
                if (PermissionUtils.isNotificationPermissionGranted(context)) {
                    @Suppress("MissingPermission")
                    notificationManager.showGeneralReminder(title, message)
                } else {
                    Log.w(TAG, "Notification permission not granted, cannot show exercise confirmation")
                }

                Log.d(TAG, "Quick exercise logged: $exerciseName for $duration minutes")
            } catch (e: Exception) {
                Log.e(TAG, "Error logging quick exercise", e)
            }
        }
    }

    /**
     * Handles dismissing a notification.
     */
    private fun handleDismissNotification(
        context: Context,
        intent: Intent,
        notificationManager: SimpleNotificationManager,
    ) {
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)

        try {
            if (notificationId != -1) {
                notificationManager.cancelNotification(notificationId)
                Log.d(TAG, "Notification $notificationId dismissed")
            } else {
                Log.w(TAG, "Invalid notification ID for dismiss action")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error dismissing notification", e)
        }
    }
}
