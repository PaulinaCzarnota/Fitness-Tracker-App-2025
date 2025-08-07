package com.example.fitnesstrackerapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.fitnesstrackerapp.data.Converters
import com.example.fitnesstrackerapp.data.dao.*
import com.example.fitnesstrackerapp.data.entity.*

/**
 * Room database class for the Fitness Tracker application.
 *
 * This database includes all entities and provides access to DAOs for:
 * - User management and authentication
 * - Workout tracking and statistics
 * - Step counting and daily activity
 * - Goal setting and progress tracking
 * - Nutrition logging and analysis
 */
@Database(
    entities = [
        User::class,
        Workout::class,
        Goal::class,
        Step::class,
        FoodEntry::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Provides access to User data operations.
     */
    abstract fun userDao(): UserDao

    /**
     * Provides access to Workout data operations.
     */
    abstract fun workoutDao(): WorkoutDao

    /**
     * Provides access to Step data operations.
     */
    abstract fun stepDao(): StepDao

    /**
     * Provides access to Goal data operations.
     */
    abstract fun goalDao(): GoalDao

    /**
     * Provides access to Nutrition data operations.
     */
    abstract fun foodEntryDao(): FoodEntryDao

    companion object {

        private const val DATABASE_NAME = "fitness_tracker_database"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Gets the singleton instance of the database.
         *
         * @param context Application context
         * @return AppDatabase instance
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration() // For development - remove in production
                    .build().also { INSTANCE = it }
            }
        }

        /**
         * Destroys the database instance (for testing purposes).
         */
        fun destroyInstance() {
            INSTANCE = null
        }
    }
}
