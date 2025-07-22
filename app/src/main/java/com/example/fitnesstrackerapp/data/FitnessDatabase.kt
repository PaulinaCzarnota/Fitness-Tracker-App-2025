package com.example.fitnesstrackerapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Main Room database for the Fitness Tracker App.
 * Includes all app entities: Workout, Goal, and Diet.
 */
@Database(
    entities = [Workout::class, Goal::class, Diet::class],
    version = 1,
    exportSchema = false
)
abstract class FitnessDatabase : RoomDatabase() {

    // DAO accessors for the different entity tables
    abstract fun workoutDao(): WorkoutDao
    abstract fun goalDao(): GoalDao
    abstract fun dietDao(): DietDao

    companion object {
        // Singleton instance of the database (thread-safe)
        @Volatile
        private var INSTANCE: FitnessDatabase? = null

        /**
         * Returns the singleton database instance.
         * Creates the database if it does not already exist.
         */
        fun getDatabase(context: Context): FitnessDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FitnessDatabase::class.java,
                    "fitness_db" // Database file name
                )
                    // Wipes and rebuilds instead of crashing if no migration is provided
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
