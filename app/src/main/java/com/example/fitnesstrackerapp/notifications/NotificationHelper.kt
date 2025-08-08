package com.example.fitnesstrackerapp.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.fitnesstrackerapp.R

class NotificationHelper(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val workoutChannel = NotificationChannel(
                CHANNEL_WORKOUT,
                "Workout Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val goalChannel = NotificationChannel(
                CHANNEL_GOAL,
                "Goal Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(workoutChannel)
            notificationManager.createNotificationChannel(goalChannel)
        }
    }

    fun showWorkoutReminder() {
        val notification = NotificationCompat.Builder(context, CHANNEL_WORKOUT)
            .setContentTitle("Time for a Workout!")
            .setContentText("Stay on track with your fitness goals")
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(WORKOUT_NOTIFICATION_ID, notification)
    }

    fun showGoalReminder(goalTitle: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_GOAL)
            .setContentTitle("Goal Reminder")
            .setContentText("Don't forget about your goal: $goalTitle")
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(GOAL_NOTIFICATION_ID, notification)
    }

    /**
     * Sends a daily reminder notification to encourage user activity.
     */
    fun sendDailyReminder() {
        val notification = NotificationCompat.Builder(context, CHANNEL_WORKOUT)
            .setContentTitle("Daily Activity Reminder")
            .setContentText("Start your day with some physical activity!")
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(DAILY_NOTIFICATION_ID, notification)
    }

    /**
     * Sends a goal reminder notification for a specific goal.
     */
    fun sendGoalReminder(goalTitle: String = "Your Fitness Goal") {
        showGoalReminder(goalTitle)
    }

    /**
     * Sends a workout reminder notification.
     */
    fun sendWorkoutReminder() {
        showWorkoutReminder()
    }

    /**
     * Shows a step goal achievement notification.
     */
    fun showStepGoalAchievement(stepCount: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_GOAL)
            .setContentTitle("Step Goal Achieved!")
            .setContentText("Congratulations! You've reached $stepCount steps today.")
            .setSmallIcon(R.drawable.ic_steps)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(STEP_GOAL_NOTIFICATION_ID, notification)
    }

    /**
     * Shows a motivational notification.
     */
    fun showMotivationalNotification(message: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_WORKOUT)
            .setContentTitle("Stay Motivated!")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_motivation)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(MOTIVATIONAL_NOTIFICATION_ID, notification)
    }

    companion object {
        const val CHANNEL_WORKOUT = "workout_reminders"
        const val CHANNEL_GOAL = "goal_reminders"
        const val WORKOUT_NOTIFICATION_ID = 1
        const val GOAL_NOTIFICATION_ID = 2
        const val DAILY_NOTIFICATION_ID = 3
        const val STEP_GOAL_NOTIFICATION_ID = 4
        const val MOTIVATIONAL_NOTIFICATION_ID = 5
    }
}
