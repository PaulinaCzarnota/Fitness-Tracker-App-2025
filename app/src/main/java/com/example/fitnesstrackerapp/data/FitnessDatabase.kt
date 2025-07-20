package com.example.fitnesstrackerapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room database class for the Fitness Tracker App.
 * Provides access to the DAO for Workout entities.
 */
@Database(entities = [Workout::class], version = 1, exportSchema = false)
abstract class FitnessDatabase : RoomDatabase() {

    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile
        private var INSTANCE: FitnessDatabase? = null

        /**
         * Returns a singleton instance of the database.
         */
        fun getDatabase(context: Context): FitnessDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FitnessDatabase::class.java,
                    "fitness_db"
                )
                    .fallbackToDestructiveMigration() // Optional: enables auto-reset if schema changes
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
