package com.example.fitnesstrackerapp.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.fitnesstrackerapp.R

/**
 * NotificationReceiver
 *
 * This BroadcastReceiver is triggered by the AlarmManager to display a motivational
 * daily notification encouraging the user to stay active and pursue their fitness goals.
 */
class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val channelId = "fitness_channel"               // Unique channel ID
        val channelName = "Fitness Notifications"       // Display name in system settings
        val notificationId = 1                          // Constant ID for single daily reminder

        // List of motivational messages (choose one at random)
        val messages = listOf(
            "Keep going! You're doing great! 💪",
            "Don't give up on your goals! 🏃‍♂️",
            "Every step counts! 👣",
            "Stay strong and motivated! 🧠",
            "Make today count! ✅"
        )
        val message = messages.random()

        // Get the system's NotificationManager service
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // For Android 8.0 (API 26) and above, a NotificationChannel is required
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily motivational reminders to help you stay on track with your fitness goals."
            }
            // Register the channel with the system (if not already created)
            notificationManager.createNotificationChannel(channel)
        }

        // Build the notification using NotificationCompat
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with actual icon (e.g., R.drawable.ic_fitness)
            .setContentTitle("Fitness Reminder")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true) // Auto-dismiss on user tap
            .build()

        // Display the notification
        notificationManager.notify(notificationId, notification)
    }
}
