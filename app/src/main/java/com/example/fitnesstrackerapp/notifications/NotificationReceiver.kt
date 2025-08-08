/**
 * Broadcast receiver for handling notification events in the Fitness Tracker App.
 *
 * Receives broadcast intents from AlarmManager and displays appropriate notifications
 * for daily reminders, goal deadlines, and other fitness-related alerts.
 * Implements proper notification channels and handles different notification types.
 */
package com.example.fitnesstrackerapp.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.fitnesstrackerapp.MainActivity
import com.example.fitnesstrackerapp.R

/**
 * BroadcastReceiver that handles various notification events.
 *
 * This receiver processes different types of notification triggers:
 * - Daily workout reminders
 * - Goal deadline notifications
 * - Motivational messages
 * - Progress updates
 *
 * The receiver creates and displays notifications with appropriate content,
 * actions, and styling based on the notification type.
 */
class NotificationReceiver : BroadcastReceiver() {

    companion object {
        // Action constants for different notification types
        const val ACTION_DAILY_REMINDER = "com.example.fitnesstrackerapp.DAILY_REMINDER"
        const val ACTION_GOAL_REMINDER = "com.example.fitnesstrackerapp.GOAL_REMINDER"
        const val ACTION_PROGRESS_UPDATE = "com.example.fitnesstrackerapp.PROGRESS_UPDATE"
        
        // Extra keys for notification data
        const val EXTRA_GOAL_ID = "extra_goal_id"
        const val EXTRA_GOAL_TITLE = "extra_goal_title"
        const val EXTRA_PROGRESS_MESSAGE = "extra_progress_message"
        
        // Notification channel IDs
        private const val CHANNEL_DAILY_REMINDERS = "daily_reminders"
        private const val CHANNEL_GOAL_REMINDERS = "goal_reminders"
        private const val CHANNEL_PROGRESS_UPDATES = "progress_updates"
        
        // Notification IDs
        private const val NOTIFICATION_ID_DAILY = 1001
        private const val NOTIFICATION_ID_GOAL_BASE = 2000
        private const val NOTIFICATION_ID_PROGRESS = 3001
    }

    /**
     * Called when a broadcast intent is received.
     *
     * Processes the intent action and displays the appropriate notification.
     *
     * @param context The context in which the receiver is running.
     * @param intent The intent being received.
     */
    override fun onReceive(context: Context, intent: Intent) {
        try {
            when (intent.action) {
                ACTION_DAILY_REMINDER -> {
                    showDailyReminderNotification(context)
                }
                ACTION_GOAL_REMINDER -> {
                    val goalId = intent.getStringExtra(EXTRA_GOAL_ID) ?: return
                    val goalTitle = intent.getStringExtra(EXTRA_GOAL_TITLE) ?: "Your Goal"
                    showGoalReminderNotification(context, goalId, goalTitle)
                }
                ACTION_PROGRESS_UPDATE -> {
                    val message = intent.getStringExtra(EXTRA_PROGRESS_MESSAGE) ?: "Check your progress!"
                    showProgressUpdateNotification(context, message)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("NotificationReceiver", "Error handling notification", e)
        }
    }

    /**
     * Shows a daily workout reminder notification.
     *
     * @param context The application context.
     */
    @SuppressLint("MissingPermission")
    private fun showDailyReminderNotification(context: Context) {
        createNotificationChannel(
            context,
            CHANNEL_DAILY_REMINDERS,
            "Daily Reminders",
            "Daily workout and fitness reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_DAILY_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Time for Your Workout! 💪")
            .setContentText("Don't forget to log your daily exercise and stay active!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Don't forget to log your daily exercise and stay active! Every step counts towards your fitness goals.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_fitness,
                "Log Workout",
                createWorkoutPendingIntent(context)
            )
            .addAction(
                R.drawable.ic_notification_steps,
                "Check Steps",
                createStepsPendingIntent(context)
            )
            .build()

        if (hasNotificationPermission(context)) {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_DAILY, notification)
        }
    }

    /**
     * Shows a goal reminder notification.
     *
     * @param context The application context.
     * @param goalId The unique identifier of the goal.
     * @param goalTitle The title of the goal.
     */
    @SuppressLint("MissingPermission")
    private fun showGoalReminderNotification(context: Context, goalId: String, goalTitle: String) {
        createNotificationChannel(
            context,
            CHANNEL_GOAL_REMINDERS,
            "Goal Reminders",
            "Reminders for your fitness goals",
            NotificationManager.IMPORTANCE_HIGH
        )

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "goals")
            putExtra("goal_id", goalId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            goalId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_GOAL_REMINDERS)
            .setSmallIcon(R.drawable.ic_fitness_goal)
            .setContentTitle("Goal Reminder: $goalTitle")
            .setContentText("Your goal deadline is approaching. Check your progress!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Your goal '$goalTitle' deadline is approaching. Don't give up now - you're closer than you think!")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_fitness_goal,
                "View Goal",
                pendingIntent
            )
            .build()

        val notificationId = NOTIFICATION_ID_GOAL_BASE + goalId.hashCode()
        if (hasNotificationPermission(context)) {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        }
    }

    /**
     * Shows a progress update notification.
     *
     * @param context The application context.
     * @param message The progress message to display.
     */
    @SuppressLint("MissingPermission")
    private fun showProgressUpdateNotification(context: Context, message: String) {
        createNotificationChannel(
            context,
            CHANNEL_PROGRESS_UPDATES,
            "Progress Updates",
            "Updates on your fitness progress",
            NotificationManager.IMPORTANCE_LOW
        )

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "progress")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_PROGRESS_UPDATES)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Fitness Progress Update")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (hasNotificationPermission(context)) {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_PROGRESS, notification)
        }
    }

    /**
     * Checks if notification permission is granted.
     *
     * @param context The application context.
     * @return true if permission is granted, false otherwise.
     */
    private fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // For older versions, notifications are granted by default
            true
        }
    }

    /**
     * Creates a notification channel for Android O and above.
     *
     * @param context The application context.
     * @param channelId The unique identifier for the channel.
     * @param channelName The user-visible name of the channel.
     * @param channelDescription The user-visible description of the channel.
     * @param importance The importance level of the channel.
     */
    private fun createNotificationChannel(
        context: Context,
        channelId: String,
        channelName: String,
        channelDescription: String,
        importance: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
                enableVibration(true)
                vibrationPattern = longArrayOf(100, 200, 300, 400, 300, 200, 100)
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * Creates a pending intent for navigating to the workout screen.
     *
     * @param context The application context.
     * @return PendingIntent for workout navigation.
     */
    private fun createWorkoutPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "workout")
        }

        return PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Creates a pending intent for navigating to the steps screen.
     *
     * @param context The application context.
     * @return PendingIntent for steps navigation.
     */
    private fun createStepsPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "steps")
        }

        return PendingIntent.getActivity(
            context,
            2,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}