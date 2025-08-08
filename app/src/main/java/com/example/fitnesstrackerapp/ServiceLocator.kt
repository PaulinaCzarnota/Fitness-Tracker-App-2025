/**
 * ServiceLocator
 *
 * Centralized, lightweight dependency provider to avoid non-standard DI libraries.
 * Provides singletons for database, repositories, and utilities.
 */

package com.example.fitnesstrackerapp

import android.content.Context
import com.example.fitnesstrackerapp.data.database.AppDatabase
import com.example.fitnesstrackerapp.notifications.NotificationHelper
import com.example.fitnesstrackerapp.repository.AuthRepository
import com.example.fitnesstrackerapp.repository.GoalRepository
import com.example.fitnesstrackerapp.repository.NutritionRepository
import com.example.fitnesstrackerapp.repository.StepRepository
import com.example.fitnesstrackerapp.repository.WorkoutRepository
import com.example.fitnesstrackerapp.security.CryptoManager

class ServiceLocator private constructor(appContext: Context) {
    private val database: AppDatabase = AppDatabase.getInstance(appContext)

    // DAOs
    private val userDao = database.userDao()
    private val workoutDao = database.workoutDao()
    private val goalDao = database.goalDao()
    private val stepDao = database.stepDao()
    private val foodEntryDao = database.foodEntryDao()

    // Utilities
    val cryptoManager = CryptoManager(appContext)
    val notificationHelper = NotificationHelper(appContext)

    // Repositories
    val authRepository = AuthRepository(userDao, cryptoManager)
    val workoutRepository = WorkoutRepository(workoutDao)
    val goalRepository = GoalRepository(goalDao)
    val stepRepository = StepRepository(stepDao)
    val nutritionRepository = NutritionRepository(foodEntryDao)

    companion object {
        @Volatile private var instance: ServiceLocator? = null

        fun init(context: Context) {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = ServiceLocator(context.applicationContext)
                    }
                }
            }
        }

        fun get(context: Context): ServiceLocator {
            return instance ?: synchronized(this) {
                instance ?: ServiceLocator(context.applicationContext).also { instance = it }
            }
        }
    }
}
