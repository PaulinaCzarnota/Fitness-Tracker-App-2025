package com.example.fitnesstrackerapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * FitnessDatabase
 *
 * Main Room database class for the Fitness Tracker App.
 * Provides access to DAOs for workout logging, goal tracking, and diet entries.
 */
@Database(
    entities = [Workout::class, Goal::class, Diet::class], // Tables in the DB
    version = 1, // Increment this if schema changes
    exportSchema = false // Avoid exporting schema files to disk
)
abstract class FitnessDatabase : RoomDatabase() {

    /** Accessor for workout DAO */
    abstract fun workoutDao(): WorkoutDao

    /** Accessor for goal DAO */
    abstract fun goalDao(): GoalDao

    /** Accessor for diet DAO */
    abstract fun dietDao(): DietDao

    companion object {
        @Volatile
        private var INSTANCE: FitnessDatabase? = null

        /**
         * Singleton accessor for database instance.
         * Builds the database using Room if not yet created.
         */
        fun getDatabase(context: Context): FitnessDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FitnessDatabase::class.java,
                    "fitness_db"
                )

                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
