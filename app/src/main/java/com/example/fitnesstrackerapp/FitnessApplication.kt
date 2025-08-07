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
import androidx.work.Configuration
import androidx.work.WorkManager
import com.example.fitnesstrackerapp.di.appModule
import com.example.fitnesstrackerapp.di.workerModule
import com.example.fitnesstrackerapp.worker.KoinWorkerFactory
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin

/**
 * Main Application class that initializes all app-wide components
 */
class FitnessApplication : Application(), Configuration.Provider {

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
