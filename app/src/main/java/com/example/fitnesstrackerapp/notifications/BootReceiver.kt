package com.example.fitnesstrackerapp.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * BootReceiver is a BroadcastReceiver that listens for device reboot events.
 * It ensures that any alarms or scheduled tasks (such as daily notifications)
 * are re-initialized when the device restarts.
 */
class BootReceiver : BroadcastReceiver() {

    /**
     * This method is called when a broadcast matching the receiver's filter is received.
     * In this case, we listen for BOOT_COMPLETED to restore any lost alarms after reboot.
     *
     * @param context The application context used to reschedule alarms.
     * @param intent The broadcast intent containing the action BOOT_COMPLETED.
     */
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "BOOT_COMPLETED received: Rescheduling daily notifications.")

            // Re-initialize scheduled notifications or alarms after reboot
            NotificationUtils.scheduleDailyReminder(context)

        } else {
            // Log any unexpected broadcasts received
            Log.w("BootReceiver", "Unexpected intent received: ${intent?.action}")
        }
    }
}
