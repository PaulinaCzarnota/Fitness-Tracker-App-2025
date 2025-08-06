/**
 * Boot receiver for the Fitness Tracker App.
 *
 * Handles device boot completion events and re-schedules notifications that were
 * cleared when the device was restarted. Ensures that daily reminders and goal
 * notifications continue working after device reboots.
 */
package com.example.fitnesstrackerapp.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * BroadcastReceiver that handles device boot completion.
 *
 * When the device boots up, all scheduled alarms and notifications are cleared
 * by the system. This receiver detects the boot completion and re-schedules
 * all necessary notifications to ensure the app continues to function properly.
 *
 * The receiver is automatically triggered when the device finishes booting
 * and the RECEIVE_BOOT_COMPLETED permission is granted.
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    /**
     * Called when the device boot is completed.
     *
     * Re-schedules all notifications that were cleared during the reboot process.
     * This includes daily workout reminders and goal deadline notifications.
     *
     * @param context The context in which the receiver is running.
     * @param intent The intent being received (should be ACTION_BOOT_COMPLETED).
     */
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            try {
                Log.d(TAG, "Device boot completed, re-scheduling notifications")
                
                val notificationScheduler = NotificationScheduler(context)
                
                // Re-schedule daily workout reminders
                notificationScheduler.scheduleDailyReminder()
                
                // Re-schedule goal reminders
                notificationScheduler.scheduleGoalReminders()
                
                Log.d(TAG, "Notifications successfully re-scheduled after boot")
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to re-schedule notifications after boot", e)
            }
        }
    }
}
