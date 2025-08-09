package com.example.fitnesstrackerapp.data.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitnesstrackerapp.data.database.AppDatabase
import com.example.fitnesstrackerapp.data.entity.Exercise
import com.example.fitnesstrackerapp.data.entity.User
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.runner.RunWith
import java.io.IOException
import java.util.*

/**
 * Comprehensive unit tests for ExerciseDao.
 *
 * Tests all exercise-related database operations including:
 * - CRUD operations (Create, Read, Update, Delete)
 * - Exercise filtering by type, muscle group, and difficulty
 * - Exercise library management
 * - User-specific exercise tracking
 * - Data integrity and constraints
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class ExerciseDaoTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AppDatabase
    private lateinit var exerciseDao: ExerciseDao
    private lateinit var userDao: UserDao

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        exerciseDao = database.exerciseDao()
        userDao = database.userDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndGetExercise() = runTest {
        val userId = createTestUser()
        val exercise = createTestExercise(
            createdBy = userId,
            name = "Push-ups",
            type = "Strength",
            muscleGroups = "Chest, Triceps",
            difficulty = "Beginner",
            description = "Classic push-up exercise",
            instructions = "Start in plank position, lower body, push up",
            caloriesPerMinute = 8.0f
        )

        val exerciseId = exerciseDao.insertExercise(exercise)
        assertThat(exerciseId).isGreaterThan(0)

        val retrievedExercise = exerciseDao.getExerciseById(exerciseId)
        assertThat(retrievedExercise).isNotNull()
        assertThat(retrievedExercise?.name).isEqualTo("Push-ups")
        assertThat(retrievedExercise?.type).isEqualTo("Strength")
        assertThat(retrievedExercise?.muscleGroups).isEqualTo("Chest, Triceps")
        assertThat(retrievedExercise?.difficulty).isEqualTo("Beginner")
        assertThat(retrievedExercise?.caloriesPerMinute).isWithin(0.1f).of(8.0f)
    }

    @Test
    fun getAllExercises() = runTest {
        val userId = createTestUser()

        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Push-ups", type = "Strength"))
        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Running", type = "Cardio"))
        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Squats", type = "Strength"))

        val allExercises = exerciseDao.getAllExercises().first()
        assertThat(allExercises).hasSize(3)
        assertThat(allExercises.map { it.name }).containsExactly("Push-ups", "Running", "Squats")
    }

    @Test
    fun getExercisesByType() = runTest {
        val userId = createTestUser()

        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Push-ups", type = "Strength"))
        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Running", type = "Cardio"))
        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Bench Press", type = "Strength"))
        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Cycling", type = "Cardio"))

        val strengthExercises = exerciseDao.getExercisesByType("Strength").first()
        assertThat(strengthExercises).hasSize(2)
        assertThat(strengthExercises.map { it.name }).containsExactly("Push-ups", "Bench Press")

        val cardioExercises = exerciseDao.getExercisesByType("Cardio").first()
        assertThat(cardioExercises).hasSize(2)
        assertThat(cardioExercises.map { it.name }).containsExactly("Running", "Cycling")
    }

    @Test
    fun getExercisesByMuscleGroup() = runTest {
        val userId = createTestUser()

        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Push-ups", muscleGroups = "Chest, Triceps"))
        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Pull-ups", muscleGroups = "Back, Biceps"))
        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Bench Press", muscleGroups = "Chest, Triceps"))
        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Squats", muscleGroups = "Legs, Glutes"))

        val chestExercises = exerciseDao.getExercisesByMuscleGroup("Chest").first()
        assertThat(chestExercises).hasSize(2)
        assertThat(chestExercises.map { it.name }).containsExactly("Push-ups", "Bench Press")

        val backExercises = exerciseDao.getExercisesByMuscleGroup("Back").first()
        assertThat(backExercises).hasSize(1)
        assertThat(backExercises[0].name).isEqualTo("Pull-ups")
    }

    @Test
    fun getExercisesByDifficulty() = runTest {
        val userId = createTestUser()

        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Push-ups", difficulty = "Beginner"))
        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Burpees", difficulty = "Advanced"))
        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Squats", difficulty = "Beginner"))
        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Pull-ups", difficulty = "Intermediate"))

        val beginnerExercises = exerciseDao.getExercisesByDifficulty("Beginner").first()
        assertThat(beginnerExercises).hasSize(2)
        assertThat(beginnerExercises.map { it.name }).containsExactly("Push-ups", "Squats")

        val advancedExercises = exerciseDao.getExercisesByDifficulty("Advanced").first()
        assertThat(advancedExercises).hasSize(1)
        assertThat(advancedExercises[0].name).isEqualTo("Burpees")
    }

    @Test
    fun getExercisesCreatedByUser() = runTest {
        val user1Id = createTestUser("user1@example.com")
        val user2Id = createTestUser("user2@example.com")

        exerciseDao.insertExercise(createTestExercise(createdBy = user1Id, name = "Custom Push-ups"))
        exerciseDao.insertExercise(createTestExercise(createdBy = user2Id, name = "Custom Squats"))
        exerciseDao.insertExercise(createTestExercise(createdBy = user1Id, name = "Custom Burpees"))

        val user1Exercises = exerciseDao.getExercisesCreatedByUser(user1Id).first()
        assertThat(user1Exercises).hasSize(2)
        assertThat(user1Exercises.map { it.name }).containsExactly("Custom Push-ups", "Custom Burpees")

        val user2Exercises = exerciseDao.getExercisesCreatedByUser(user2Id).first()
        assertThat(user2Exercises).hasSize(1)
        assertThat(user2Exercises[0].name).isEqualTo("Custom Squats")
    }

    @Test
    fun searchExercises() = runTest {
        val userId = createTestUser()

        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Push-ups"))
        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Pull-ups"))
        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Squats"))
        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Lunges"))

        val searchResults = exerciseDao.searchExercises("up").first()
        assertThat(searchResults).hasSize(2)
        assertThat(searchResults.map { it.name }).containsExactly("Push-ups", "Pull-ups")
    }

    @Test
    fun getExercisesByTypeAndMuscleGroup() = runTest {
        val userId = createTestUser()

        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Push-ups", type = "Strength", muscleGroups = "Chest, Triceps"))
        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Bench Press", type = "Strength", muscleGroups = "Chest, Triceps"))
        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Running", type = "Cardio", muscleGroups = "Legs, Cardio"))
        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Squats", type = "Strength", muscleGroups = "Legs, Glutes"))

        val strengthChestExercises = exerciseDao.getExercisesByTypeAndMuscleGroup("Strength", "Chest").first()
        assertThat(strengthChestExercises).hasSize(2)
        assertThat(strengthChestExercises.map { it.name }).containsExactly("Push-ups", "Bench Press")
    }

    @Test
    fun getExercisesByTypeAndDifficulty() = runTest {
        val userId = createTestUser()

        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Push-ups", type = "Strength", difficulty = "Beginner"))
        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Bench Press", type = "Strength", difficulty = "Intermediate"))
        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Running", type = "Cardio", difficulty = "Beginner"))
        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Squats", type = "Strength", difficulty = "Beginner"))

        val beginnerStrengthExercises = exerciseDao.getExercisesByTypeAndDifficulty("Strength", "Beginner").first()
        assertThat(beginnerStrengthExercises).hasSize(2)
        assertThat(beginnerStrengthExercises.map { it.name }).containsExactly("Push-ups", "Squats")
    }

    @Test
    fun updateExercise() = runTest {
        val userId = createTestUser()
        val exercise = createTestExercise(
            createdBy = userId,
            name = "Push-ups",
            description = "Original description"
        )

        val exerciseId = exerciseDao.insertExercise(exercise)

        val updatedExercise = exercise.copy(
            id = exerciseId,
            name = "Modified Push-ups",
            description = "Updated description",
            updatedAt = Date(System.currentTimeMillis() + 1000)
        )

        exerciseDao.updateExercise(updatedExercise)

        val retrievedExercise = exerciseDao.getExerciseById(exerciseId)
        assertThat(retrievedExercise?.name).isEqualTo("Modified Push-ups")
        assertThat(retrievedExercise?.description).isEqualTo("Updated description")
        assertThat(retrievedExercise?.updatedAt).isEqualTo(updatedExercise.updatedAt)
    }

    @Test
    fun deleteExercise() = runTest {
        val userId = createTestUser()
        val exercise = createTestExercise(createdBy = userId, name = "Push-ups")

        val exerciseId = exerciseDao.insertExercise(exercise)

        // Verify exercise exists
        var retrievedExercise = exerciseDao.getExerciseById(exerciseId)
        assertThat(retrievedExercise).isNotNull()

        // Delete exercise
        exerciseDao.deleteExercise(exercise.copy(id = exerciseId))

        // Verify exercise is deleted
        retrievedExercise = exerciseDao.getExerciseById(exerciseId)
        assertThat(retrievedExercise).isNull()
    }

    @Test
    fun deleteExerciseById() = runTest {
        val userId = createTestUser()
        val exercise = createTestExercise(createdBy = userId, name = "Push-ups")

        val exerciseId = exerciseDao.insertExercise(exercise)

        // Verify exercise exists
        var retrievedExercise = exerciseDao.getExerciseById(exerciseId)
        assertThat(retrievedExercise).isNotNull()

        // Delete exercise by ID
        exerciseDao.deleteExerciseById(exerciseId)

        // Verify exercise is deleted
        retrievedExercise = exerciseDao.getExerciseById(exerciseId)
        assertThat(retrievedExercise).isNull()
    }

    @Test
    fun deleteExercisesCreatedByUser() = runTest {
        val user1Id = createTestUser("user1@example.com")
        val user2Id = createTestUser("user2@example.com")

        exerciseDao.insertExercise(createTestExercise(createdBy = user1Id, name = "User1 Exercise1"))
        exerciseDao.insertExercise(createTestExercise(createdBy = user1Id, name = "User1 Exercise2"))
        exerciseDao.insertExercise(createTestExercise(createdBy = user2Id, name = "User2 Exercise1"))

        // Verify exercises exist
        var user1Exercises = exerciseDao.getExercisesCreatedByUser(user1Id).first()
        var user2Exercises = exerciseDao.getExercisesCreatedByUser(user2Id).first()
        assertThat(user1Exercises).hasSize(2)
        assertThat(user2Exercises).hasSize(1)

        // Delete user1's exercises
        exerciseDao.deleteExercisesCreatedByUser(user1Id)

        // Verify only user1's exercises are deleted
        user1Exercises = exerciseDao.getExercisesCreatedByUser(user1Id).first()
        user2Exercises = exerciseDao.getExercisesCreatedByUser(user2Id).first()
        assertThat(user1Exercises).isEmpty()
        assertThat(user2Exercises).hasSize(1)
    }

    @Test
    fun getExerciseCount() = runTest {
        val userId = createTestUser()

        assertThat(exerciseDao.getExerciseCount()).isEqualTo(0)

        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Push-ups"))
        assertThat(exerciseDao.getExerciseCount()).isEqualTo(1)

        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Squats"))
        assertThat(exerciseDao.getExerciseCount()).isEqualTo(2)
    }

    @Test
    fun getExerciseCountByType() = runTest {
        val userId = createTestUser()

        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Push-ups", type = "Strength"))
        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Squats", type = "Strength"))
        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Running", type = "Cardio"))

        assertThat(exerciseDao.getExerciseCountByType("Strength")).isEqualTo(2)
        assertThat(exerciseDao.getExerciseCountByType("Cardio")).isEqualTo(1)
        assertThat(exerciseDao.getExerciseCountByType("Flexibility")).isEqualTo(0)
    }

    @Test
    fun getExerciseCountByDifficulty() = runTest {
        val userId = createTestUser()

        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Push-ups", difficulty = "Beginner"))
        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Squats", difficulty = "Beginner"))
        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Pull-ups", difficulty = "Intermediate"))
        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Burpees", difficulty = "Advanced"))

        assertThat(exerciseDao.getExerciseCountByDifficulty("Beginner")).isEqualTo(2)
        assertThat(exerciseDao.getExerciseCountByDifficulty("Intermediate")).isEqualTo(1)
        assertThat(exerciseDao.getExerciseCountByDifficulty("Advanced")).isEqualTo(1)
    }

    @Test
    fun getDistinctExerciseTypes() = runTest {
        val userId = createTestUser()

        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Push-ups", type = "Strength"))
        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Squats", type = "Strength"))
        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Running", type = "Cardio"))
        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Yoga", type = "Flexibility"))

        val types = exerciseDao.getDistinctExerciseTypes()
        assertThat(types).containsExactly("Cardio", "Flexibility", "Strength")
    }

    @Test
    fun getDistinctDifficultyLevels() = runTest {
        val userId = createTestUser()

        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Push-ups", difficulty = "Beginner"))
        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Squats", difficulty = "Beginner"))
        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Pull-ups", difficulty = "Intermediate"))
        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Burpees", difficulty = "Advanced"))

        val difficulties = exerciseDao.getDistinctDifficultyLevels()
        assertThat(difficulties).containsExactly("Advanced", "Beginner", "Intermediate")
    }

    @Test
    fun getAverageCaloriesPerMinute() = runTest {
        val userId = createTestUser()

        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Push-ups", caloriesPerMinute = 8.0f))
        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Running", caloriesPerMinute = 12.0f))
        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Walking", caloriesPerMinute = 4.0f))

        val avgCalories = exerciseDao.getAverageCaloriesPerMinute()
        assertThat(avgCalories).isWithin(0.1f).of(8.0f) // (8 + 12 + 4) / 3
    }

    @Test
    fun getExercisesWithHighestCalories() = runTest {
        val userId = createTestUser()

        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Push-ups", caloriesPerMinute = 8.0f))
        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Running", caloriesPerMinute = 15.0f))
        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Walking", caloriesPerMinute = 4.0f))
        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "HIIT", caloriesPerMinute = 18.0f))
        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Yoga", caloriesPerMinute = 3.0f))

        val highCalorieExercises = exerciseDao.getExercisesWithHighestCalories(3).first()
        assertThat(highCalorieExercises).hasSize(3)
        assertThat(highCalorieExercises.map { it.name }).containsExactly("HIIT", "Running", "Push-ups")
        assertThat(highCalorieExercises.map { it.caloriesPerMinute }).containsExactly(18.0f, 15.0f, 8.0f)
    }

    @Test
    fun getRecentExercises() = runTest {
        val userId = createTestUser()
        val baseTime = System.currentTimeMillis()

        // Insert exercises with different creation dates
        exerciseDao.insertExercise(
            createTestExercise(
                createdBy = userId,
                name = "Exercise1",
                createdAt = Date(baseTime - (3 * 24 * 60 * 60 * 1000L)) // 3 days ago
            )
        )
        exerciseDao.insertExercise(
            createTestExercise(
                createdBy = userId,
                name = "Exercise2",
                createdAt = Date(baseTime - (1 * 24 * 60 * 60 * 1000L)) // 1 day ago
            )
        )
        exerciseDao.insertExercise(
            createTestExercise(
                createdBy = userId,
                name = "Exercise3",
                createdAt = Date(baseTime) // Now
            )
        )

        val recentExercises = exerciseDao.getRecentExercises(2).first()
        assertThat(recentExercises).hasSize(2)
        assertThat(recentExercises.map { it.name }).containsExactly("Exercise3", "Exercise2")
    }

    @Test
    fun testForeignKeyConstraint() = runTest {
        val exercise = createTestExercise(
            createdBy = 999L, // Non-existent user
            name = "Invalid Exercise"
        )

        try {
            exerciseDao.insertExercise(exercise)
            Assert.fail("Should throw foreign key constraint exception")
        } catch (e: Exception) {
            // Expected foreign key constraint violation
            assertThat(e.message).containsAnyOf("FOREIGN KEY", "constraint", "no such table")
        }
    }

    @Test
    fun testExerciseCascadeDelete() = runTest {
        val userId = createTestUser()
        val exercise = createTestExercise(createdBy = userId, name = "Test Exercise")
        exerciseDao.insertExercise(exercise)

        // Verify exercise exists
        var userExercises = exerciseDao.getExercisesCreatedByUser(userId).first()
        assertThat(userExercises).hasSize(1)

        // Delete user (should cascade delete exercises)
        userDao.deleteUserById(userId)

        // Verify exercises are also deleted
        userExercises = exerciseDao.getExercisesCreatedByUser(userId).first()
        assertThat(userExercises).isEmpty()
    }

    private suspend fun createTestUser(email: String = "test@example.com"): Long {
        val user = User(
            email = email,
            username = "testuser_${System.currentTimeMillis()}",
            passwordHash = "test_hash",
            passwordSalt = "test_salt",
            createdAt = Date(),
            updatedAt = Date()
        )
        return userDao.insertUser(user)
    }

    private fun createTestExercise(
        createdBy: Long,
        name: String = "Test Exercise",
        type: String = "Strength",
        muscleGroups: String = "Chest",
        difficulty: String = "Beginner",
        description: String = "Test exercise description",
        instructions: String = "Test instructions",
        caloriesPerMinute: Float = 8.0f,
        isPublic: Boolean = true,
        createdAt: Date = Date()
    ) = Exercise(
        name = name,
        type = type,
        muscleGroups = muscleGroups,
        difficulty = difficulty,
        description = description,
        instructions = instructions,
        caloriesPerMinute = caloriesPerMinute,
        isPublic = isPublic,
        createdBy = createdBy,
        createdAt = createdAt,
        updatedAt = Date()
    )
}
