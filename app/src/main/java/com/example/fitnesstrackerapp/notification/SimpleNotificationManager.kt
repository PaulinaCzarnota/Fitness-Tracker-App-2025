/**
 * Simple Notification Manager for Fitness Tracker App.
 *
 * This manager handles all notification functionality for the fitness tracker,
 * including workout reminders, goal achievements, and daily progress updates.
 * It uses only standard Android SDK components without external dependencies.
 *
 * Key Features:
 * - Goal reminder notifications
 * - Workout completion notifications
 * - Daily progress summary notifications
 * - Customizable notification channels
 *
 * This implementation focuses on core notification functionality while
 * maintaining compatibility with Android API levels 21+.
 */

package com.example.fitnesstrackerapp.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.fitnesstrackerapp.MainActivity
import com.example.fitnesstrackerapp.R

/**
 * Simple notification manager for handling fitness-related notifications.
 *
 * Provides methods to create and manage notifications for goals, workouts,
 * and progress updates using standard Android notification APIs.
 *
 * @param context The application context
 */
class SimpleNotificationManager(private val context: Context) {
    companion object {
        private const val TAG = "SimpleNotificationManager"

        // Notification channels
        const val CHANNEL_ID_GOALS = "fitness_goals"
        const val CHANNEL_ID_WORKOUTS = "workouts"
        const val CHANNEL_ID_PROGRESS = "daily_progress"
        const val CHANNEL_ID_REMINDERS = "reminders"

        // Notification IDs
        const val NOTIFICATION_ID_GOAL_REMINDER = 1001
        const val NOTIFICATION_ID_WORKOUT_COMPLETE = 1002
        const val NOTIFICATION_ID_DAILY_PROGRESS = 1003
        const val NOTIFICATION_ID_GENERAL_REMINDER = 1004

        // Request codes for pending intents
        const val REQUEST_CODE_MAIN_ACTIVITY = 2001
        const val REQUEST_CODE_GOAL_DETAIL = 2002
        const val REQUEST_CODE_WORKOUT_DETAIL = 2003
    }

    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    init {
        createNotificationChannels()
    }

    /**
     * Creates notification channels for different types of notifications.
     * This method is safe to call multiple times.
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                // Goals channel
                val goalsChannel = NotificationChannel(
                    CHANNEL_ID_GOALS,
                    context.getString(R.string.notification_channel_goals),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description = context.getString(R.string.notification_channel_goals_description)
                    enableLights(true)
                    enableVibration(true)
                }

                // Workouts channel
                val workoutsChannel = NotificationChannel(
                    CHANNEL_ID_WORKOUTS,
                    context.getString(R.string.notification_channel_workouts),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description = context.getString(R.string.notification_channel_workouts_description)
                    enableLights(true)
                    enableVibration(true)
                }

                // Daily progress channel
                val progressChannel = NotificationChannel(
                    CHANNEL_ID_PROGRESS,
                    context.getString(R.string.notification_channel_progress),
                    NotificationManager.IMPORTANCE_LOW,
                ).apply {
                    description = context.getString(R.string.notification_channel_progress_description)
                    enableLights(false)
                    enableVibration(false)
                }

                // Reminders channel
                val remindersChannel = NotificationChannel(
                    CHANNEL_ID_REMINDERS,
                    context.getString(R.string.notification_channel_reminders),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description = context.getString(R.string.notification_channel_reminders_description)
                    enableLights(true)
                    enableVibration(true)
                }

                // Create all channels
                notificationManager.createNotificationChannels(
                    listOf(goalsChannel, workoutsChannel, progressChannel, remindersChannel),
                )

                Log.d(TAG, "Notification channels created successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error creating notification channels", e)
            }
        }
    }

    /**
     * Shows a goal reminder notification.
     *
     * @param title The notification title
     * @param message The notification message
     * @param goalId Optional goal ID for deep linking
     */
    fun showGoalReminder(title: String, message: String, goalId: Long? = null) {
        try {
            val intent = createMainActivityIntent(goalId)
            val pendingIntent = createPendingIntent(intent, REQUEST_CODE_GOAL_DETAIL)

            val notification = NotificationCompat.Builder(context, CHANNEL_ID_GOALS)
                .setSmallIcon(R.drawable.ic_fitness_goal)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            if (areNotificationsEnabled()) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS,
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                NotificationManagerCompat.from(context)
                    .notify(NOTIFICATION_ID_GOAL_REMINDER, notification)
                Log.d(TAG, "Goal reminder notification shown: $title")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing goal reminder notification", e)
        }
    }

    /**
     * Shows a workout completion notification.
     *
     * @param workoutName The name of the completed workout
     * @param duration The workout duration in minutes
     * @param caloriesBurned The estimated calories burned
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showWorkoutComplete(workoutName: String, duration: Int, caloriesBurned: Int) {
        try {
            val title = context.getString(R.string.notification_workout_complete_title)
            val message = context.getString(
                R.string.notification_workout_complete_message,
                workoutName,
                duration,
                caloriesBurned,
            )

            val intent = createMainActivityIntent()
            val pendingIntent = createPendingIntent(intent, REQUEST_CODE_WORKOUT_DETAIL)

            val notification = NotificationCompat.Builder(context, CHANNEL_ID_WORKOUTS)
                .setSmallIcon(R.drawable.ic_workout_complete)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            if (areNotificationsEnabled()) {
                NotificationManagerCompat.from(context)
                    .notify(NOTIFICATION_ID_WORKOUT_COMPLETE, notification)
                Log.d(TAG, "Workout complete notification shown: $workoutName")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing workout complete notification", e)
        }
    }

    /**
     * Shows a daily progress summary notification.
     *
     * @param stepsCompleted Number of steps completed
     * @param caloriesBurned Total calories burned
     * @param workoutsCompleted Number of workouts completed
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showDailyProgress(stepsCompleted: Int, caloriesBurned: Int, workoutsCompleted: Int) {
        try {
            val title = context.getString(R.string.notification_daily_progress_title)
            val message = context.getString(
                R.string.notification_daily_progress_message,
                stepsCompleted,
                caloriesBurned,
                workoutsCompleted,
            )

            val intent = createMainActivityIntent()
            val pendingIntent = createPendingIntent(intent, REQUEST_CODE_MAIN_ACTIVITY)

            val notification = NotificationCompat.Builder(context, CHANNEL_ID_PROGRESS)
                .setSmallIcon(R.drawable.ic_daily_progress)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setOngoing(false)
                .build()

            if (areNotificationsEnabled()) {
                NotificationManagerCompat.from(context)
                    .notify(NOTIFICATION_ID_DAILY_PROGRESS, notification)
                Log.d(TAG, "Daily progress notification shown")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing daily progress notification", e)
        }
    }

    /**
     * Shows a general fitness reminder notification.
     *
     * @param title The notification title
     * @param message The notification message
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showGeneralReminder(title: String, message: String) {
        try {
            val intent = createMainActivityIntent()
            val pendingIntent = createPendingIntent(intent, REQUEST_CODE_MAIN_ACTIVITY)

            val notification = NotificationCompat.Builder(context, CHANNEL_ID_REMINDERS)
                .setSmallIcon(R.drawable.ic_reminder)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            if (areNotificationsEnabled()) {
                NotificationManagerCompat.from(context)
                    .notify(NOTIFICATION_ID_GENERAL_REMINDER, notification)
                Log.d(TAG, "General reminder notification shown: $title")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing general reminder notification", e)
        }
    }

    /**
     * Cancels a specific notification by ID.
     *
     * @param notificationId The ID of the notification to cancel
     */
    fun cancelNotification(notificationId: Int) {
        try {
            NotificationManagerCompat.from(context).cancel(notificationId)
            Log.d(TAG, "Notification cancelled: $notificationId")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling notification: $notificationId", e)
        }
    }

    /**
     * Cancels all notifications from this app.
     */
    fun cancelAllNotifications() {
        try {
            NotificationManagerCompat.from(context).cancelAll()
            Log.d(TAG, "All notifications cancelled")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling all notifications", e)
        }
    }

    /**
     * Checks if notifications are enabled for the app.
     *
     * @return True if notifications are enabled, false otherwise
     */
    fun areNotificationsEnabled(): Boolean {
        return try {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking notification permissions", e)
            false
        }
    }

    /**
     * Creates an intent to open the main activity.
     *
     * @param goalId Optional goal ID for deep linking
     * @return The created intent
     */
    private fun createMainActivityIntent(goalId: Long? = null): Intent {
        return Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            goalId?.let { putExtra("goal_id", it) }
        }
    }

    /**
     * Creates a pending intent for notification actions.
     *
     * @param intent The intent to wrap
     * @param requestCode The request code for the pending intent
     * @return The created pending intent
     */
    private fun createPendingIntent(intent: Intent, requestCode: Int): PendingIntent {
        val flags =
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        return PendingIntent.getActivity(context, requestCode, intent, flags)
    }

    /**
     * Shows a motivational notification based on user progress.
     *
     * @param progressPercentage The user's current progress percentage (0-100)
     * @param goalType The type of goal (e.g., "steps", "calories", "workouts")
     */
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showMotivationalNotification(progressPercentage: Int, goalType: String) {
        try {
            val (title, message) = when {
                progressPercentage >= 100 -> {
                    Pair(
                        context.getString(R.string.notification_goal_achieved_title),
                        context.getString(R.string.notification_goal_achieved_message, goalType),
                    )
                }
                progressPercentage >= 75 -> {
                    Pair(
                        context.getString(R.string.notification_almost_there_title),
                        context.getString(R.string.notification_almost_there_message, goalType),
                    )
                }
                progressPercentage >= 50 -> {
                    Pair(
                        context.getString(R.string.notification_halfway_title),
                        context.getString(R.string.notification_halfway_message, goalType),
                    )
                }
                else -> {
                    Pair(
                        context.getString(R.string.notification_keep_going_title),
                        context.getString(R.string.notification_keep_going_message, goalType),
                    )
                }
            }

            showGeneralReminder(title, message)
        } catch (e: Exception) {
            Log.e(TAG, "Error showing motivational notification", e)
        }
    }
}
