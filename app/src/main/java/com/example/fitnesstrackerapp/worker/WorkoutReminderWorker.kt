/**
 * Simple Workout Reminder Worker for workout notifications.
 *
 * This worker sends notifications to remind users about their scheduled workouts.
 * It uses only standard Android SDK components.
 */
package com.example.fitnesstrackerapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.fitnesstrackerapp.notifications.NotificationManager

/**
 * Worker for handling workout reminder notifications.
 */
class WorkoutReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val notificationManager = NotificationManager(applicationContext)

    companion object {
        private const val TAG = "WorkoutReminderWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            // Send a basic workout reminder notification
            val notificationId = notificationManager.showWorkoutReminder(
                title = "Workout Time!",
                message = "Your scheduled workout is ready. Let's get moving!",
                workoutType = "Cardio"
            )
            
            Result.success(workDataOf("notification_id" to notificationId))
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to send workout reminder", e)
            Result.failure(workDataOf("error" to e.message))
        }
    }
}
