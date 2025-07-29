package com.example.fitnesstrackerapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.fitnesstrackerapp.data.converter.DateConverter

/**
 * Main Room database class for the Fitness Tracker App.
 *
 * @Database:
 *  - entities: the data tables in this database.
 *  - version: bump this whenever you change the schema.
 *  - exportSchema: set to false to skip keeping a schema file in your repo.
 *
 * @TypeConverters:
 *  Register any converters for non‑primitive types (e.g. Date).
 */
@Database(
    entities = [
        User::class,
        Diet::class,
        Workout::class,
        Goal::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class FitnessDatabase : RoomDatabase() {

    /** Provides access to user-related database operations. */
    abstract fun userDao(): UserDao

    /** Provides access to diet-related database operations. */
    abstract fun dietDao(): DietDao

    /** Provides access to workout-related database operations. */
    abstract fun workoutDao(): WorkoutDao

    /** Provides access to goal-related database operations. */
    abstract fun goalDao(): GoalDao
}
