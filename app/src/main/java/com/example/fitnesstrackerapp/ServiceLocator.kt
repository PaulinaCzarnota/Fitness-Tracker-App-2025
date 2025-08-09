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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fitnesstrackerapp.auth.SessionManager
import com.example.fitnesstrackerapp.data.database.AppDatabase
import com.example.fitnesstrackerapp.notification.SimpleNotificationManager
import com.example.fitnesstrackerapp.repository.AuthRepository
import com.example.fitnesstrackerapp.repository.FoodEntryRepository
import com.example.fitnesstrackerapp.repository.GoalRepository
import com.example.fitnesstrackerapp.repository.NotificationRepository
import com.example.fitnesstrackerapp.repository.SimpleNutritionRepository
import com.example.fitnesstrackerapp.repository.StepRepository
import com.example.fitnesstrackerapp.repository.WorkoutRepository
import com.example.fitnesstrackerapp.security.CryptoManager
import com.example.fitnesstrackerapp.sensors.StepTracker
import com.example.fitnesstrackerapp.settings.SettingsManager
import com.example.fitnesstrackerapp.ui.auth.AuthViewModel
import com.example.fitnesstrackerapp.ui.goal.GoalViewModel
import com.example.fitnesstrackerapp.ui.nutrition.NutritionViewModel
import com.example.fitnesstrackerapp.ui.viewmodel.ProgressViewModel
import com.example.fitnesstrackerapp.ui.viewmodel.StepCounterViewModel
import com.example.fitnesstrackerapp.ui.viewmodel.StepDashboardViewModel
import com.example.fitnesstrackerapp.ui.workout.WorkoutManagementViewModel
import com.example.fitnesstrackerapp.ui.workout.WorkoutViewModel
import com.example.fitnesstrackerapp.usecase.LogWorkoutUseCase
import com.example.fitnesstrackerapp.usecase.ManageGoalsUseCase
import com.example.fitnesstrackerapp.usecase.TrackNutritionUseCase
import com.example.fitnesstrackerapp.usecase.TrackStepsUseCase

/**
 * ServiceLocator provides centralized access to all app dependencies.
 *
 * Implements thread-safe singleton pattern with lazy initialization
 * and proper resource management for memory efficiency.
 */
class ServiceLocator private constructor(private val appContext: Context) {
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
         * Gets the ServiceLocator instance (alias for get).
         *
         * @param context Context for fallback initialization
         * @return ServiceLocator singleton instance
         */
        fun getInstance(context: Context): ServiceLocator = get(context)

        // Convenience accessors for commonly used dependencies
        val authRepository: AuthRepository get() = get().authRepository
        val workoutRepository: WorkoutRepository get() = get().workoutRepository
        val goalRepository: GoalRepository get() = get().goalRepository
        val stepRepository: StepRepository get() = get().stepRepository
        val nutritionRepository: SimpleNutritionRepository get() = get().nutritionRepository
        val foodEntryRepository: FoodEntryRepository get() = get().foodEntryRepository
        val notificationRepository: NotificationRepository get() = get().notificationRepository
        val notificationManager: SimpleNotificationManager get() = get().notificationManager
        val sessionManager: SessionManager get() = get().sessionManager
        val settingsManager: SettingsManager get() = get().settingsManager
        val stepTracker: StepTracker get() = get().stepTracker
        val viewModelFactory: ViewModelProvider.Factory get() = get().viewModelFactory

        /**
         * Performs memory cleanup during low memory conditions.
         * Called during low memory situations.
         */
        fun performMemoryCleanup() {
            instance?.let { serviceLocator ->
                try {
                    // Clear any in-memory caches
                    serviceLocator.stepTracker.clearCache()

                    // Request garbage collection to free up memory
                    System.gc()

                    Log.d(TAG, "Memory cleanup performed")
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
    val database: AppDatabase = AppDatabase.getInstance(appContext)

    // DAOs - Lazy initialization for better performance
    private val userDao by lazy { database.userDao() }
    private val workoutDao by lazy { database.workoutDao() }
    private val goalDao by lazy { database.goalDao() }
    private val stepDao by lazy { database.stepDao() }
    private val foodEntryDao by lazy { database.foodEntryDao() }
    private val notificationDao by lazy { database.notificationDao() }

    // Core utilities and managers
    val cryptoManager by lazy { CryptoManager(appContext) }
    val sessionManager by lazy { SessionManager(appContext, userDao) }
    val settingsManager by lazy { SettingsManager(appContext) }
    val notificationManager by lazy { SimpleNotificationManager(appContext) }
    val stepTracker by lazy { StepTracker(appContext) }

    // Repositories with proper initialization
    val authRepository by lazy { AuthRepository(userDao, cryptoManager, sessionManager, appContext) }
    val workoutRepository by lazy { WorkoutRepository(workoutDao) }
    val goalRepository by lazy { GoalRepository(goalDao) }
    val stepRepository by lazy { StepRepository(stepDao) }
    val nutritionRepository by lazy { SimpleNutritionRepository(foodEntryDao) }
    val foodEntryRepository by lazy { FoodEntryRepository(foodEntryDao) }
    val notificationRepository by lazy { NotificationRepository(notificationDao) }

    // Use cases with proper initialization
    val logWorkoutUseCase by lazy { LogWorkoutUseCase(workoutRepository) }
    val trackStepsUseCase by lazy { TrackStepsUseCase(stepRepository) }
    val trackNutritionUseCase by lazy { TrackNutritionUseCase(nutritionRepository) }
    val manageGoalsUseCase by lazy { ManageGoalsUseCase(goalRepository) }

    // ViewModel Factory for dependency injection
    val viewModelFactory by lazy {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return when {
                    modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                        @Suppress("UNCHECKED_CAST")
                        AuthViewModel(authRepository) as T
                    }
                    modelClass.isAssignableFrom(
                        WorkoutViewModel::class.java,
                    ) -> {
                        @Suppress("UNCHECKED_CAST")
                        WorkoutViewModel(workoutRepository, 1L) as T
                    }
                    modelClass.isAssignableFrom(GoalViewModel::class.java) -> {
                        @Suppress("UNCHECKED_CAST")
                        GoalViewModel(goalRepository) as T
                    }
                    modelClass.isAssignableFrom(
                        NutritionViewModel::class.java,
                    ) -> {
                        @Suppress("UNCHECKED_CAST")
                        NutritionViewModel(nutritionRepository, 1L) as T
                    }
                    modelClass.isAssignableFrom(
                        StepCounterViewModel::class.java,
                    ) -> {
                        @Suppress("UNCHECKED_CAST")
                        StepCounterViewModel(
                            stepRepository,
                            authRepository,
                            1L,
                        ) as T
                    }
                    modelClass.isAssignableFrom(
                        ProgressViewModel::class.java,
                    ) -> {
                        @Suppress("UNCHECKED_CAST")
                        ProgressViewModel(
                            workoutRepository,
                            stepRepository,
                            authRepository,
                        ) as T
                    }
                    modelClass.isAssignableFrom(
                        StepDashboardViewModel::class.java,
                    ) -> {
                        @Suppress("UNCHECKED_CAST")
                        StepDashboardViewModel(appContext as android.app.Application) as T
                    }
                    // Note: WorkoutLoggingViewModel and WorkoutManagementViewModel are commented out
                    // because they require additional repositories (ExerciseRepository, WorkoutSetRepository)
                    // that are not yet implemented. Uncomment and fix when these dependencies are available.
                    /*
                    modelClass.isAssignableFrom(
                        WorkoutLoggingViewModel::class.java,
                    ) -> {
                        @Suppress("UNCHECKED_CAST")
                        WorkoutLoggingViewModel(
                            workoutRepository,
                            exerciseRepository, // Would need to be implemented
                            workoutSetRepository, // Would need to be implemented
                            1L
                        ) as T
                    }
                     */
                    modelClass.isAssignableFrom(
                        WorkoutManagementViewModel::class.java,
                    ) -> {
                        @Suppress("UNCHECKED_CAST")
                        WorkoutManagementViewModel(workoutRepository, 1L) as T
                    }
                    else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
                }
            }
        }
    }

    /**
     * Checks if the ServiceLocator is properly initialized.
     *
     * @return true if initialized, false otherwise
     */
    fun isInitialized(): Boolean = instance != null
}
