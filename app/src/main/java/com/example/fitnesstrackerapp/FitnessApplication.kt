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
        System.gc()
    }
}
