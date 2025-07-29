package com.example.fitnesstrackerapp.notifications

import android.Manifest
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
 * NotificationReceiver
 *
 * This BroadcastReceiver is triggered by AlarmManager or other system events
 * to show a reminder notification to the user.
 */
class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Notification ID to uniquely identify this notification
        val notificationId = 1001

        // Notification channel ID (must match one used during channel creation)
        val channelId = "daily_reminder_channel"

        // Create an intent to open the app when the notification is tapped
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            mainIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Create the notification channel (required for Android 8.0+)
        createNotificationChannel(context)

        // Build the actual notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // App icon
            .setContentTitle("Fitness Reminder")
            .setContentText("Don't forget to log your workout and check your progress!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // Check permission before showing notification
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Show the notification
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        }
    }

    /**
     * Creates a notification channel if running on Android 8.0+.
     * This must be done before sending notifications to avoid errors.
     */
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "daily_reminder_channel"
            val channelName = "Daily Reminders"
            val channelDescription = "Reminds the user to stay active and healthy"
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
