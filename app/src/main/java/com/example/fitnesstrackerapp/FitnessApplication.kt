/**
 * Main Application class for the Fitness Tracker Android application.
 *
 * This class serves as the entry point for application-wide initialization and configuration.
 * It manages the lifecycle of core components and ensures proper dependency injection setup.
 *
 * Key Responsibilities:
 * - Initialize ServiceLocator dependency injection
 * - Configure Room database and repository layer
 * - Handle application-level exceptions and crash reporting
 */

package com.example.fitnesstrackerapp

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import com.example.fitnesstrackerapp.scheduler.WorkManagerScheduler

/**
 * Main Application class that initializes all app-wide components.
 */
class FitnessApplication : Application() {

    companion object {
        private const val TAG = "FitnessApplication"
    }

    /**
     * Called when the application is starting, before any other application objects have been created.
     * Initializes dependency injection and critical app components.
     */
    override fun onCreate() {
        super.onCreate()

        try {
            // Initialize ServiceLocator for dependency injection
            ServiceLocator.init(this)

            // Initialize WorkManager with custom configuration
            initializeWorkManager()

            // Initialize background work and step tracking
            initializeBackgroundWork()

            Log.i(TAG, "Application initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize application", e)
            throw e
        }
    }

    /**
     * Called when the application is terminating.
     * Cleans up resources and closes database connections.
     */
    override fun onTerminate() {
        super.onTerminate()
        try {
            ServiceLocator.cleanup()
            Log.i(TAG, "Application terminated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error during application termination", e)
        }
    }

    /**
     * Called when the overall system is running low on memory.
     * Performs memory cleanup operations.
     */
    override fun onLowMemory() {
        super.onLowMemory()
        Log.w(TAG, "System low on memory - performing cleanup")
        ServiceLocator.performMemoryCleanup()
    }

    /**
     * Initializes WorkManager with custom configuration for better performance.
     */
    private fun initializeWorkManager() {
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) Log.DEBUG else Log.INFO)
            .build()

        WorkManager.initialize(this, config)
        Log.d(TAG, "WorkManager initialized with custom configuration")
    }

    /**
     * Initializes background work including step tracking service and notifications.
     */
    private fun initializeBackgroundWork() {
        try {
            val workManagerScheduler = WorkManagerScheduler.getInstance(this)

            // Start step counter service immediately
            workManagerScheduler.startStepCounterService()

            // Get current user ID (fallback to 1L if not available)
            val prefs = getSharedPreferences("fitness_tracker_prefs", MODE_PRIVATE)
            val userId = prefs.getLong("current_user_id", 1L)

            // Schedule all background notifications and work
            workManagerScheduler.initializeAllWork(userId)

            Log.d(TAG, "Background work initialized successfully for user: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize background work", e)
            // Don't throw here as the app can still function without background work
        }
    }
}
