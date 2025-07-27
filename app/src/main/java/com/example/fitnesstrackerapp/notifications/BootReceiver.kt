package com.example.fitnesstrackerapp.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * BootReceiver
 *
 * This BroadcastReceiver is triggered when the device finishes booting.
 * It is used to re-schedule any alarms or notifications that were set before reboot.
 */
class BootReceiver : BroadcastReceiver() {

    /**
     * Called when the system broadcasts BOOT_COMPLETED.
     * Reschedules the daily reminder notification.
     *
     * @param context The application context
     * @param intent The received intent (should be BOOT_COMPLETED)
     */
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device rebooted, rescheduling reminders...")

            // Reschedule daily workout reminder
            NotificationUtils.scheduleDailyReminder(context)
        }
    }
}
