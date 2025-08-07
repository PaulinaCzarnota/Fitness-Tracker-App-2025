package com.example.fitnesstrackerapp.di

import androidx.room.Room
import com.example.fitnesstrackerapp.data.database.AppDatabase
import com.example.fitnesstrackerapp.notifications.NotificationHelper
import com.example.fitnesstrackerapp.repository.*
import com.example.fitnesstrackerapp.security.CryptoManager
import com.example.fitnesstrackerapp.ui.auth.AuthViewModel
import com.example.fitnesstrackerapp.ui.nutrition.NutritionViewModel
import com.example.fitnesstrackerapp.ui.viewmodel.*
import com.example.fitnesstrackerapp.ui.workout.WorkoutViewModel
import com.example.fitnesstrackerapp.ui.goal.GoalViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Database
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "fitness_tracker_database"
        ).fallbackToDestructiveMigration()
         .build()
    }

    // DAOs
    single { get<AppDatabase>().userDao() }
    single { get<AppDatabase>().workoutDao() }
    single { get<AppDatabase>().goalDao() }
    single { get<AppDatabase>().stepDao() }
    single { get<AppDatabase>().foodEntryDao() }

    // Security
    single { CryptoManager(androidContext()) }

    // Repositories
    single { AuthRepository(get(), get()) }
    single { WorkoutRepository(get()) }
    single { GoalRepository(get()) }
    single { StepRepository(get()) }
    single { NutritionRepository(get()) }

    // Utilities
    single { NotificationHelper(androidContext()) }

    // ViewModels - Fixed to match actual constructors
    viewModel { AuthViewModel(get()) }
    viewModel { (userId: Long) -> WorkoutViewModel(get(), userId) }
    viewModel { GoalViewModel(get()) }
    viewModel { (userId: Long) -> StepCounterViewModel(get(), get(), userId) }
    viewModel { (userId: Long) -> NutritionViewModel(get(), userId) }
    viewModel { ProgressViewModel(get(), get(), get()) }
}
