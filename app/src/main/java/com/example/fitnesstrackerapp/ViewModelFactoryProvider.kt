package com.example.fitnesstrackerapp

/**
 * ViewModelFactoryProvider - Centralized ViewModel Factory Provider
 *
 * Provides factory-backed ViewModel instances without relying on external DI libraries.
 * This implementation uses the ServiceLocator pattern to inject dependencies into ViewModels
 * while maintaining compliance with assignment requirements.
 *
 * Each method creates a custom ViewModelProvider.Factory that properly instantiates
 * the requested ViewModel with its required dependencies.
 */

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fitnesstrackerapp.ui.auth.AuthViewModel
import com.example.fitnesstrackerapp.ui.goal.GoalViewModel
import com.example.fitnesstrackerapp.ui.nutrition.NutritionViewModel
import com.example.fitnesstrackerapp.ui.viewmodel.ProgressViewModel
import com.example.fitnesstrackerapp.ui.viewmodel.StepCounterViewModel
import com.example.fitnesstrackerapp.ui.workout.WorkoutManagementViewModel
import com.example.fitnesstrackerapp.ui.workout.WorkoutViewModel

/**
 * Object providing centralized ViewModel factory methods for dependency injection.
 */
object ViewModelFactoryProvider {
    const val TAG = "ViewModelFactoryProvider"

    /**
     * Creates an AuthViewModel with proper dependency injection.
     *
     * @param activity ComponentActivity for ViewModel scope
     * @return AuthViewModel instance with injected AuthRepository
     */
    fun getAuthViewModel(activity: ComponentActivity): AuthViewModel {
        val appContext = activity.applicationContext
        val authRepo = ServiceLocator.get(appContext).authRepository

        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return AuthViewModel(authRepo) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }

        Log.d(TAG, "Creating AuthViewModel")
        return ViewModelProvider(activity, factory)[AuthViewModel::class.java]
    }

    /**
     * Creates an AuthViewModel factory with proper dependency injection.
     *
     * @param serviceLocator ServiceLocator instance for dependency injection
     * @return ViewModelProvider.Factory for AuthViewModel
     */
    fun getAuthViewModelFactory(serviceLocator: ServiceLocator): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return AuthViewModel(serviceLocator.authRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }

    /**
     * Creates a WorkoutViewModel with proper dependency injection.
     *
     * @param activity ComponentActivity for ViewModel scope
     * @param userId User ID for workout data filtering
     * @return WorkoutViewModel instance with injected WorkoutRepository
     */
    fun getWorkoutViewModel(activity: ComponentActivity, userId: Long): WorkoutViewModel {
        val appContext = activity.applicationContext
        val repo = ServiceLocator.get(appContext).workoutRepository

        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(WorkoutViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return WorkoutViewModel(repo, userId) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }

        Log.d(TAG, "Creating WorkoutViewModel for user: $userId")
        return ViewModelProvider(activity, factory)[WorkoutViewModel::class.java]
    }

    /**
     * Creates a GoalViewModel with proper dependency injection.
     *
     * @param activity ComponentActivity for ViewModel scope
     * @return GoalViewModel instance with injected GoalRepository
     */
    fun getGoalViewModel(activity: ComponentActivity): GoalViewModel {
        val appContext = activity.applicationContext
        val repo = ServiceLocator.get(appContext).goalRepository

        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(GoalViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return GoalViewModel(repo) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }

        Log.d(TAG, "Creating GoalViewModel")
        return ViewModelProvider(activity, factory)[GoalViewModel::class.java]
    }

    /**
     * Creates a StepCounterViewModel with proper dependency injection.
     *
     * @param activity ComponentActivity for ViewModel scope
     * @param userId User ID for step data filtering
     * @return StepCounterViewModel instance with injected repositories
     */
    fun getStepCounterViewModel(activity: ComponentActivity, userId: Long): StepCounterViewModel {
        val appContext = activity.applicationContext
        val stepRepo = ServiceLocator.get(appContext).stepRepository
        val authRepo = ServiceLocator.get(appContext).authRepository

        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(StepCounterViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return StepCounterViewModel(stepRepo, authRepo, userId) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }

        Log.d(TAG, "Creating StepCounterViewModel for user: $userId")
        return ViewModelProvider(activity, factory)[StepCounterViewModel::class.java]
    }

    /**
     * Creates a ProgressViewModel with proper dependency injection.
     *
     * @param activity ComponentActivity for ViewModel scope
     * @return ProgressViewModel instance with injected repositories
     */
    fun getProgressViewModel(activity: ComponentActivity): ProgressViewModel {
        val appContext = activity.applicationContext
        val workoutRepo = ServiceLocator.get(appContext).workoutRepository
        val stepRepo = ServiceLocator.get(appContext).stepRepository
        val authRepo = ServiceLocator.get(appContext).authRepository

        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ProgressViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return ProgressViewModel(workoutRepo, stepRepo, authRepo) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }

        Log.d(TAG, "Creating ProgressViewModel")
        return ViewModelProvider(activity, factory)[ProgressViewModel::class.java]
    }

    /**
     * Creates a NutritionViewModel with proper dependency injection.
     *
     * @param activity ComponentActivity for ViewModel scope
     * @param userId User ID for nutrition data filtering
     * @return NutritionViewModel instance with injected NutritionRepository
     */
    fun getNutritionViewModel(activity: ComponentActivity, userId: Long): NutritionViewModel {
        val appContext = activity.applicationContext
        val repo = ServiceLocator.get(appContext).nutritionRepository

        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(NutritionViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return NutritionViewModel(repo, userId) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }

        Log.d(TAG, "Creating NutritionViewModel for user: $userId")
        return ViewModelProvider(activity, factory)[NutritionViewModel::class.java]
    }

    /**
     * Creates a WorkoutManagementViewModel with proper dependency injection.
     *
     * @param activity ComponentActivity for ViewModel scope
     * @param userId User ID for workout data filtering
     * @return WorkoutManagementViewModel instance with injected WorkoutRepository
     */
    fun getWorkoutManagementViewModel(activity: ComponentActivity, userId: Long): WorkoutManagementViewModel {
        val appContext = activity.applicationContext
        val workoutRepo = ServiceLocator.get(appContext).workoutRepository

        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(WorkoutManagementViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return WorkoutManagementViewModel(workoutRepo, userId) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }

        Log.d(TAG, "Creating WorkoutManagementViewModel for user: $userId")
        return ViewModelProvider(activity, factory)[WorkoutManagementViewModel::class.java]
    }

    /**
     * Generic method to create any ViewModel with custom factory.
     * Useful for testing or future ViewModel additions.
     *
     * @param activity ComponentActivity for ViewModel scope
     * @param factory Custom ViewModelProvider.Factory
     * @param modelClass Class of the ViewModel to create
     * @return ViewModel instance of the requested type
     */
    inline fun <reified T : ViewModel> createViewModel(
        activity: ComponentActivity,
        factory: ViewModelProvider.Factory,
    ): T {
        Log.d(TAG, "Creating custom ViewModel: ${T::class.java.simpleName}")
        return ViewModelProvider(activity, factory)[T::class.java]
    }

    /**
     * Checks if ServiceLocator is properly initialized before creating ViewModels.
     *
     * @param context Context to check ServiceLocator initialization
     * @throws IllegalStateException if ServiceLocator is not initialized
     */
    private fun checkServiceLocatorInitialization(context: android.content.Context) {
        if (!ServiceLocator.get(context).isInitialized()) {
            error("ServiceLocator must be initialized before creating ViewModels")
        }
    }
}
