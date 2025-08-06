/**
 * Koin Application Module
 *
 * Provides application-level dependencies for the Fitness Tracker App including:
 * - ViewModels for UI state management
 * - Repositories for data access
 * - Services for background operations
 * - Utility classes for app functionality
 */

package com.example.fitnesstrackerapp.di

import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.fitnesstrackerapp.auth.AuthService
import com.example.fitnesstrackerapp.auth.BiometricAuthManager
import com.example.fitnesstrackerapp.auth.SessionManager
import com.example.fitnesstrackerapp.repository.AuthRepository
import com.example.fitnesstrackerapp.repository.GoalRepository
import com.example.fitnesstrackerapp.repository.NutritionRepository
import com.example.fitnesstrackerapp.repository.StepRepository
import com.example.fitnesstrackerapp.repository.WorkoutRepository
import com.example.fitnesstrackerapp.scheduler.TaskScheduler
import com.example.fitnesstrackerapp.security.PasswordManager
import com.example.fitnesstrackerapp.sensors.StepTracker
import com.example.fitnesstrackerapp.ui.auth.AuthViewModel
import com.example.fitnesstrackerapp.ui.goal.GoalViewModel
import com.example.fitnesstrackerapp.ui.nutrition.NutritionViewModel
import com.example.fitnesstrackerapp.ui.viewmodel.ProgressViewModel
import com.example.fitnesstrackerapp.ui.viewmodel.StepCounterViewModel
import com.example.fitnesstrackerapp.ui.workout.WorkoutViewModel
import com.example.fitnesstrackerapp.util.NotificationHelper
import com.example.fitnesstrackerapp.notifications.NotificationManager
import com.example.fitnesstrackerapp.security.SecurityManager
import com.example.fitnesstrackerapp.settings.SettingsManager
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    // Shared Preferences (Encrypted)
    single<SharedPreferences> {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            "fitness_tracker_prefs",
            masterKeyAlias,
            androidContext(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // Repositories
    single { AuthRepository(get()) }
    single { WorkoutRepository(get()) }
    single { GoalRepository(get()) }
    single { StepRepository(get()) }
    single { NutritionRepository(get()) }

    // ViewModels
    viewModel { AuthViewModel(get()) }
    viewModel { WorkoutViewModel(get(), get()) }
    viewModel { GoalViewModel(get()) }
    viewModel { NutritionViewModel(get()) }
    viewModel { ProgressViewModel(get(), get(), get()) }
    viewModel { StepCounterViewModel(get(), get()) }

    // Services and Utilities
    single { AuthService(get(), get(), get(), get()) }
    single { SessionManager(androidContext()) }
    single { BiometricAuthManager(androidContext()) }
    single { PasswordManager() }
    single { NotificationManager(androidContext()) }
    single { StepTracker(androidContext()) }
    single { TaskScheduler(androidContext()) }
    single { SecurityManager(androidContext()) }
    single { SettingsManager(androidContext()) }
}
