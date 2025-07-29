package com.example.fitnesstrackerapp.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.*

/**
 * NotificationUtils
 *
 * Utility object to schedule a daily fitness reminder using AlarmManager.
 * Triggers [NotificationReceiver] every day at a specified time.
 */
object NotificationUtils {

    /**
     * scheduleDailyReminder
     *
     * Sets up a daily alarm using AlarmManager that fires a notification each day.
     *
     * @param context Application context used to access system services.
     * @param hour The hour (24-hour format) to trigger the alarm. Default = 9.
     * @param minute The minute of the hour to trigger the alarm. Default = 0.
     */
    fun scheduleDailyReminder(context: Context, hour: Int = 9, minute: Int = 0) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        if (alarmManager == null) {
            Log.e("NotificationUtils", "AlarmManager service unavailable.")
            return
        }

        val intent = Intent(context, NotificationReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )

        Log.d("NotificationUtils", "Daily reminder scheduled at $hour:$minute")
    }
}
