package com.example.fitnesstrackerapp.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.fitnesstrackerapp.R

/**
 * Helper class for managing notifications
 */
class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID_GENERAL = "general_notifications"
        const val CHANNEL_ID_WORKOUTS = "workout_reminders"
        const val CHANNEL_ID_GOALS = "goal_reminders"
        const val CHANNEL_ID_STEPS = "step_tracking"
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_ID_GENERAL,
                    "General Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
                ),
                NotificationChannel(
                    CHANNEL_ID_WORKOUTS,
                    "Workout Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ),
                NotificationChannel(
                    CHANNEL_ID_GOALS,
                    "Goal Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
                ),
                NotificationChannel(
                    CHANNEL_ID_STEPS,
                    "Step Tracking",
                    NotificationManager.IMPORTANCE_LOW
                )
            )

            channels.forEach { channel ->
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    fun showWorkoutReminder(title: String, message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_WORKOUTS)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }

    fun showGoalReminder(title: String, message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_GOALS)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1002, notification)
    }

    fun showStepProgress(steps: Int, goal: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_STEPS)
            .setContentTitle("Step Progress")
            .setContentText("$steps / $goal steps today")
            .setSmallIcon(R.drawable.ic_steps)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        notificationManager.notify(1003, notification)
    }
}
