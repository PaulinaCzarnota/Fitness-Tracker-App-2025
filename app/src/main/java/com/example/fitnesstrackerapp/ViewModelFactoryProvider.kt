/**
 * ViewModelFactoryProvider
 *
 * Provides factory-backed ViewModel instances without relying on external DI libraries.
 */

package com.example.fitnesstrackerapp

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fitnesstrackerapp.ui.auth.AuthViewModel
import com.example.fitnesstrackerapp.ui.goal.GoalViewModel
import com.example.fitnesstrackerapp.ui.viewmodel.ProgressViewModel
import com.example.fitnesstrackerapp.ui.viewmodel.StepCounterViewModel
import com.example.fitnesstrackerapp.ui.workout.WorkoutViewModel
import com.example.fitnesstrackerapp.ui.nutrition.NutritionViewModel

object ViewModelFactoryProvider {
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
        return ViewModelProvider(activity, factory)[AuthViewModel::class.java]
    }

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
        return ViewModelProvider(activity, factory)[WorkoutViewModel::class.java]
    }

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
        return ViewModelProvider(activity, factory)[GoalViewModel::class.java]
    }

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
        return ViewModelProvider(activity, factory)[StepCounterViewModel::class.java]
    }

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
        return ViewModelProvider(activity, factory)[ProgressViewModel::class.java]
    }

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
        return ViewModelProvider(activity, factory)[NutritionViewModel::class.java]
    }
}
