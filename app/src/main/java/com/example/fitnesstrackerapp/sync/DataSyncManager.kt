package com.example.fitnesstrackerapp.sync

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.*
import com.example.fitnesstrackerapp.data.database.AppDatabase
import com.example.fitnesstrackerapp.repository.StepRepository
import com.example.fitnesstrackerapp.repository.WorkoutRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class DataSyncManager(
    private val context: Context,
    private val database: AppDatabase,
    private val stepRepository: StepRepository,
    private val workoutRepository: WorkoutRepository
) {
    private val workManager = WorkManager.getInstance(context)

    fun scheduleDailySync() {
        val syncRequest = PeriodicWorkRequestBuilder<DataSyncWorker>(
            24, TimeUnit.HOURS,
            1, TimeUnit.HOURS
        )
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
        )
        .build()

        workManager.enqueueUniquePeriodicWork(
            "daily_data_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }

    suspend fun performSync(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Export database to backup location
            val dbFile = context.getDatabasePath(database.openHelper.databaseName)
            if (dbFile.exists()) {
                val backupFile = context.getFileStreamPath("backup_${System.currentTimeMillis()}.db")
                dbFile.copyTo(backupFile, overwrite = true)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Database file not found"))
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: DataSyncManager? = null

        fun getInstance(context: Context): DataSyncManager {
            return INSTANCE ?: synchronized(this) {
                val database = AppDatabase.getInstance(context)
                val stepRepository = StepRepository(database.stepDao())
                val workoutRepository = WorkoutRepository(database.workoutDao())
                val instance = DataSyncManager(context, database, stepRepository, workoutRepository)
                INSTANCE = instance
                instance
            }
        }
    }
}

class DataSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            val syncManager = DataSyncManager.getInstance(applicationContext)
            val result = syncManager.performSync()
            if (result.isSuccess) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
