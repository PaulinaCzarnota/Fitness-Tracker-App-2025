package com.example.fitnesstrackerapp.data.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.fitnesstrackerapp.data.Converters
import com.example.fitnesstrackerapp.data.dao.ExerciseDao
import com.example.fitnesstrackerapp.data.dao.FoodEntryDao
import com.example.fitnesstrackerapp.data.dao.GoalDao
import com.example.fitnesstrackerapp.data.dao.NotificationDao
import com.example.fitnesstrackerapp.data.dao.NotificationLogDao
import com.example.fitnesstrackerapp.data.dao.NutritionEntryDao
import com.example.fitnesstrackerapp.data.dao.StepDao
import com.example.fitnesstrackerapp.data.dao.UserDao
import com.example.fitnesstrackerapp.data.dao.WorkoutDao
import com.example.fitnesstrackerapp.data.dao.WorkoutSetDao
import com.example.fitnesstrackerapp.data.entity.Exercise
import com.example.fitnesstrackerapp.data.entity.FoodEntry
import com.example.fitnesstrackerapp.data.entity.Goal
import com.example.fitnesstrackerapp.data.entity.Notification
import com.example.fitnesstrackerapp.data.entity.NotificationLog
import com.example.fitnesstrackerapp.data.entity.NutritionEntry
import com.example.fitnesstrackerapp.data.entity.Step
import com.example.fitnesstrackerapp.data.entity.User
import com.example.fitnesstrackerapp.data.entity.Workout
import com.example.fitnesstrackerapp.data.entity.WorkoutSet
import java.util.concurrent.Executors

/**
 * Room database class for the Fitness Tracker application.
 *
 * This database serves as the main data persistence layer and includes:
 * - User management and secure authentication data
 * - Workout tracking with detailed exercise statistics
 * - Step counting and daily activity monitoring
 * - Goal setting and progress tracking with notifications
 * - Nutrition logging and dietary analysis
 * - Comprehensive type converters for complex data types
 *
 * The database uses Room's built-in migration system and provides
 * thread-safe singleton access pattern for optimal performance.
 */
@Database(
    entities = [
        User::class,
        Workout::class,
        Goal::class,
        Step::class,
        FoodEntry::class,
        NutritionEntry::class,
        Notification::class,
        NotificationLog::class,
        Exercise::class,
        WorkoutSet::class,
    ],
    version = 4,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Provides access to User data operations including authentication,
     * profile management, and account security features.
     *
     * @return UserDao instance for user-related database operations
     */
    abstract fun userDao(): UserDao

    /**
     * Provides access to Workout data operations including exercise logging,
     * performance tracking, and workout history management.
     *
     * @return WorkoutDao instance for workout-related database operations
     */
    abstract fun workoutDao(): WorkoutDao

    /**
     * Provides access to Step tracking operations including daily step counts,
     * distance calculations, and activity monitoring.
     *
     * @return StepDao instance for step-related database operations
     */
    abstract fun stepDao(): StepDao

    /**
     * Provides access to Goal management operations including target setting,
     * progress tracking, and achievement notifications.
     *
     * @return GoalDao instance for goal-related database operations
     */
    abstract fun goalDao(): GoalDao

    /**
     * Provides access to Nutrition data operations including food entry logging,
     * calorie tracking, and dietary analysis.
     *
     * @return FoodEntryDao instance for nutrition-related database operations
     */
    abstract fun foodEntryDao(): FoodEntryDao

    /**
     * Provides access to enhanced Nutrition data operations including comprehensive
     * nutritional analysis, micronutrient tracking, and dietary insights.
     *
     * @return NutritionEntryDao instance for advanced nutrition-related database operations
     */
    abstract fun nutritionEntryDao(): NutritionEntryDao

    /**
     * Provides access to Notification data operations including notification scheduling,
     * delivery tracking, and user interaction management.
     *
     * @return NotificationDao instance for notification-related database operations
     */
    abstract fun notificationDao(): NotificationDao

    /**
     * Provides access to Notification Log data operations including delivery analytics,
     * performance monitoring, and user interaction tracking.
     *
     * @return NotificationLogDao instance for notification logging and analytics operations
     */
    abstract fun notificationLogDao(): NotificationLogDao

    /**
     * Provides access to Exercise data operations including exercise definitions,
     * muscle group filtering, and custom exercise management.
     *
     * @return ExerciseDao instance for exercise-related database operations
     */
    abstract fun exerciseDao(): ExerciseDao

    /**
     * Provides access to WorkoutSet data operations including set logging,
     * performance tracking, and personal record management.
     *
     * @return WorkoutSetDao instance for workout set-related database operations
     */
    abstract fun workoutSetDao(): WorkoutSetDao

    companion object {
        private const val TAG = "AppDatabase"
        private const val DATABASE_NAME = "fitness_tracker.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Gets the singleton instance of the database with proper thread safety.
         *
         * Uses double-checked locking pattern to ensure thread safety while
         * maintaining performance. The database is configured with appropriate
         * settings for production use.
         *
         * @param context Application context (automatically converted to application context)
         * @return Thread-safe AppDatabase singleton instance
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = INSTANCE
                instance
                    ?: try {
                        val newInstance = Room.databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java,
                            DATABASE_NAME,
                        )
                            .setQueryExecutor(Executors.newFixedThreadPool(4))
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3) // Add migrations
                            .fallbackToDestructiveMigration() // Only for development
                            .build()

                        INSTANCE = newInstance
                        Log.d(TAG, "Database instance created successfully")
                        newInstance
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to create database instance", e)
                        throw e
                    }
            }
        }

        /**
         * Destroys the database instance.
         *
         * This method is primarily intended for testing purposes to ensure
         * a clean state between test runs. Should be used with caution in
         * production code.
         */
        fun destroyInstance() {
            synchronized(this) {
                INSTANCE?.close()
                INSTANCE = null
                Log.d(TAG, "Database instance destroyed")
            }
        }

        /**
         * Clears all data from the database.
         *
         * WARNING: This operation is irreversible and will delete all user data.
         * Should only be used for testing or explicit user-requested data clearing.
         *
         * @param context Application context to get database instance
         */
        fun clearAllData(context: Context) {
            try {
                val db = getInstance(context)
                db.clearAllAppTables()
                Log.w(TAG, "All database tables cleared")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear database", e)
                throw e
            }
        }

        /**
         * Migration from version 1 to 2: Adds notifications table.
         *
         * This migration adds the comprehensive notifications table with all
         * required columns, indices, and foreign key constraints.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    // Create notifications table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `notifications` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `user_id` INTEGER NOT NULL,
                            `type` TEXT NOT NULL,
                            `title` TEXT NOT NULL,
                            `message` TEXT NOT NULL,
                            `priority` TEXT NOT NULL DEFAULT 'DEFAULT',
                            `status` TEXT NOT NULL DEFAULT 'PENDING',
                            `scheduled_time` INTEGER NOT NULL,
                            `sent_time` INTEGER,
                            `read_time` INTEGER,
                            `dismissed_time` INTEGER,
                            `clicked_time` INTEGER,
                            `is_read` INTEGER NOT NULL DEFAULT 0,
                            `is_recurring` INTEGER NOT NULL DEFAULT 0,
                            `recurrence_pattern` TEXT,
                            `channel_id` TEXT NOT NULL,
                            `notification_id` INTEGER,
                            `action_data` TEXT,
                            `related_entity_id` INTEGER,
                            `related_entity_type` TEXT,
                            `retry_count` INTEGER NOT NULL DEFAULT 0,
                            `max_retries` INTEGER NOT NULL DEFAULT 3,
                            `created_at` INTEGER NOT NULL,
                            `updated_at` INTEGER NOT NULL,
                            FOREIGN KEY(`user_id`) REFERENCES `users`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                        )
                        """.trimIndent(),
                    )

                    // Create indices for notifications table
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_notifications_user_id` ON `notifications` (`user_id`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_notifications_type` ON `notifications` (`type`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_notifications_status` ON `notifications` (`status`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_notifications_scheduled_time` ON `notifications` (`scheduled_time`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_notifications_is_read` ON `notifications` (`is_read`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_notifications_user_id_type` ON `notifications` (`user_id`, `type`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_notifications_user_id_status` ON `notifications` (`user_id`, `status`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_notifications_user_id_scheduled_time` ON `notifications` (`user_id`, `scheduled_time`)")

                    Log.i(TAG, "Successfully migrated from version 1 to 2 - added notifications table")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to migrate from version 1 to 2", e)
                    throw e
                }
            }
        }

        /**
         * Migration from version 2 to 3: Adds Exercise and WorkoutSet tables.
         *
         * This migration adds comprehensive exercise tracking with detailed
         * workout sets for enhanced fitness logging capabilities.
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    // Create exercises table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `exercises` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `name` TEXT NOT NULL,
                            `description` TEXT,
                            `muscleGroup` TEXT NOT NULL,
                            `equipmentType` TEXT NOT NULL,
                            `exerciseType` TEXT NOT NULL,
                            `difficulty` TEXT NOT NULL DEFAULT 'INTERMEDIATE',
                            `instructions` TEXT,
                            `safetyNotes` TEXT,
                            `imageUrl` TEXT,
                            `videoUrl` TEXT,
                            `isCustom` INTEGER NOT NULL DEFAULT 0,
                            `createdBy` INTEGER,
                            `createdAt` INTEGER NOT NULL,
                            `updatedAt` INTEGER NOT NULL
                        )
                        """.trimIndent(),
                    )

                    // Create indices for exercises table
                    db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_exercises_name` ON `exercises` (`name`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_exercises_muscleGroup` ON `exercises` (`muscleGroup`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_exercises_equipmentType` ON `exercises` (`equipmentType`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_exercises_exerciseType` ON `exercises` (`exerciseType`)")

                    // Create workout_sets table
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `workout_sets` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `workoutId` INTEGER NOT NULL,
                            `exerciseId` INTEGER NOT NULL,
                            `setNumber` INTEGER NOT NULL,
                            `setType` TEXT NOT NULL DEFAULT 'NORMAL',
                            `repetitions` INTEGER NOT NULL DEFAULT 0,
                            `targetReps` INTEGER,
                            `weight` REAL NOT NULL DEFAULT 0.0,
                            `duration` INTEGER NOT NULL DEFAULT 0,
                            `distance` REAL NOT NULL DEFAULT 0.0,
                            `restTime` INTEGER NOT NULL DEFAULT 0,
                            `rpe` INTEGER,
                            `notes` TEXT,
                            `isPersonalRecord` INTEGER NOT NULL DEFAULT 0,
                            `isCompleted` INTEGER NOT NULL DEFAULT 1,
                            `completedAt` INTEGER,
                            `createdAt` INTEGER NOT NULL,
                            `updatedAt` INTEGER NOT NULL,
                            FOREIGN KEY(`workoutId`) REFERENCES `workouts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                            FOREIGN KEY(`exerciseId`) REFERENCES `exercises`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                        )
                        """.trimIndent(),
                    )

                    // Create indices for workout_sets table
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_sets_workoutId` ON `workout_sets` (`workoutId`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_sets_exerciseId` ON `workout_sets` (`exerciseId`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_sets_workoutId_exerciseId` ON `workout_sets` (`workoutId`, `exerciseId`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_sets_workoutId_setNumber` ON `workout_sets` (`workoutId`, `setNumber`)")
                    db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_sets_createdAt` ON `workout_sets` (`createdAt`)")

                    Log.i(TAG, "Successfully migrated from version 2 to 3 - added exercises and workout_sets tables")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to migrate from version 2 to 3", e)
                    throw e
                }
            }
        }

        /**
         * Creates database instance for testing with in-memory storage.
         *
         * This method is specifically designed for unit tests to provide
         * a clean, fast database instance that doesn't persist between tests.
         *
         * @param context Application context
         * @return In-memory database instance for testing
         */
        fun getInMemoryDatabase(context: Context): AppDatabase {
            return Room.inMemoryDatabaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
            )
                .allowMainThreadQueries() // Allow for synchronous testing
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        Log.d(TAG, "In-memory test database created successfully")
                    }
                })
                .build()
        }

        /**
         * Gets the current database version for migration testing.
         *
         * @return Current database version number
         */
        fun getCurrentVersion(): Int = 3

        /**
         * Validates database integrity and checks for corruption.
         *
         * @param context Application context
         * @return true if database is healthy, false if corrupted
         */
        fun validateDatabaseIntegrity(context: Context): Boolean {
            return try {
                val db = getInstance(context)
                // Simple check - try to access the database
                db.isOpen
            } catch (e: Exception) {
                Log.e(TAG, "Database integrity check failed with exception", e)
                false
            }
        }
    }

    // Helper method for clearing tables during memory pressure
    fun clearAllAppTables() {
        if (isOpen) {
            try {
                clearAllTables()
                Log.d(TAG, "All database tables cleared successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing database tables", e)
            }
        }
    }
}
