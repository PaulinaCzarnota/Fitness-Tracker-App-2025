/**
 * Fitness Tracker Application Class
 *
 * Responsibilities:
 * - Initialize app-wide components
 * - Set up database and repositories
 * - Configure background services
 */

package com.example.fitnesstrackerapp

import android.app.Application
import androidx.room.Room
import com.example.fitnesstrackerapp.data.database.AppDatabase
import com.example.fitnesstrackerapp.repository.AuthRepository
import com.example.fitnesstrackerapp.repository.WorkoutRepository
import com.example.fitnesstrackerapp.security.CryptoManager

/**
 * Application class for initializing app-wide components
 */
class FitnessTrackerApplication : Application() {

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

}
