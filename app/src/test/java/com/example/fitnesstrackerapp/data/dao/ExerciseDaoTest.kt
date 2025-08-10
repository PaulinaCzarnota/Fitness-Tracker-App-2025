package com.example.fitnesstrackerapp.data.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.fitnesstrackerapp.data.database.AppDatabase
import com.example.fitnesstrackerapp.data.entity.DifficultyLevel
import com.example.fitnesstrackerapp.data.entity.EquipmentType
import com.example.fitnesstrackerapp.data.entity.Exercise
import com.example.fitnesstrackerapp.data.entity.ExerciseType
import com.example.fitnesstrackerapp.data.entity.MuscleGroup
import com.example.fitnesstrackerapp.data.entity.User
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
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
@RunWith(JUnit4::class)
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
            AppDatabase::class.java,
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
            exerciseType = ExerciseType.STRENGTH,
            muscleGroup = MuscleGroup.CHEST,
            difficulty = DifficultyLevel.BEGINNER,
            description = "Classic push-up exercise",
            instructions = "Start in plank position, lower body, push up",
        )

        val exerciseId = exerciseDao.insertExercise(exercise)
        Assert.assertTrue("Exercise ID should be greater than 0", exerciseId > 0)

        val retrievedExercise = exerciseDao.getExerciseById(exerciseId)
        Assert.assertNotNull("Retrieved exercise should not be null", retrievedExercise)
        Assert.assertEquals("Push-ups", retrievedExercise?.name)
        Assert.assertEquals(ExerciseType.STRENGTH, retrievedExercise?.exerciseType)
        Assert.assertEquals(MuscleGroup.CHEST, retrievedExercise?.muscleGroup)
        Assert.assertEquals(DifficultyLevel.BEGINNER, retrievedExercise?.difficulty)
    }

    @Test
    fun getAllExercises() = runTest {
        val userId = createTestUser()

        exerciseDao.insertExercise(
            createTestExercise(createdBy = userId, name = "Push-ups", exerciseType = ExerciseType.STRENGTH),
        )
        exerciseDao.insertExercise(
            createTestExercise(createdBy = userId, name = "Running", exerciseType = ExerciseType.CARDIO),
        )
        exerciseDao.insertExercise(
            createTestExercise(createdBy = userId, name = "Squats", exerciseType = ExerciseType.STRENGTH),
        )

        val allExercises = exerciseDao.getAllExercises().first()
        Assert.assertEquals(3, allExercises.size)
        val exerciseNames = allExercises.map { it.name }
        Assert.assertTrue(exerciseNames.contains("Push-ups"))
        Assert.assertTrue(exerciseNames.contains("Running"))
        Assert.assertTrue(exerciseNames.contains("Squats"))
    }

    @Test
    fun getExercisesByType() = runTest {
        val userId = createTestUser()

        exerciseDao.insertExercise(
            createTestExercise(createdBy = userId, name = "Push-ups", exerciseType = ExerciseType.STRENGTH),
        )
        exerciseDao.insertExercise(
            createTestExercise(createdBy = userId, name = "Running", exerciseType = ExerciseType.CARDIO),
        )
        exerciseDao.insertExercise(
            createTestExercise(createdBy = userId, name = "Yoga", exerciseType = ExerciseType.FLEXIBILITY),
        )

        val strengthExercises = exerciseDao.getExercisesByType(ExerciseType.STRENGTH).first()
        Assert.assertEquals(1, strengthExercises.size)
        Assert.assertEquals("Push-ups", strengthExercises[0].name)

        val cardioExercises = exerciseDao.getExercisesByType(ExerciseType.CARDIO).first()
        Assert.assertEquals(1, cardioExercises.size)
        Assert.assertEquals("Running", cardioExercises[0].name)
    }

    @Test
    fun getExercisesByMuscleGroup() = runTest {
        val userId = createTestUser()

        exerciseDao.insertExercise(
            createTestExercise(
                createdBy = userId,
                name = "Push-ups",
                muscleGroup = MuscleGroup.CHEST,
            ),
        )
        exerciseDao.insertExercise(
            createTestExercise(
                createdBy = userId,
                name = "Bench Press",
                muscleGroup = MuscleGroup.CHEST,
            ),
        )
        exerciseDao.insertExercise(
            createTestExercise(
                createdBy = userId,
                name = "Pull-ups",
                muscleGroup = MuscleGroup.BACK,
            ),
        )
        exerciseDao.insertExercise(
            createTestExercise(
                createdBy = userId,
                name = "Squats",
                muscleGroup = MuscleGroup.LEGS,
            ),
        )

        val chestExercises = exerciseDao.getExercisesByMuscleGroup(MuscleGroup.CHEST).first()
        Assert.assertEquals(2, chestExercises.size)
        val chestExerciseNames = chestExercises.map { it.name }
        Assert.assertTrue(chestExerciseNames.contains("Push-ups"))
        Assert.assertTrue(chestExerciseNames.contains("Bench Press"))

        val backExercises = exerciseDao.getExercisesByMuscleGroup(MuscleGroup.BACK).first()
        Assert.assertEquals(1, backExercises.size)
        Assert.assertEquals("Pull-ups", backExercises[0].name)
    }

    @Test
    fun getExercisesByDifficulty() = runTest {
        val userId = createTestUser()

        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Push-ups", difficulty = DifficultyLevel.BEGINNER))
        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Pull-ups", difficulty = DifficultyLevel.INTERMEDIATE))
        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Muscle-ups", difficulty = DifficultyLevel.ADVANCED))

        val beginnerExercises = exerciseDao.getExercisesByDifficulty(DifficultyLevel.BEGINNER).first()
        Assert.assertEquals(1, beginnerExercises.size)
        Assert.assertEquals("Push-ups", beginnerExercises[0].name)

        val advancedExercises = exerciseDao.getExercisesByDifficulty(DifficultyLevel.ADVANCED).first()
        Assert.assertEquals(1, advancedExercises.size)
        Assert.assertEquals("Muscle-ups", advancedExercises[0].name)
    }

    @Test
    fun getCustomExercisesByUser() = runTest {
        val user1Id = createTestUser("user1@example.com")
        val user2Id = createTestUser("user2@example.com")

        exerciseDao.insertExercise(createTestExercise(createdBy = user1Id, name = "User1 Exercise", isCustom = true))
        exerciseDao.insertExercise(createTestExercise(createdBy = user2Id, name = "User2 Exercise", isCustom = true))
        exerciseDao.insertExercise(createTestExercise(createdBy = user1Id, name = "Predefined Exercise", isCustom = false))

        val user1Exercises = exerciseDao.getCustomExercisesByUser(user1Id).first()
        Assert.assertEquals(1, user1Exercises.size)
        Assert.assertEquals("User1 Exercise", user1Exercises[0].name)

        val user2Exercises = exerciseDao.getCustomExercisesByUser(user2Id).first()
        Assert.assertEquals(1, user2Exercises.size)
        Assert.assertEquals("User2 Exercise", user2Exercises[0].name)
    }

    @Test
    fun searchExercises() = runTest {
        val userId = createTestUser()

        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Push-ups"))
        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Diamond Push-ups"))
        exerciseDao.insertExercise(createTestExercise(createdBy = userId, name = "Squats"))

        val searchResults = exerciseDao.searchExercises("push").first()
        Assert.assertEquals(2, searchResults.size)
        val resultNames = searchResults.map { it.name }
        Assert.assertTrue(resultNames.contains("Push-ups"))
        Assert.assertTrue(resultNames.contains("Diamond Push-ups"))
    }

    @Test
    fun updateExercise() = runTest {
        val userId = createTestUser()
        val exercise = createTestExercise(createdBy = userId, name = "Push-ups")
        val exerciseId = exerciseDao.insertExercise(exercise)

        val retrievedExercise = exerciseDao.getExerciseById(exerciseId)
        Assert.assertNotNull(retrievedExercise)

        val updatedExercise = retrievedExercise!!.copy(
            name = "Wide Push-ups",
            description = "A wider variation of push-ups",
            difficulty = DifficultyLevel.INTERMEDIATE,
        )

        exerciseDao.updateExercise(updatedExercise)

        val finalExercise = exerciseDao.getExerciseById(exerciseId)
        Assert.assertNotNull(finalExercise)
        Assert.assertEquals("Wide Push-ups", finalExercise?.name)
        Assert.assertEquals("A wider variation of push-ups", finalExercise?.description)
        Assert.assertEquals(DifficultyLevel.INTERMEDIATE, finalExercise?.difficulty)
    }

    @Test
    fun deleteExercise() = runTest {
        val userId = createTestUser()
        val exercise = createTestExercise(createdBy = userId)
        val exerciseId = exerciseDao.insertExercise(exercise)

        var retrievedExercise = exerciseDao.getExerciseById(exerciseId)
        Assert.assertNotNull(retrievedExercise)

        exerciseDao.deleteExercise(retrievedExercise!!)

        retrievedExercise = exerciseDao.getExerciseById(exerciseId)
        Assert.assertNull(retrievedExercise)
    }

    @Test
    fun deleteExerciseById() = runTest {
        val userId = createTestUser()
        val exercise = createTestExercise(createdBy = userId)
        val exerciseId = exerciseDao.insertExercise(exercise)

        var retrievedExercise = exerciseDao.getExerciseById(exerciseId)
        Assert.assertNotNull(retrievedExercise)

        exerciseDao.deleteExerciseById(exerciseId)

        retrievedExercise = exerciseDao.getExerciseById(exerciseId)
        Assert.assertNull(retrievedExercise)
    }

    @Test
    fun deleteCustomExercisesByUser() = runTest {
        val user1Id = createTestUser("user1@example.com")
        val user2Id = createTestUser("user2@example.com")

        exerciseDao.insertExercise(createTestExercise(createdBy = user1Id, name = "User1 Ex1", isCustom = true))
        exerciseDao.insertExercise(createTestExercise(createdBy = user1Id, name = "User1 Ex2", isCustom = true))
        exerciseDao.insertExercise(createTestExercise(createdBy = user2Id, name = "User2 Ex1", isCustom = true))

        var user1Exercises = exerciseDao.getCustomExercisesByUser(user1Id).first()
        var user2Exercises = exerciseDao.getCustomExercisesByUser(user2Id).first()
        assertThat(user1Exercises).hasSize(2)
        assertThat(user2Exercises).hasSize(1)

        // Delete exercises for user1
        exerciseDao.deleteCustomExercisesByUser(user1Id)

        // Verify exercises are deleted for user1 but not for user2
        user1Exercises = exerciseDao.getCustomExercisesByUser(user1Id).first()
        user2Exercises = exerciseDao.getCustomExercisesByUser(user2Id).first()
        assertThat(user1Exercises).isEmpty()
        assertThat(user2Exercises).hasSize(1)
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
                createdAt = Date(baseTime - (3 * 24 * 60 * 60 * 1000L)), // 3 days ago
            ),
        )
        exerciseDao.insertExercise(
            createTestExercise(
                createdBy = userId,
                name = "Exercise2",
                createdAt = Date(baseTime - (1 * 24 * 60 * 60 * 1000L)), // 1 day ago
            ),
        )
        exerciseDao.insertExercise(
            createTestExercise(
                createdBy = userId,
                name = "Exercise3",
                createdAt = Date(baseTime), // Now
            ),
        )

        val recentExercises = exerciseDao.getRecentExercises(2).first()
        assertThat(recentExercises).hasSize(2)
        assertThat(recentExercises.map { it.name }).containsExactly("Exercise3", "Exercise2").inOrder()
    }

    @Test
    fun testForeignKeyConstraint() = runTest {
        val exercise = createTestExercise(
            createdBy = 999L, // Non-existent user
            name = "Invalid Exercise",
        )

        try {
            exerciseDao.insertExercise(exercise)
            Assert.fail("Should throw foreign key constraint exception")
        } catch (e: Exception) {
            // Expected foreign key constraint violation
            assertThat(e.message).contains("FOREIGN KEY")
        }
    }

    @Test
    fun testExerciseCascadeDelete() = runTest {
        val userId = createTestUser()
        val exercise = createTestExercise(createdBy = userId, name = "Test Exercise")
        exerciseDao.insertExercise(exercise)

        // Verify exercise exists
        var userExercises = exerciseDao.getCustomExercisesByUser(userId).first()
        assertThat(userExercises).hasSize(1)

        // Delete user (should cascade delete exercises)
        userDao.deleteUserById(userId)

        // Verify exercises are also deleted
        userExercises = exerciseDao.getCustomExercisesByUser(userId).first()
        assertThat(userExercises).isEmpty()
    }

    private suspend fun createTestUser(email: String = "test@example.com"): Long {
        val user = User(
            email = email,
            username = "testuser_${System.currentTimeMillis()}",
            passwordHash = "test_hash",
            passwordSalt = "test_salt",
            createdAt = Date(),
            updatedAt = Date(),
        )
        return userDao.insertUser(user)
    }

    private fun createTestExercise(
        name: String = "Test Exercise",
        muscleGroup: MuscleGroup = MuscleGroup.CHEST,
        equipmentType: EquipmentType = EquipmentType.BODYWEIGHT,
        exerciseType: ExerciseType = ExerciseType.STRENGTH,
        difficulty: DifficultyLevel = DifficultyLevel.INTERMEDIATE,
        description: String = "Test exercise description",
        instructions: String = "Test instructions",
        isCustom: Boolean = true,
        createdBy: Long? = 1L,
        createdAt: Date = Date(),
    ) = Exercise(
        name = name,
        muscleGroup = muscleGroup,
        equipmentType = equipmentType,
        exerciseType = exerciseType,
        difficulty = difficulty,
        description = description,
        instructions = instructions,
        isCustom = isCustom,
        createdBy = createdBy,
        createdAt = createdAt,
        updatedAt = Date(),
    )
}
