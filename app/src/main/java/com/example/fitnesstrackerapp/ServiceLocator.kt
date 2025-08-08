/**
 * ServiceLocator - Centralized Dependency Provider
 *
 * Lightweight dependency injection solution that avoids non-standard libraries.
 * Provides singleton instances for database, repositories, and utility classes.
 *
 * This implementation follows the Service Locator pattern to manage dependencies
 * while maintaining compliance with assignment requirements.
 */

package com.example.fitnesstrackerapp

import android.content.Context
import android.util.Log
import com.example.fitnesstrackerapp.data.database.AppDatabase
import com.example.fitnesstrackerapp.repository.AuthRepository
import com.example.fitnesstrackerapp.repository.GoalRepository
import com.example.fitnesstrackerapp.repository.NutritionRepository
import com.example.fitnesstrackerapp.repository.StepRepository
import com.example.fitnesstrackerapp.repository.WorkoutRepository
import com.example.fitnesstrackerapp.security.CryptoManager

/**
 * ServiceLocator provides centralized access to all app dependencies.
 *
 * Implements thread-safe singleton pattern with lazy initialization
 * and proper resource management for memory efficiency.
 */
class ServiceLocator private constructor(appContext: Context) {

    companion object {
        private const val TAG = "ServiceLocator"

        @Volatile
        private var instance: ServiceLocator? = null

        /**
         * Initializes the ServiceLocator with application context.
         * Must be called during application startup.
         *
         * @param context Application context for dependency initialization
         */
        fun init(context: Context) {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = ServiceLocator(context.applicationContext)
                        Log.d(TAG, "ServiceLocator initialized successfully")
                    }
                }
            }
        }

        /**
         * Gets the ServiceLocator instance.
         *
         * @param context Context for fallback initialization
         * @return ServiceLocator singleton instance
         * @throws IllegalStateException if not initialized and context is null
         */
        fun get(context: Context? = null): ServiceLocator {
            return instance ?: synchronized(this) {
                instance ?: run {
                    if (context == null) {
                        throw IllegalStateException("ServiceLocator not initialized. Call init() first.")
                    }
                    ServiceLocator(context.applicationContext).also {
                        instance = it
                        Log.d(TAG, "ServiceLocator lazy-initialized")
                    }
                }
            }
        }

        /**
         * Clears cached data and performs memory cleanup.
         * Called during low memory conditions.
         */
        fun clearCaches() {
            instance?.let { serviceLocator ->
                try {
                    // Request garbage collection to free up memory
                    System.gc()

                    // Clear any in-memory caches if needed
                    // Note: Repositories will automatically handle their own cache invalidation
                    // through Room's built-in memory management

                    Log.d(TAG, "Memory cleanup requested")
                } catch (e: Exception) {
                    Log.e(TAG, "Error during memory cleanup", e)
                }
            }
        }

        /**
         * Cleans up resources and closes database connections.
         * Called during application termination.
         */
        fun cleanup() {
            instance?.let { serviceLocator ->
                try {
                    serviceLocator.database.close()
                    instance = null
                    Log.d(TAG, "ServiceLocator cleaned up successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Error during cleanup", e)
                }
            }
        }
    }

    // Core components
    private val database: AppDatabase = AppDatabase.getInstance(appContext)

    // DAOs - Lazy initialization for better performance
    private val userDao by lazy { database.userDao() }
    private val workoutDao by lazy { database.workoutDao() }
    private val goalDao by lazy { database.goalDao() }
    private val stepDao by lazy { database.stepDao() }
    private val foodEntryDao by lazy { database.foodEntryDao() }

    // Utilities with proper initialization - only keep what's actively used
    val cryptoManager by lazy { CryptoManager(appContext) }

    // Repositories with proper initialization
    val authRepository by lazy { AuthRepository(userDao, cryptoManager, appContext) }
    val workoutRepository by lazy { WorkoutRepository(workoutDao) }
    val goalRepository by lazy { GoalRepository(goalDao) }
    val stepRepository by lazy { StepRepository(stepDao) }
    val nutritionRepository by lazy { NutritionRepository(foodEntryDao) }

    /**
     * Checks if the ServiceLocator is properly initialized.
     *
     * @return true if initialized, false otherwise
     */
    fun isInitialized(): Boolean = instance != null
}
