package com.example.fitnesstrackerapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * FitnessDatabase
 *
 * Main Room database for the Fitness Tracker App.
 * Includes entities: Workout, Goal, Diet, and User.
 * Provides DAOs for data access.
 */
@Database(
    entities = [Workout::class, Goal::class, Diet::class, User::class],
    version = 1,
    exportSchema = false
)
abstract class FitnessDatabase : RoomDatabase() {

    // --- DAO accessors ---
    abstract fun workoutDao(): WorkoutDao
    abstract fun goalDao(): GoalDao
    abstract fun dietDao(): DietDao
    abstract fun userDao(): UserDao

    companion object {
        // Singleton instance for application-wide DB access
        @Volatile
        private var INSTANCE: FitnessDatabase? = null

        /**
         * Gets the singleton instance of the FitnessDatabase.
         * Builds the database using fallbackToDestructiveMigration with `true`
         * to safely drop all tables on schema changes.
         *
         * @param context Application context (do not use Activity context)
         * @return FitnessDatabase instance
         */
        fun getDatabase(context: Context): FitnessDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FitnessDatabase::class.java,
                    "fitness_database"
                )
                    // Corrected: Pass `true` to indicate all tables may be dropped if needed
                    .fallbackToDestructiveMigration(true)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
