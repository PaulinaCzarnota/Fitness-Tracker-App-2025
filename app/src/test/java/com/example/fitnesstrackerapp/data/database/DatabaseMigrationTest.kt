package com.example.fitnesstrackerapp.data.database

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.migration.Migration
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.example.fitnesstrackerapp.data.entity.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.runner.RunWith
import java.io.IOException
import java.util.*

/**
 * Comprehensive database migration tests for the Fitness Tracker application.
 *
 * This test suite validates all database migrations from version 1 to the current version,
 * ensuring data integrity and schema correctness across versions. It tests both forward
 * and backward compatibility scenarios where applicable.
 *
 * Key Testing Areas:
 * - Schema changes validation
 * - Data preservation during migrations
 * - Index and constraint validation
 * - Foreign key relationship maintenance
 * - Migration failure handling
 * - Performance impact assessment
 */
@ExperimentalCoroutinesApi
@RunWith(org.junit.runners.JUnit4::class)
class DatabaseMigrationTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val migrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
    )

    private lateinit var context: Context
    private val testDatabaseName = "migration_test.db"

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        // Cleanup is handled by MigrationTestHelper
    }

    // region Migration 1 to 2 Tests

    /**
     * Tests migration from version 1 to 2: Adding notifications table.
     *
     * This migration adds the comprehensive notifications table with all required columns,
     * indices, and foreign key constraints.
     */
    @Test
    fun testMigration1To2_AddNotificationsTable() = runTest {
        // Create database with version 1 schema
        val db = migrationTestHelper.createDatabase(testDatabaseName, 1)

        // Insert test data in version 1
        db.execSQL(
            """
            INSERT INTO users (id, email, username, password_hash, password_salt, created_at, updated_at)
            VALUES (1, 'test@example.com', 'testuser', 'hash', 'salt', ${System.currentTimeMillis()}, ${System.currentTimeMillis()})
        """,
        )

        db.close()

        // Run migration to version 2
        val migratedDb = migrationTestHelper.runMigrationsAndValidate(
            testDatabaseName,
            2,
            true,
            AppDatabase.MIGRATION_1_2,
        )

        // Verify notifications table exists with correct schema
        val cursor = migratedDb.query("SELECT sql FROM sqlite_master WHERE type='table' AND name='notifications'")
        Assert.assertTrue("Notifications table should exist", cursor.moveToFirst())
        val createTableSql = cursor.getString(0)

        // Verify essential columns exist
        Assert.assertTrue("Should have user_id column", createTableSql.contains("user_id"))
        Assert.assertTrue("Should have type column", createTableSql.contains("type"))
        Assert.assertTrue("Should have title column", createTableSql.contains("title"))
        Assert.assertTrue("Should have message column", createTableSql.contains("message"))
        Assert.assertTrue("Should have status column", createTableSql.contains("status"))
        Assert.assertTrue("Should have scheduled_time column", createTableSql.contains("scheduled_time"))

        cursor.close()

        // Verify foreign key constraint exists
        Assert.assertTrue(
            "Should have foreign key reference to users",
            createTableSql.contains("FOREIGN KEY(`user_id`) REFERENCES `users`(`id`)"),
        )

        // Verify indices were created
        val indexCursor = migratedDb.query("SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='notifications'")
        val indices = mutableListOf<String>()
        while (indexCursor.moveToNext()) {
            indices.add(indexCursor.getString(0))
        }
        indexCursor.close()

        Assert.assertTrue("Should have user_id index", indices.any { it.contains("user_id") })
        Assert.assertTrue("Should have type index", indices.any { it.contains("type") })
        Assert.assertTrue("Should have status index", indices.any { it.contains("status") })

        // Test insertion into new table
        migratedDb.execSQL(
            """
            INSERT INTO notifications (user_id, type, title, message, priority, status, scheduled_time, channel_id, created_at, updated_at)
            VALUES (1, 'WORKOUT_REMINDER', 'Test Notification', 'Test Message', 'DEFAULT', 'PENDING', ${System.currentTimeMillis()}, 'default', ${System.currentTimeMillis()}, ${System.currentTimeMillis()})
        """,
        )

        // Verify data was inserted successfully
        val notificationCursor = migratedDb.query("SELECT COUNT(*) FROM notifications")
        Assert.assertTrue("Should be able to query notifications", notificationCursor.moveToFirst())
        Assert.assertEquals("Should have one notification", 1, notificationCursor.getInt(0))
        notificationCursor.close()

        migratedDb.close()
    }

    /**
     * Tests that user data is preserved during migration 1 to 2.
     */
    @Test
    fun testMigration1To2_PreservesUserData() = runTest {
        val testEmail = "preserved@example.com"
        val testUsername = "preserved_user"
        val currentTime = System.currentTimeMillis()

        // Create database with version 1 and insert test data
        val db = migrationTestHelper.createDatabase(testDatabaseName, 1)
        db.execSQL(
            """
            INSERT INTO users (email, username, password_hash, password_salt, created_at, updated_at)
            VALUES ('$testEmail', '$testUsername', 'hash', 'salt', $currentTime, $currentTime)
        """,
        )
        db.close()

        // Run migration to version 2
        val migratedDb = migrationTestHelper.runMigrationsAndValidate(
            testDatabaseName,
            2,
            true,
            AppDatabase.MIGRATION_1_2,
        )

        // Verify user data is preserved
        val cursor = migratedDb.query("SELECT email, username FROM users WHERE email = '$testEmail'")
        Assert.assertTrue("User should exist after migration", cursor.moveToFirst())
        Assert.assertEquals("Email should be preserved", testEmail, cursor.getString(0))
        Assert.assertEquals("Username should be preserved", testUsername, cursor.getString(1))
        cursor.close()

        migratedDb.close()
    }

    // endregion

    // region Migration 2 to 3 Tests

    /**
     * Tests migration from version 2 to 3: Adding Exercise and WorkoutSet tables.
     */
    @Test
    fun testMigration2To3_AddExerciseAndWorkoutSetTables() = runTest {
        // Create database with version 2 schema
        val db = migrationTestHelper.createDatabase(testDatabaseName, 2)

        // Insert test data
        db.execSQL(
            """
            INSERT INTO users (id, email, username, password_hash, password_salt, created_at, updated_at)
            VALUES (1, 'test@example.com', 'testuser', 'hash', 'salt', ${System.currentTimeMillis()}, ${System.currentTimeMillis()})
        """,
        )

        val workoutStartTime = System.currentTimeMillis()
        db.execSQL(
            """
            INSERT INTO workouts (id, userId, workoutType, title, startTime, created_at, updated_at)
            VALUES (1, 1, 'RUNNING', 'Test Workout', $workoutStartTime, ${System.currentTimeMillis()}, ${System.currentTimeMillis()})
        """,
        )

        db.close()

        // Run migration to version 3
        val migratedDb = migrationTestHelper.runMigrationsAndValidate(
            testDatabaseName,
            3,
            true,
            AppDatabase.MIGRATION_2_3,
        )

        // Verify exercises table exists
        val exercisesCursor = migratedDb.query("SELECT sql FROM sqlite_master WHERE type='table' AND name='exercises'")
        Assert.assertTrue("Exercises table should exist", exercisesCursor.moveToFirst())
        val exercisesTableSql = exercisesCursor.getString(0)

        // Verify essential exercise columns
        Assert.assertTrue("Should have name column", exercisesTableSql.contains("name"))
        Assert.assertTrue("Should have muscleGroup column", exercisesTableSql.contains("muscleGroup"))
        Assert.assertTrue("Should have equipmentType column", exercisesTableSql.contains("equipmentType"))
        Assert.assertTrue("Should have difficulty column", exercisesTableSql.contains("difficulty"))

        exercisesCursor.close()

        // Verify workout_sets table exists
        val workoutSetsCursor = migratedDb.query("SELECT sql FROM sqlite_master WHERE type='table' AND name='workout_sets'")
        Assert.assertTrue("WorkoutSets table should exist", workoutSetsCursor.moveToFirst())
        val workoutSetsTableSql = workoutSetsCursor.getString(0)

        // Verify essential workout set columns
        Assert.assertTrue("Should have workoutId column", workoutSetsTableSql.contains("workoutId"))
        Assert.assertTrue("Should have exerciseId column", workoutSetsTableSql.contains("exerciseId"))
        Assert.assertTrue("Should have repetitions column", workoutSetsTableSql.contains("repetitions"))
        Assert.assertTrue("Should have weight column", workoutSetsTableSql.contains("weight"))

        workoutSetsCursor.close()

        // Verify foreign key constraints
        Assert.assertTrue(
            "Should have foreign key to workouts",
            workoutSetsTableSql.contains("FOREIGN KEY(`workoutId`) REFERENCES `workouts`(`id`)"),
        )
        Assert.assertTrue(
            "Should have foreign key to exercises",
            workoutSetsTableSql.contains("FOREIGN KEY(`exerciseId`) REFERENCES `exercises`(`id`)"),
        )

        // Test data insertion into new tables
        val exerciseInsertTime = System.currentTimeMillis()
        migratedDb.execSQL(
            """
            INSERT INTO exercises (name, description, muscleGroup, equipmentType, exerciseType, difficulty, createdAt, updatedAt)
            VALUES ('Push-up', 'Basic push-up exercise', 'CHEST', 'BODYWEIGHT', 'STRENGTH', 'BEGINNER', $exerciseInsertTime, $exerciseInsertTime)
        """,
        )

        migratedDb.execSQL(
            """
            INSERT INTO workout_sets (workoutId, exerciseId, setNumber, repetitions, weight, createdAt, updatedAt)
            VALUES (1, 1, 1, 10, 0.0, ${System.currentTimeMillis()}, ${System.currentTimeMillis()})
        """,
        )

        // Verify insertions were successful
        val exerciseCountCursor = migratedDb.query("SELECT COUNT(*) FROM exercises")
        Assert.assertTrue("Should be able to query exercises", exerciseCountCursor.moveToFirst())
        Assert.assertEquals("Should have one exercise", 1, exerciseCountCursor.getInt(0))
        exerciseCountCursor.close()

        val workoutSetCountCursor = migratedDb.query("SELECT COUNT(*) FROM workout_sets")
        Assert.assertTrue("Should be able to query workout sets", workoutSetCountCursor.moveToFirst())
        Assert.assertEquals("Should have one workout set", 1, workoutSetCountCursor.getInt(0))
        workoutSetCountCursor.close()

        migratedDb.close()
    }

    /**
     * Tests that existing data is preserved during migration 2 to 3.
     */
    @Test
    fun testMigration2To3_PreservesExistingData() = runTest {
        val testEmail = "migrate23@example.com"
        val workoutTitle = "Preserved Workout"
        val currentTime = System.currentTimeMillis()

        // Create database with version 2 and insert test data
        val db = migrationTestHelper.createDatabase(testDatabaseName, 2)

        db.execSQL(
            """
            INSERT INTO users (id, email, username, password_hash, password_salt, created_at, updated_at)
            VALUES (1, '$testEmail', 'testuser', 'hash', 'salt', $currentTime, $currentTime)
        """,
        )

        db.execSQL(
            """
            INSERT INTO workouts (id, userId, workoutType, title, startTime, created_at, updated_at)
            VALUES (1, 1, 'STRENGTH_TRAINING', '$workoutTitle', $currentTime, $currentTime, $currentTime)
        """,
        )

        db.execSQL(
            """
            INSERT INTO notifications (user_id, type, title, message, status, scheduled_time, channel_id, created_at, updated_at)
            VALUES (1, 'WORKOUT_REMINDER', 'Test Notification', 'Test Message', 'PENDING', $currentTime, 'default', $currentTime, $currentTime)
        """,
        )

        db.close()

        // Run migration to version 3
        val migratedDb = migrationTestHelper.runMigrationsAndValidate(
            testDatabaseName,
            3,
            true,
            AppDatabase.MIGRATION_2_3,
        )

        // Verify all existing data is preserved
        val userCursor = migratedDb.query("SELECT email FROM users WHERE email = '$testEmail'")
        Assert.assertTrue("User should exist after migration", userCursor.moveToFirst())
        Assert.assertEquals("Email should be preserved", testEmail, userCursor.getString(0))
        userCursor.close()

        val workoutCursor = migratedDb.query("SELECT title FROM workouts WHERE title = '$workoutTitle'")
        Assert.assertTrue("Workout should exist after migration", workoutCursor.moveToFirst())
        Assert.assertEquals("Workout title should be preserved", workoutTitle, workoutCursor.getString(0))
        workoutCursor.close()

        val notificationCursor = migratedDb.query("SELECT COUNT(*) FROM notifications WHERE user_id = 1")
        Assert.assertTrue("Notification should exist after migration", notificationCursor.moveToFirst())
        Assert.assertEquals("Should have one notification", 1, notificationCursor.getInt(0))
        notificationCursor.close()

        migratedDb.close()
    }

    // endregion

    // region Full Migration Path Tests

    /**
     * Tests complete migration path from version 1 to current version.
     */
    @Test
    fun testFullMigrationPath_1ToLatest() = runTest {
        val testEmail = "fullpath@example.com"
        val currentTime = System.currentTimeMillis()

        // Create database with version 1
        val db = migrationTestHelper.createDatabase(testDatabaseName, 1)
        db.execSQL(
            """
            INSERT INTO users (id, email, username, password_hash, password_salt, created_at, updated_at)
            VALUES (1, '$testEmail', 'fullpathuser', 'hash', 'salt', $currentTime, $currentTime)
        """,
        )
        db.close()

        // Run all migrations to current version
        val migratedDb = migrationTestHelper.runMigrationsAndValidate(
            testDatabaseName,
            AppDatabase.getCurrentVersion(),
            true,
            AppDatabase.MIGRATION_1_2,
            AppDatabase.MIGRATION_2_3,
        )

        // Verify final schema has all expected tables
        val tablesCursor = migratedDb.query("SELECT name FROM sqlite_master WHERE type='table'")
        val tables = mutableListOf<String>()
        while (tablesCursor.moveToNext()) {
            tables.add(tablesCursor.getString(0))
        }
        tablesCursor.close()

        val expectedTables = listOf("users", "workouts", "goals", "steps", "food_entries", "notifications", "exercises", "workout_sets")
        for (expectedTable in expectedTables) {
            Assert.assertTrue("Should have $expectedTable table", tables.contains(expectedTable))
        }

        // Verify original data is preserved
        val userCursor = migratedDb.query("SELECT email FROM users WHERE email = '$testEmail'")
        Assert.assertTrue("User should exist after full migration", userCursor.moveToFirst())
        Assert.assertEquals("Email should be preserved", testEmail, userCursor.getString(0))
        userCursor.close()

        // Test that we can insert into all new tables
        val insertTime = System.currentTimeMillis()

        // Test workout insertion
        migratedDb.execSQL(
            """
            INSERT INTO workouts (userId, workoutType, title, startTime, created_at, updated_at)
            VALUES (1, 'RUNNING', 'Full Path Test Workout', $insertTime, $insertTime, $insertTime)
        """,
        )

        // Test notification insertion
        migratedDb.execSQL(
            """
            INSERT INTO notifications (user_id, type, title, message, status, scheduled_time, channel_id, created_at, updated_at)
            VALUES (1, 'GOAL_ACHIEVEMENT', 'Full Path Test', 'Test Message', 'PENDING', $insertTime, 'default', $insertTime, $insertTime)
        """,
        )

        // Test exercise insertion
        migratedDb.execSQL(
            """
            INSERT INTO exercises (name, muscleGroup, equipmentType, exerciseType, difficulty, createdAt, updatedAt)
            VALUES ('Full Path Exercise', 'LEGS', 'BODYWEIGHT', 'CARDIO', 'INTERMEDIATE', $insertTime, $insertTime)
        """,
        )

        // Verify all insertions were successful
        val workoutCount = migratedDb.query("SELECT COUNT(*) FROM workouts").use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }
        Assert.assertEquals("Should have one workout", 1, workoutCount)

        val notificationCount = migratedDb.query("SELECT COUNT(*) FROM notifications").use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }
        Assert.assertEquals("Should have one notification", 1, notificationCount)

        val exerciseCount = migratedDb.query("SELECT COUNT(*) FROM exercises").use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }
        Assert.assertEquals("Should have one exercise", 1, exerciseCount)

        migratedDb.close()
    }

    // endregion

    // region Schema Validation Tests

    /**
     * Tests that the current database schema matches expected structure.
     */
    @Test
    fun testCurrentDatabaseSchema() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getInMemoryDatabase(context)

        try {
            // Verify all DAOs are accessible
            Assert.assertNotNull("UserDao should not be null", db.userDao())
            Assert.assertNotNull("WorkoutDao should not be null", db.workoutDao())
            Assert.assertNotNull("StepDao should not be null", db.stepDao())
            Assert.assertNotNull("GoalDao should not be null", db.goalDao())
            Assert.assertNotNull("FoodEntryDao should not be null", db.foodEntryDao())
            Assert.assertNotNull("NotificationDao should not be null", db.notificationDao())
            Assert.assertNotNull("ExerciseDao should not be null", db.exerciseDao())
            Assert.assertNotNull("WorkoutSetDao should not be null", db.workoutSetDao())

            // Test basic CRUD operations on all entities
            val currentTime = Date()

            // Test User entity
            val testUser = User(
                email = "schema@example.com",
                username = "schemauser",
                passwordHash = "hash",
                passwordSalt = "salt",
                createdAt = currentTime,
                updatedAt = currentTime,
            )
            val userId = db.userDao().insertUser(testUser)
            Assert.assertTrue("User ID should be positive", userId > 0)

            // Test Workout entity
            val testWorkout = Workout(
                userId = userId,
                workoutType = WorkoutType.RUNNING,
                title = "Schema Test Workout",
                startTime = currentTime,
                createdAt = currentTime,
                updatedAt = currentTime,
            )
            val workoutId = db.workoutDao().insertWorkout(testWorkout)
            Assert.assertTrue("Workout ID should be positive", workoutId > 0)

            // Test Step entity
            val testStep = Step(
                userId = userId,
                count = 5000,
                goal = 10000,
                date = currentTime,
                createdAt = currentTime,
                updatedAt = currentTime,
            )
            val stepId = db.stepDao().insertStep(testStep)
            Assert.assertTrue("Step ID should be positive", stepId > 0)

            // Test Goal entity
            val testGoal = Goal(
                userId = userId,
                title = "Schema Test Goal",
                goalType = GoalType.WEIGHT_LOSS,
                targetValue = 10.0,
                unit = "kg",
                targetDate = Date(currentTime.time + 86400000), // Tomorrow
                createdAt = currentTime,
                updatedAt = currentTime,
            )
            val goalId = db.goalDao().insertGoal(testGoal)
            Assert.assertTrue("Goal ID should be positive", goalId > 0)

            // Test FoodEntry entity
            val testFoodEntry = FoodEntry(
                userId = userId,
                foodName = "Schema Test Food",
                servingSize = 1.0,
                servingUnit = "piece",
                caloriesPerServing = 100.0,
                mealType = MealType.BREAKFAST,
                dateConsumed = currentTime,
                createdAt = currentTime,
                loggedAt = currentTime,
            )
            val foodEntryId = db.foodEntryDao().insertFoodEntry(testFoodEntry)
            Assert.assertTrue("FoodEntry ID should be positive", foodEntryId > 0)

            // Test Notification entity
            val testNotification = Notification(
                userId = userId,
                type = NotificationType.WORKOUT_REMINDER,
                title = "Schema Test Notification",
                message = "Test message",
                scheduledTime = currentTime,
                channelId = "test_channel",
                createdAt = currentTime,
                updatedAt = currentTime,
            )
            val notificationId = db.notificationDao().insertNotification(testNotification)
            Assert.assertTrue("Notification ID should be positive", notificationId > 0)

            // Test Exercise entity
            val testExercise = Exercise(
                name = "Push-up",
                description = "Basic bodyweight exercise",
                muscleGroup = MuscleGroup.CHEST,
                equipmentType = EquipmentType.BODYWEIGHT,
                exerciseType = ExerciseType.STRENGTH,
                difficulty = DifficultyLevel.INTERMEDIATE,
                createdAt = currentTime,
                updatedAt = currentTime,
            )
            val exerciseId = db.exerciseDao().insertExercise(testExercise)
            Assert.assertTrue("Exercise ID should be positive", exerciseId > 0)

            // Test WorkoutSet entity
            val testWorkoutSet = WorkoutSet(
                workoutId = workoutId,
                exerciseId = exerciseId,
                setNumber = 1,
                repetitions = 10,
                weight = 50.0, // Changed from 50.0f to 50.0 (Double instead of Float)
                createdAt = currentTime,
                updatedAt = currentTime,
            )
            val workoutSetId = db.workoutSetDao().insertWorkoutSet(testWorkoutSet)
            Assert.assertTrue("WorkoutSet ID should be positive", workoutSetId > 0)
        } finally {
            db.close()
        }
    }

    /**
     * Tests database integrity validation.
     */
    @Test
    fun testDatabaseIntegrityValidation() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Test integrity validation method
        val isValid = AppDatabase.validateDatabaseIntegrity(context)
        Assert.assertTrue("Database integrity should be valid", isValid)

        // Test current version retrieval
        val currentVersion = AppDatabase.getCurrentVersion()
        Assert.assertTrue("Current version should be positive", currentVersion > 0)
        Assert.assertEquals("Current version should be 3", 3, currentVersion)
    }

    // endregion

    // region Performance Tests

    /**
     * Tests migration performance to ensure migrations complete within reasonable time.
     */
    @Test(timeout = 10000) // 10 seconds timeout
    fun testMigrationPerformance() = runTest {
        val testRecords = 100

        // Create database with version 1 and insert test data
        val db = migrationTestHelper.createDatabase(testDatabaseName, 1)

        // Insert multiple users
        for (i in 1..testRecords) {
            db.execSQL(
                """
                INSERT INTO users (email, username, password_hash, password_salt, created_at, updated_at)
                VALUES ('test$i@example.com', 'user$i', 'hash$i', 'salt$i', ${System.currentTimeMillis()}, ${System.currentTimeMillis()})
            """,
            )
        }

        db.close()

        val startTime = System.currentTimeMillis()

        // Run all migrations
        val migratedDb = migrationTestHelper.runMigrationsAndValidate(
            testDatabaseName,
            AppDatabase.getCurrentVersion(),
            true,
            AppDatabase.MIGRATION_1_2,
            AppDatabase.MIGRATION_2_3,
        )

        val migrationTime = System.currentTimeMillis() - startTime

        // Verify all data is preserved
        val userCount = migratedDb.query("SELECT COUNT(*) FROM users").use { cursor ->
            cursor.moveToFirst()
            cursor.getInt(0)
        }

        Assert.assertEquals("All users should be preserved", testRecords, userCount)
        Assert.assertTrue("Migration should complete within 5 seconds", migrationTime < 5000)

        migratedDb.close()
    }

    // endregion

    // region Helper Methods

    /**
     * Verifies that a table exists with the expected columns.
     */
    private fun verifyTableSchema(db: SupportSQLiteDatabase, tableName: String, expectedColumns: List<String>) {
        val cursor = db.query("PRAGMA table_info($tableName)")
        val actualColumns = mutableListOf<String>()

        while (cursor.moveToNext()) {
            actualColumns.add(cursor.getString(1)) // Column name is at index 1
        }
        cursor.close()

        for (expectedColumn in expectedColumns) {
            Assert.assertTrue(
                "Table $tableName should have column $expectedColumn",
                actualColumns.contains(expectedColumn),
            )
        }
    }

    /**
     * Verifies that required indices exist for a table.
     */
    private fun verifyIndices(db: SupportSQLiteDatabase, tableName: String, expectedIndices: List<String>) {
        val cursor = db.query("SELECT name FROM sqlite_master WHERE type='index' AND tbl_name='$tableName'")
        val actualIndices = mutableListOf<String>()

        while (cursor.moveToNext()) {
            actualIndices.add(cursor.getString(0))
        }
        cursor.close()

        for (expectedIndex in expectedIndices) {
            Assert.assertTrue(
                "Table $tableName should have index containing $expectedIndex",
                actualIndices.any { it.contains(expectedIndex) },
            )
        }
    }

    // endregion

    // region Custom Migration Objects for Testing

    companion object {
        /**
         * Reference to migration 1 to 2 for testing purposes.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // This should match the actual migration logic in AppDatabase
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

                // Create indices
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_notifications_user_id` ON `notifications` (`user_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_notifications_type` ON `notifications` (`type`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_notifications_status` ON `notifications` (`status`)")
            }
        }

        /**
         * Reference to migration 2 to 3 for testing purposes.
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
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

                // Create indices
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_exercises_name` ON `exercises` (`name`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_sets_workoutId` ON `workout_sets` (`workoutId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_workout_sets_exerciseId` ON `workout_sets` (`exerciseId`)")
            }
        }
    }

    // endregion
}
