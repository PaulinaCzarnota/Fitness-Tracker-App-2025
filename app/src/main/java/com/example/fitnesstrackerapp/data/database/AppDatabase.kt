package com.example.fitnesstrackerapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.fitnesstrackerapp.data.dao.GoalDao
import com.example.fitnesstrackerapp.data.dao.NutritionDao
import com.example.fitnesstrackerapp.data.dao.StepDao
import com.example.fitnesstrackerapp.data.dao.UserDao
import com.example.fitnesstrackerapp.data.dao.WorkoutDao
import com.example.fitnesstrackerapp.data.entity.Goal
import com.example.fitnesstrackerapp.data.entity.Nutrition
import com.example.fitnesstrackerapp.data.entity.Step
import com.example.fitnesstrackerapp.data.entity.User
import com.example.fitnesstrackerapp.data.entity.Workout
import com.example.fitnesstrackerapp.data.entity.FoodEntry
import com.example.fitnesstrackerapp.data.Converters

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
        Step::class,
        Goal::class,
        Nutrition::class,
        FoodEntry::class
    ],
    version = 1,
    exportSchema = false
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
    abstract fun nutritionDao(): NutritionDao

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
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration() // For development - remove in production
                    .build()

                INSTANCE = instance
                instance
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
