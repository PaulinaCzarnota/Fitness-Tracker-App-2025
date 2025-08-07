/**
 * Fitness Application Class
 *
 * Responsibilities:
 * - Initialize Koin dependency injection
 * - Set up database and repositories
 * - Configure WorkManager with custom factory
 * - Initialize app-wide components
 */

package com.example.fitnesstrackerapp

import android.app.Application
import androidx.room.Room
import androidx.work.Configuration
import androidx.work.WorkManager
import com.example.fitnesstrackerapp.data.database.AppDatabase
import com.example.fitnesstrackerapp.di.appModule
import com.example.fitnesstrackerapp.di.workerModule
import com.example.fitnesstrackerapp.repository.AuthRepository
import com.example.fitnesstrackerapp.repository.WorkoutRepository
import com.example.fitnesstrackerapp.security.CryptoManager
import com.example.fitnesstrackerapp.worker.KoinWorkerFactory
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin

/**
 * Main Application class that initializes all app-wide components
 */
class FitnessApplication : Application(), Configuration.Provider {

    /**
     * Lazy initialization of the Room database
     */
    val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "fitness_tracker_database"
        ).build()
    }

    /**
     * Lazy initialization of CryptoManager for security operations
     */
    val cryptoManager by lazy {
        CryptoManager(applicationContext)
    }

    /**
     * Lazy initialization of repositories
     */
    val authRepository by lazy {
        AuthRepository(database.userDao(), cryptoManager)
    }

    val workoutRepository by lazy {
        WorkoutRepository(database.workoutDao())
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize Koin dependency injection
        startKoin {
            androidLogger()
            androidContext(this@FitnessApplication)
            workManagerFactory()
            modules(appModule, workerModule)
        }

        // Initialize WorkManager with custom configuration
        WorkManager.initialize(
            this,
            getWorkManagerConfiguration()
        )
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(KoinWorkerFactory())
            .build()

    /**
     * Get WorkManager configuration for initialization
     */
    fun getWorkManagerConfiguration(): Configuration {
        return workManagerConfiguration
    }
}
