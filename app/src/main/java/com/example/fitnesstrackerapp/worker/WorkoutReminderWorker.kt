/**
 * Simple Workout Reminder Worker for workout notifications.
 *
 * This worker sends notifications to remind users about their scheduled workouts.
 * It uses only standard Android SDK components and the SimpleNotificationManager.
 *
 * @author Fitness Tracker Team
 * @version 1.0.0
 * @since 2024-12-01
 */
package com.example.fitnesstrackerapp.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.fitnesstrackerapp.notification.SimpleNotificationManager

/**
 * Worker for handling workout reminder notifications.
 *
 * This worker sends basic workout reminder notifications using the
 * SimpleNotificationManager to ensure compatibility with standard Android SDK.
 *
 * @param context The application context
 * @param params Worker parameters from WorkManager
 */
class WorkoutReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    private val notificationManager = SimpleNotificationManager(applicationContext)

    companion object {
        private const val TAG = "WorkoutReminderWorker"
    }

    /**
     * Performs the work of sending a workout reminder notification.
     *
     * @return Result indicating success or failure of the work
     */
    override suspend fun doWork(): Result {
        return try {
            // Get workout type from input data or use default
            val workoutType = inputData.getString("workout_type") ?: "General"
            val title = inputData.getString("title") ?: "Workout Time!"
            val message = inputData.getString("message") ?: "Your scheduled workout is ready. Let's get moving!"

            // Send workout reminder notification
            notificationManager.showGeneralReminder(title, message)

            Log.d(TAG, "Workout reminder notification sent successfully")
            Result.success(
                workDataOf(
                    "notification_sent" to true,
                    "workout_type" to workoutType,
                    "timestamp" to System.currentTimeMillis(),
                ),
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send workout reminder notification", e)
            Result.failure(
                workDataOf(
                    "error" to (e.message ?: "Unknown error occurred"),
                    "timestamp" to System.currentTimeMillis(),
                ),
            )
        }
    }
}
