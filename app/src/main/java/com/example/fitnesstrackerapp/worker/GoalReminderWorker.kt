/**
 * Simple Goal Reminder Worker for goal deadline notifications.
 *
 * This worker sends notifications to remind users about their fitness goals.
 * It uses only standard Android SDK components and the SimpleNotificationManager.
 *
 * Key Features:
 * - Sends goal reminder notifications
 * - Supports custom goal messages
 * - Uses standard WorkManager framework
 * - Error handling and logging
 */
package com.example.fitnesstrackerapp.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.fitnesstrackerapp.notification.SimpleNotificationManager

/**
 * Worker for handling goal reminder notifications.
 *
 * This worker is responsible for sending periodic goal reminder notifications
 * to help users stay on track with their fitness objectives.
 *
 * @param context The application context
 * @param params Worker parameters from WorkManager
 */
class GoalReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    private val notificationManager = SimpleNotificationManager(applicationContext)

    companion object {
        private const val TAG = "GoalReminderWorker"

        // Input data keys
        const val KEY_GOAL_TITLE = "goal_title"
        const val KEY_GOAL_MESSAGE = "goal_message"
        const val KEY_GOAL_ID = "goal_id"
        const val KEY_PROGRESS_PERCENTAGE = "progress_percentage"
    }

    /**
     * Performs the work of sending a goal reminder notification.
     *
     * This method extracts goal information from the input data and
     * sends an appropriate notification to remind the user about their goal.
     *
     * @return Result indicating success or failure of the work
     */
    override suspend fun doWork(): Result {
        return try {
            // Extract goal information from input data
            val goalTitle = inputData.getString(KEY_GOAL_TITLE) ?: "Daily Fitness Goal"
            val goalMessage = inputData.getString(KEY_GOAL_MESSAGE)
                ?: "Don't forget to work towards your fitness goal today!"
            val goalId = inputData.getLong(KEY_GOAL_ID, -1L)
            val progressPercentage = inputData.getInt(KEY_PROGRESS_PERCENTAGE, 0)

            // Create appropriate notification title and message based on progress
            val (title, message) = createNotificationContent(goalTitle, goalMessage, progressPercentage)

            // Send goal reminder notification
            notificationManager.showGoalReminder(
                title = title,
                message = message,
                goalId = if (goalId != -1L) goalId else null,
            )

            Log.d(TAG, "Goal reminder notification sent successfully for: $goalTitle")
            Result.success(
                workDataOf(
                    "notification_sent" to true,
                    "goal_title" to goalTitle,
                    "progress_percentage" to progressPercentage,
                    "timestamp" to System.currentTimeMillis(),
                ),
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send goal reminder notification", e)
            Result.failure(
                workDataOf(
                    "error" to (e.message ?: "Unknown error occurred"),
                    "timestamp" to System.currentTimeMillis(),
                ),
            )
        }
    }

    /**
     * Creates appropriate notification content based on goal progress.
     *
     * @param goalTitle The title of the goal
     * @param defaultMessage Default message if no custom message is provided
     * @param progressPercentage Current progress percentage (0-100)
     * @return Pair of (title, message) for the notification
     */
    private fun createNotificationContent(
        goalTitle: String,
        defaultMessage: String,
        progressPercentage: Int,
    ): Pair<String, String> {
        return when {
            progressPercentage >= 90 -> {
                Pair(
                    "🎯 Almost There!",
                    "You're $progressPercentage% of the way to completing '$goalTitle'. Just a little more!",
                )
            }
            progressPercentage >= 75 -> {
                Pair(
                    "🔥 Great Progress!",
                    "You've made it $progressPercentage% of the way to '$goalTitle'. Keep it up!",
                )
            }
            progressPercentage >= 50 -> {
                Pair(
                    "💪 Halfway There!",
                    "You're $progressPercentage% done with '$goalTitle'. Don't stop now!",
                )
            }
            progressPercentage > 0 -> {
                Pair(
                    "🚀 Keep Going!",
                    "You've started '$goalTitle' and are $progressPercentage% complete. Every step counts!",
                )
            }
            else -> {
                Pair(
                    "⏰ Goal Reminder",
                    "Time to work on '$goalTitle'. $defaultMessage",
                )
            }
        }
    }
}
