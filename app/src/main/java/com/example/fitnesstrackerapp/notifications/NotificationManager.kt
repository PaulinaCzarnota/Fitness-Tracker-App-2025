/**
 * Notification Manager for Fitness Tracker Application
 *
 * Responsibilities:
 * - Manages all app notifications including workout reminders and goal updates
 * - Handles notification channels and categories
 * - Provides motivational messages and tips
 * - Creates and schedules various types of fitness-related notifications
 */
package com.example.fitnesstrackerapp.notifications

import android.app.NotificationChannel
import android.app.NotificationManager as AndroidNotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.fitnesstrackerapp.MainActivity
import com.example.fitnesstrackerapp.R

/**
 * Manages all notification-related functionality for the fitness tracker app.
 */
class NotificationManager(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager

    companion object {
        const val CHANNEL_WORKOUTS = "workout_reminders"
        const val CHANNEL_GOALS = "goal_updates"
        const val CHANNEL_MOTIVATION = "motivational_tips"
        const val CHANNEL_STEPS = "step_counter"

        const val NOTIFICATION_WORKOUT_REMINDER = 1001
        const val NOTIFICATION_GOAL_ACHIEVEMENT = 1002
        const val NOTIFICATION_DAILY_MOTIVATION = 1003
        const val NOTIFICATION_STEP_MILESTONE = 1004
    }

    /**
     * Creates notification channels for different types of notifications.
     * Required for Android 8.0 (API level 26) and above.
     */
    fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_WORKOUTS,
                    "Workout Reminders",
                    AndroidNotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Reminds you about scheduled workouts"
                },
                NotificationChannel(
                    CHANNEL_GOALS,
                    "Goal Updates",
                    AndroidNotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Updates about your fitness goals"
                },
                NotificationChannel(
                    CHANNEL_MOTIVATION,
                    "Motivational Tips",
                    AndroidNotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Daily motivational messages and fitness tips"
                },
                NotificationChannel(
                    CHANNEL_STEPS,
                    "Step Counter",
                    AndroidNotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Step counting notifications"
                }
            )

            channels.forEach { channel ->
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    /**
     * Shows a workout reminder notification.
     */
    fun showWorkoutReminder(workoutType: String, scheduledTime: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_WORKOUTS)
            .setSmallIcon(R.drawable.ic_fitness)
            .setContentTitle("Workout Reminder")
            .setContentText("Time for your $workoutType workout at $scheduledTime!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_WORKOUT_REMINDER, notification)
    }

    /**
     * Shows a goal achievement notification.
     */
    fun showGoalAchievement(goalTitle: String) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_GOALS)
            .setSmallIcon(R.drawable.ic_trophy)
            .setContentTitle("Goal Achieved! 🎉")
            .setContentText("Congratulations! You've achieved your goal: $goalTitle")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_GOAL_ACHIEVEMENT, notification)
    }

    /**
     * Shows a daily motivational message.
     */
    fun showDailyMotivation(message: String) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_MOTIVATION)
            .setSmallIcon(R.drawable.ic_motivation)
            .setContentTitle("Daily Motivation")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_DAILY_MOTIVATION, notification)
    }

    /**
     * Shows a step milestone notification.
     */
    fun showStepMilestone(steps: Int, milestone: Int) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_STEPS)
            .setSmallIcon(R.drawable.ic_steps)
            .setContentTitle("Step Milestone Reached!")
            .setContentText("Great job! You've reached $milestone steps today. Current: $steps")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_STEP_MILESTONE, notification)
    }

    /**
     * Cancels a specific notification.
     */
    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    /**
     * Cancels all notifications.
     */
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }
}
