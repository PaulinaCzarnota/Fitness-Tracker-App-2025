package com.example.fitnesstrackerapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * FitnessDatabase
 *
 * Room database for the Fitness Tracker App.
 * This database holds tables for Workout, Goal, and Diet entities.
 * It exposes DAO interfaces to access each table.
 */
@Database(
    entities = [Workout::class, Goal::class, Diet::class], // Your 3 main entities
    version = 1,
    exportSchema = false // No schema file needed for local app use
)
abstract class FitnessDatabase : RoomDatabase() {

    /** Provides access to Workout table operations */
    abstract fun workoutDao(): WorkoutDao

    /** Provides access to Goal table operations */
    abstract fun goalDao(): GoalDao

    /** Provides access to Diet table operations */
    abstract fun dietDao(): DietDao

    companion object {
        @Volatile
        private var INSTANCE: FitnessDatabase? = null

        /**
         * Returns the singleton database instance.
         * Uses synchronized block for thread-safe initialization.
         */
        fun getDatabase(context: Context): FitnessDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FitnessDatabase::class.java,
                    "fitness_db" // Database file name
                )
                    .fallbackToDestructiveMigration(dropAllTables = true) // Reset DB on version mismatch
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
