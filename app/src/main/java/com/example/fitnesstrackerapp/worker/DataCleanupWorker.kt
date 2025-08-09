/**
 * Background worker for periodic data cleanup operations.
 *
 * This worker is responsible for:
 * - Cleaning up old notification logs
 * - Removing expired temporary files
 * - Optimizing database performance
 */
package com.example.fitnesstrackerapp.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.fitnesstrackerapp.ServiceLocator

/**
 * Worker that performs periodic data cleanup operations.
 */
class DataCleanupWorker(
    context: Context,
    parameters: WorkerParameters,
) : CoroutineWorker(context, parameters) {

    companion object {
        const val TAG = "DataCleanupWorker"
        const val KEY_RETENTION_DAYS = "retention_days"
        const val DEFAULT_RETENTION_DAYS = 90
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Starting data cleanup operation")

            val retentionDays = inputData.getInt(KEY_RETENTION_DAYS, DEFAULT_RETENTION_DAYS)
            val notificationRepository = ServiceLocator.get(applicationContext).notificationRepository

            // Clean up old notification logs
            val deletedCount = notificationRepository.cleanupOldLogs(retentionDays)
            Log.d(TAG, "Cleaned up $deletedCount old notification logs (older than $retentionDays days)")

            // TODO: Add other cleanup operations here
            // - Clean up temporary files
            // - Optimize database (VACUUM)
            // - Remove old cached data

            Log.d(TAG, "Data cleanup completed successfully")
            Result.success()
        } catch (exception: Exception) {
            Log.e(TAG, "Data cleanup failed", exception)
            Result.failure()
        }
    }
}
