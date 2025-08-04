/**
 * Worker for showing goal reminder notifications in the Fitness Tracker App.
 *
 * Handles background work for showing goal reminder notifications using WorkManager and integrates with NotificationHelper for notification display.
 */
 
package com.example.fitnesstrackerapp.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.fitnesstrackerapp.util.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker class for executing background goal reminder notifications.
 *
 * Executes in the background to show goal reminders, handles notification display logic, and ensures reliable delivery of reminders.
 */
@HiltWorker
class GoalReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, workerParams) {

    /**
     * Performs the background work of showing the goal reminder notification.
     *
     * Retrieves goal details from input data, shows the notification using NotificationHelper, and returns the result status.
     *
     * @return Result indicating success or failure of the operation.
     */
    override suspend fun doWork(): Result {
        // Extract required data from input parameters
        val goalTitle = inputData.getString(KEY_GOAL_TITLE) ?: return Result.failure()
        val goalId = inputData.getString(KEY_GOAL_ID) ?: return Result.failure()

        // Show the notification
        notificationHelper.showGoalReminderNotification(goalTitle, goalId)

        return Result.success()
    }

    companion object {
        /**
         * Key for retrieving goal title from input data
         */
        const val KEY_GOAL_TITLE = "KEY_GOAL_TITLE"
        
        /**
         * Key for retrieving goal ID from input data
         */
        const val KEY_GOAL_ID = "KEY_GOAL_ID"
    }
}
