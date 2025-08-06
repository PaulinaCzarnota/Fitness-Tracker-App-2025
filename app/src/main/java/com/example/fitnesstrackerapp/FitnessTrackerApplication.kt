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

}
