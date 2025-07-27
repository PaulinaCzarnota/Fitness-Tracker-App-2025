package com.example.fitnesstrackerapp.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.*

/**
 * NotificationUtils
 *
 * Utility object responsible for scheduling daily reminder notifications
 * using Android's AlarmManager. These reminders help users stay consistent
 * with their fitness routines.
 */
object NotificationUtils {

    /**
     * Schedules a repeating alarm that triggers a notification every day
     * at the specified hour and minute. Default is 9:00 AM.
     *
     * @param context The context used to access the AlarmManager
     * @param hour The hour of day (24-hour format) for the reminder
     * @param minute The minute of the hour for the reminder
     */
    fun scheduleDailyReminder(context: Context, hour: Int = 9, minute: Int = 0) {
        // Get the AlarmManager system service
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Intent targeting NotificationReceiver (triggered by the alarm)
        val intent = Intent(context, NotificationReceiver::class.java)

        // Create a PendingIntent that wraps the intent for broadcasting
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0, // Unique request code
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Set the time the alarm should first go off
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If it's already past the target time today, schedule for tomorrow
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        // Set a repeating alarm (daily) that will fire at the calculated time
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,              // Wake up device if asleep
            calendar.timeInMillis,                // First trigger time
            AlarmManager.INTERVAL_DAY,            // Interval: 24 hours
            pendingIntent                         // Operation to execute
        )
    }
}
