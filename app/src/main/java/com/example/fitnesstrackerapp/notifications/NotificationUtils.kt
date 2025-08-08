/**
 * Utility object for scheduling daily fitness reminders in the Fitness Tracker App.
 *
 * Schedules a daily fitness reminder using AlarmManager and triggers NotificationReceiver every day at a specified time.
 */

package com.example.fitnesstrackerapp.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.Calendar

object NotificationUtils {
    /**
     * Schedules a daily alarm using AlarmManager to fire a notification each day.
     *
     * @param context Application context used to access system services.
     * @param hour The hour (24-hour format) to trigger the alarm. Default = 9.
     * @param minute The minute of the hour to trigger the alarm. Default = 0.
     */
    fun scheduleDailyReminder(context: Context, hour: Int = 9, minute: Int = 0) {
        // Get the AlarmManager system service
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        if (alarmManager == null) {
            Log.e("com.example.fitnesstrackerapp.notifications.NotificationUtils", "AlarmManager service unavailable.")
            return
        }

        // Intent to trigger NotificationReceiver when the alarm fires
        val intent = Intent(context, NotificationReceiver::class.java)

        // PendingIntent that wraps the intent for AlarmManager
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Set up the calendar to the next scheduled time (today or tomorrow)
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If the scheduled time has already passed today, schedule for tomorrow
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        // Schedule a repeating alarm to trigger the notification every day
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )

        Log.d("com.example.fitnesstrackerapp.notifications.NotificationUtils", "Daily reminder scheduled at $hour:$minute")
    }
}
