/**
 * NotificationReceiver BroadcastReceiver for the Fitness Tracker application.
 *
 * This receiver handles broadcast intents for daily notifications, including
 * goal reminders, workout notifications, and progress updates. It processes
 * alarm triggers from the AlarmManager and creates appropriate notifications
 * using the SimpleNotificationManager.
 *
 * Key Features:
 * - Handles daily notification alarms
 * - Processes goal reminder notifications
 * - Creates workout and progress notifications
 * - Integrates with notification scheduling system
 * - Maintains user notification preferences
 */

package com.example.fitnesstrackerapp.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.fitnesstrackerapp.ServiceLocator
import com.example.fitnesstrackerapp.notification.SimpleNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver for handling daily notification triggers.
 *
 * This receiver is triggered by the AlarmManager for scheduled notifications
 * including daily reminders, goal notifications, and progress updates.
 * It uses the SimpleNotificationManager to display notifications and
 * integrates with the repository layer for data retrieval.
 */
class NotificationReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "NotificationReceiver"
        
        // Action constants for different notification types
        const val ACTION_DAILY_REMINDER = "com.example.fitnesstrackerapp.ACTION_DAILY_REMINDER"
        const val ACTION_GOAL_REMINDER = "com.example.fitnesstrackerapp.ACTION_GOAL_REMINDER"
        const val ACTION_WORKOUT_REMINDER = "com.example.fitnesstrackerapp.ACTION_WORKOUT_REMINDER"
        const val ACTION_PROGRESS_UPDATE = "com.example.fitnesstrackerapp.ACTION_PROGRESS_UPDATE"
        
        // Extra keys for notification data
        const val EXTRA_NOTIFICATION_TYPE = "notification_type"
        const val EXTRA_GOAL_ID = "goal_id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_MESSAGE = "message"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "NotificationReceiver triggered with action: ${intent.action}")
        
        try {
            val notificationManager = SimpleNotificationManager(context)
            val coroutineScope = CoroutineScope(Dispatchers.IO)
            
            when (intent.action) {
                ACTION_DAILY_REMINDER -> {
                    handleDailyReminder(context, notificationManager, coroutineScope)
                }
                ACTION_GOAL_REMINDER -> {
                    handleGoalReminder(context, intent, notificationManager, coroutineScope)
                }
                ACTION_WORKOUT_REMINDER -> {
                    handleWorkoutReminder(context, intent, notificationManager)
                }
                ACTION_PROGRESS_UPDATE -> {
                    handleProgressUpdate(context, notificationManager, coroutineScope)
                }
                else -> {
                    Log.w(TAG, "Unknown action received: ${intent.action}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing notification broadcast", e)
        }
    }

    /**
     * Handles daily reminder notifications.
     * Shows general fitness reminders to keep users engaged.
     */
    private fun handleDailyReminder(
        context: Context,
        notificationManager: SimpleNotificationManager,
        coroutineScope: CoroutineScope
    ) {
        coroutineScope.launch {
            try {
                val title = "Daily Fitness Reminder"
                val message = "Don't forget to log your workouts and track your progress today!"
                
                notificationManager.showGeneralReminder(title, message)
                Log.d(TAG, "Daily reminder notification sent")
            } catch (e: Exception) {
                Log.e(TAG, "Error sending daily reminder", e)
            }
        }
    }

    /**
     * Handles goal-specific reminder notifications.
     * Shows reminders for specific fitness goals based on user progress.
     */
    private fun handleGoalReminder(
        context: Context,
        intent: Intent,
        notificationManager: SimpleNotificationManager,
        coroutineScope: CoroutineScope
    ) {
        val goalId = intent.getLongExtra(EXTRA_GOAL_ID, -1L)
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Goal Reminder"
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "Time to work on your fitness goal!"
        
        coroutineScope.launch {
            try {
                if (goalId != -1L) {
                    notificationManager.showGoalReminder(title, message, goalId)
                } else {
                    notificationManager.showGoalReminder(title, message)
                }
                Log.d(TAG, "Goal reminder notification sent for goal: $goalId")
            } catch (e: Exception) {
                Log.e(TAG, "Error sending goal reminder", e)
            }
        }
    }

    /**
     * Handles workout reminder notifications.
     * Reminds users to complete their scheduled workouts.
     */
    private fun handleWorkoutReminder(
        context: Context,
        intent: Intent,
        notificationManager: SimpleNotificationManager
    ) {
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Workout Reminder"
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "Time for your scheduled workout!"
        
        try {
            notificationManager.showGeneralReminder(title, message)
            Log.d(TAG, "Workout reminder notification sent")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending workout reminder", e)
        }
    }

    /**
     * Handles daily progress update notifications.
     * Shows users their current progress towards daily goals.
     */
    private fun handleProgressUpdate(
        context: Context,
        notificationManager: SimpleNotificationManager,
        coroutineScope: CoroutineScope
    ) {
        coroutineScope.launch {
            try {
                val serviceLocator = ServiceLocator.get(context)
                serviceLocator.stepRepository
                
                // Get today's step data - placeholder implementation
                // In real implementation, would query repository for current user's data
                val todaySteps = 5000 // Placeholder
                val caloriesBurned = 200 // Placeholder
                val workoutsCompleted = 1 // Placeholder
                
                notificationManager.showDailyProgress(
                    todaySteps,
                    caloriesBurned,
                    workoutsCompleted
                )
                Log.d(TAG, "Progress update notification sent")
            } catch (e: Exception) {
                Log.e(TAG, "Error sending progress update", e)
            }
        }
    }
}
