/**
 * Comprehensive Robolectric tests for Repositories.
 *
 * These tests use Robolectric with an in-memory Room database to test
 * repository implementations. Tests cover:
 * - Database operations (CRUD)
 * - Data consistency and integrity
 * - Error handling and edge cases
 * - Concurrent access scenarios
 * - Data flow and reactive streams
 */

package com.example.fitnesstrackerapp.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitnesstrackerapp.data.dao.*
import com.example.fitnesstrackerapp.data.database.FitnessDatabase
import com.example.fitnesstrackerapp.data.entity.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.Date
import java.util.concurrent.Executors

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class RepositoryRobolectricTests {

    private lateinit var database: FitnessDatabase
    private lateinit var userDao: UserDao
    private lateinit var workoutDao: WorkoutDao
    private lateinit var goalDao: GoalDao
    private lateinit var stepDao: StepDao
    private lateinit var exerciseDao: ExerciseDao
    private lateinit var workoutSetDao: WorkoutSetDao

    // Repository instances
    private lateinit var authRepository: AuthRepository
    private lateinit var workoutRepository: WorkoutRepository
    private lateinit var goalRepository: GoalRepository
    private lateinit var stepRepository: StepRepository
    private lateinit var exerciseRepository: ExerciseRepository
    private lateinit var workoutSetRepository: WorkoutSetRepository

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Create in-memory database for testing
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            FitnessDatabase::class.java,
        )
            .allowMainThreadQueries()
            .setTransactionExecutor(Executors.newSingleThreadExecutor())
            .build()

        // Get DAOs
        userDao = database.userDao()
        workoutDao = database.workoutDao()
        goalDao = database.goalDao()
        stepDao = database.stepDao()
        exerciseDao = database.exerciseDao()
        workoutSetDao = database.workoutSetDao()

        // Initialize repositories
        authRepository = AuthRepository(userDao)
        workoutRepository = WorkoutRepository(workoutDao)
        goalRepository = GoalRepository(goalDao)
        stepRepository = StepRepository(stepDao)
        exerciseRepository = ExerciseRepository(exerciseDao)
        workoutSetRepository = WorkoutSetRepository(workoutSetDao)
    }

    @After
    fun tearDown() {
        database.close()
        Dispatchers.resetMain()
    }

    // AuthRepository Tests
    @Test
    fun authRepository_registerUser_savesAndReturnsUser() = runTest {
        // Arrange
        val name = "John Doe"
        val email = "john@example.com"
        val password = "securePassword123"

        // Act
        val result = authRepository.register(name, email, password)

        // Assert
        assertTrue("Registration should succeed", result.isSuccess)
        val user = result.getOrNull()
        assertNotNull("User should not be null", user)
        assertEquals("Name should match", name, user?.name)
        assertEquals("Email should match", email, user?.email)
        assertTrue("User ID should be assigned", (user?.id ?: 0L) > 0L)
    }

    @Test
    fun authRepository_registerDuplicateEmail_fails() = runTest {
        // Arrange
        val email = "duplicate@example.com"
        authRepository.register("User 1", email, "password1")

        // Act
        val result = authRepository.register("User 2", email, "password2")

        // Assert
        assertTrue("Duplicate email registration should fail", result.isFailure)
        assertNotNull("Should have error message", result.exceptionOrNull())
    }

    @Test
    fun authRepository_loginValidCredentials_returnsUser() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = "testPassword"
        authRepository.register("Test User", email, password)

        // Act
        val result = authRepository.login(email, password)

        // Assert
        assertTrue("Login should succeed", result.isSuccess)
        val user = result.getOrNull()
        assertEquals("Email should match", email, user?.email)
    }

    @Test
    fun authRepository_loginInvalidCredentials_fails() = runTest {
        // Arrange
        val email = "test@example.com"
        authRepository.register("Test User", email, "correctPassword")

        // Act
        val result = authRepository.login(email, "wrongPassword")

        // Assert
        assertTrue("Login with wrong password should fail", result.isFailure)
    }

    // WorkoutRepository Tests
    @Test
    fun workoutRepository_createWorkout_savesSuccessfully() = runTest {
        // Arrange
        val workout = Workout(
            id = 0L,
            userId = 1L,
            workoutType = WorkoutType.RUNNING,
            title = "Morning Run",
            startTime = Date(),
            endTime = null,
            duration = 0,
            caloriesBurned = 0,
            distance = 0.0f,
            notes = "",
        )

        // Act
        val result = workoutRepository.createWorkout(workout)

        // Assert
        assertTrue("Workout creation should succeed", result.isSuccess)
        val savedWorkout = result.getOrNull()
        assertNotNull("Saved workout should not be null", savedWorkout)
        assertTrue("Workout should have assigned ID", (savedWorkout?.id ?: 0L) > 0L)
        assertEquals("Title should match", workout.title, savedWorkout?.title)
    }

    @Test
    fun workoutRepository_getUserWorkouts_returnsUserSpecificWorkouts() = runTest {
        // Arrange
        val user1Id = 1L
        val user2Id = 2L

        val user1Workout = Workout(0L, user1Id, WorkoutType.RUNNING, "User 1 Run", Date(), null, 30, 300, 5.0f, "")
        val user2Workout = Workout(0L, user2Id, WorkoutType.CYCLING, "User 2 Cycle", Date(), null, 45, 400, 10.0f, "")

        workoutRepository.createWorkout(user1Workout)
        workoutRepository.createWorkout(user2Workout)

        // Act
        val user1Workouts = workoutRepository.getWorkoutsByUser(user1Id).first()
        val user2Workouts = workoutRepository.getWorkoutsByUser(user2Id).first()

        // Assert
        assertEquals("User 1 should have 1 workout", 1, user1Workouts.size)
        assertEquals("User 2 should have 1 workout", 1, user2Workouts.size)
        assertEquals("User 1 workout title should match", "User 1 Run", user1Workouts[0].title)
        assertEquals("User 2 workout title should match", "User 2 Cycle", user2Workouts[0].title)
    }

    @Test
    fun workoutRepository_updateWorkout_modifiesExistingWorkout() = runTest {
        // Arrange
        val originalWorkout = Workout(0L, 1L, WorkoutType.RUNNING, "Run", Date(), null, 0, 0, 0.0f, "")
        val createResult = workoutRepository.createWorkout(originalWorkout)
        val savedWorkout = createResult.getOrNull()!!

        val updatedWorkout = savedWorkout.copy(
            endTime = Date(),
            duration = 30,
            caloriesBurned = 300,
            distance = 5.0f,
            notes = "Great run!",
        )

        // Act
        val updateResult = workoutRepository.updateWorkout(updatedWorkout)

        // Assert
        assertTrue("Update should succeed", updateResult.isSuccess)
        val updated = updateResult.getOrNull()
        assertNotNull("Updated workout should not be null", updated)
        assertEquals("Duration should be updated", 30, updated?.duration)
        assertEquals("Calories should be updated", 300, updated?.caloriesBurned)
        assertEquals("Distance should be updated", 5.0f, updated?.distance)
        assertEquals("Notes should be updated", "Great run!", updated?.notes)
    }

    @Test
    fun workoutRepository_deleteWorkout_removesFromDatabase() = runTest {
        // Arrange
        val workout = Workout(0L, 1L, WorkoutType.RUNNING, "Delete Me", Date(), null, 30, 300, 5.0f, "")
        val createResult = workoutRepository.createWorkout(workout)
        val workoutId = createResult.getOrNull()?.id!!

        // Act
        val deleteResult = workoutRepository.deleteWorkout(workoutId)

        // Assert
        assertTrue("Deletion should succeed", deleteResult.isSuccess)

        // Verify workout is gone
        val workouts = workoutRepository.getWorkoutsByUser(1L).first()
        assertTrue("Workout list should be empty after deletion", workouts.isEmpty())
    }

    // GoalRepository Tests
    @Test
    fun goalRepository_createGoal_savesWithCorrectFields() = runTest {
        // Arrange
        val goal = Goal(
            id = 0L,
            userId = 1L,
            type = GoalType.STEPS,
            title = "Daily Steps",
            targetValue = 10000f,
            currentValue = 0f,
            startDate = Date(),
            endDate = null,
            isActive = true,
            description = "Walk 10,000 steps daily",
        )

        // Act
        val result = goalRepository.createGoal(goal)

        // Assert
        assertTrue("Goal creation should succeed", result.isSuccess)
        val savedGoal = result.getOrNull()
        assertNotNull("Saved goal should not be null", savedGoal)
        assertEquals("Target value should match", 10000f, savedGoal?.targetValue)
        assertEquals("Goal type should match", GoalType.STEPS, savedGoal?.type)
        assertTrue("Goal should be active", savedGoal?.isActive ?: false)
    }

    @Test
    fun goalRepository_updateGoalProgress_modifiesCurrentValue() = runTest {
        // Arrange
        val goal = Goal(0L, 1L, GoalType.STEPS, "Steps", 10000f, 5000f, Date(), null, true, "Half way")
        val createResult = goalRepository.createGoal(goal)
        val savedGoal = createResult.getOrNull()!!

        val updatedGoal = savedGoal.copy(currentValue = 8500f)

        // Act
        val updateResult = goalRepository.updateGoal(updatedGoal)

        // Assert
        assertTrue("Goal update should succeed", updateResult.isSuccess)
        val updated = updateResult.getOrNull()
        assertEquals("Current value should be updated", 8500f, updated?.currentValue)
    }

    @Test
    fun goalRepository_getActiveGoals_returnsOnlyActiveGoals() = runTest {
        // Arrange
        val activeGoal = Goal(0L, 1L, GoalType.STEPS, "Active", 10000f, 0f, Date(), null, true, "Active goal")
        val inactiveGoal = Goal(0L, 1L, GoalType.WEIGHT, "Inactive", 70f, 0f, Date(), null, false, "Inactive goal")

        goalRepository.createGoal(activeGoal)
        goalRepository.createGoal(inactiveGoal)

        // Act
        val activeGoals = goalRepository.getActiveGoalsByUser(1L).first()

        // Assert
        assertEquals("Should have 1 active goal", 1, activeGoals.size)
        assertTrue("Goal should be active", activeGoals[0].isActive)
        assertEquals("Should be the correct goal", "Active", activeGoals[0].title)
    }

    // StepRepository Tests
    @Test
    fun stepRepository_recordTodaysSteps_savesCorrectly() = runTest {
        // Arrange
        val stepEntry = StepEntry(
            id = 0L,
            userId = 1L,
            date = Date(),
            count = 8500,
            goal = 10000,
            floors = 12,
            caloriesBurned = 425f,
            distanceMeters = 6800f,
        )

        // Act
        val result = stepRepository.recordTodaysSteps(stepEntry)

        // Assert
        assertTrue("Step recording should succeed", result.isSuccess)
        val saved = result.getOrNull()
        assertNotNull("Saved step entry should not be null", saved)
        assertEquals("Step count should match", 8500, saved?.count)
        assertEquals("Goal should match", 10000, saved?.goal)
        assertEquals("Calories should match", 425f, saved?.caloriesBurned)
    }

    @Test
    fun stepRepository_getTodaysSteps_returnsCurrentDayData() = runTest {
        // Arrange
        val today = Date()
        val stepEntry = StepEntry(0L, 1L, today, 5000, 10000, 8, 250f, 4000f)
        stepRepository.recordTodaysSteps(stepEntry)

        // Act
        val todaysSteps = stepRepository.getTodaysStepsFlow(1L).first()

        // Assert
        assertNotNull("Today's steps should not be null", todaysSteps)
        assertEquals("Step count should match", 5000, todaysSteps?.count)
        assertEquals("Goal should match", 10000, todaysSteps?.goal)
    }

    @Test
    fun stepRepository_updateStepCount_modifiesExistingEntry() = runTest {
        // Arrange
        val originalEntry = StepEntry(0L, 1L, Date(), 3000, 10000, 5, 150f, 2400f)
        val createResult = stepRepository.recordTodaysSteps(originalEntry)
        val savedEntry = createResult.getOrNull()!!

        val updatedEntry = savedEntry.copy(
            count = 7500,
            caloriesBurned = 375f,
            distanceMeters = 6000f,
        )

        // Act
        val updateResult = stepRepository.updateStepEntry(updatedEntry)

        // Assert
        assertTrue("Step update should succeed", updateResult.isSuccess)
        val updated = updateResult.getOrNull()
        assertEquals("Count should be updated", 7500, updated?.count)
        assertEquals("Calories should be updated", 375f, updated?.caloriesBurned)
        assertEquals("Distance should be updated", 6000f, updated?.distanceMeters)
    }

    // ExerciseRepository Tests
    @Test
    fun exerciseRepository_addExercise_savesWithMuscleGroups() = runTest {
        // Arrange
        val exercise = Exercise(
            id = 0L,
            name = "Bench Press",
            description = "Chest exercise",
            muscleGroups = listOf("Chest", "Triceps", "Shoulders"),
            equipment = "Barbell",
            instructions = listOf("Lie on bench", "Lower bar to chest", "Press up"),
            difficulty = Exercise.Difficulty.INTERMEDIATE,
        )

        // Act
        val result = exerciseRepository.addExercise(exercise)

        // Assert
        assertTrue("Exercise creation should succeed", result.isSuccess)
        val saved = result.getOrNull()
        assertNotNull("Saved exercise should not be null", saved)
        assertEquals("Name should match", "Bench Press", saved?.name)
        assertEquals("Muscle groups should match", 3, saved?.muscleGroups?.size)
        assertTrue("Should include chest", saved?.muscleGroups?.contains("Chest") ?: false)
        assertEquals("Difficulty should match", Exercise.Difficulty.INTERMEDIATE, saved?.difficulty)
    }

    @Test
    fun exerciseRepository_searchExercises_findsMatchingNames() = runTest {
        // Arrange
        val pushUp = Exercise(0L, "Push-up", "Bodyweight chest exercise", listOf("Chest"), "None", emptyList(), Exercise.Difficulty.BEGINNER)
        val pullUp = Exercise(0L, "Pull-up", "Bodyweight back exercise", listOf("Back"), "Pull-up bar", emptyList(), Exercise.Difficulty.ADVANCED)
        val pushPress = Exercise(0L, "Push Press", "Overhead press variation", listOf("Shoulders"), "Barbell", emptyList(), Exercise.Difficulty.INTERMEDIATE)

        exerciseRepository.addExercise(pushUp)
        exerciseRepository.addExercise(pullUp)
        exerciseRepository.addExercise(pushPress)

        // Act
        val searchResults = exerciseRepository.searchExercises("push").first()

        // Assert
        assertEquals("Should find 2 exercises with 'push'", 2, searchResults.size)
        val names = searchResults.map { it.name }
        assertTrue("Should include Push-up", names.contains("Push-up"))
        assertTrue("Should include Push Press", names.contains("Push Press"))
        assertFalse("Should not include Pull-up", names.contains("Pull-up"))
    }

    @Test
    fun exerciseRepository_getExercisesByMuscleGroup_filtersCorrectly() = runTest {
        // Arrange
        val chestExercise = Exercise(0L, "Bench Press", "", listOf("Chest"), "", emptyList(), Exercise.Difficulty.INTERMEDIATE)
        val backExercise = Exercise(0L, "Row", "", listOf("Back"), "", emptyList(), Exercise.Difficulty.INTERMEDIATE)
        val compoundExercise = Exercise(0L, "Deadlift", "", listOf("Back", "Legs"), "", emptyList(), Exercise.Difficulty.ADVANCED)

        exerciseRepository.addExercise(chestExercise)
        exerciseRepository.addExercise(backExercise)
        exerciseRepository.addExercise(compoundExercise)

        // Act
        val backExercises = exerciseRepository.getExercisesByMuscleGroup("Back").first()

        // Assert
        assertEquals("Should find 2 back exercises", 2, backExercises.size)
        val names = backExercises.map { it.name }
        assertTrue("Should include Row", names.contains("Row"))
        assertTrue("Should include Deadlift", names.contains("Deadlift"))
        assertFalse("Should not include Bench Press", names.contains("Bench Press"))
    }

    // WorkoutSetRepository Tests
    @Test
    fun workoutSetRepository_addWorkoutSet_savesWithExerciseDetails() = runTest {
        // Arrange
        val workoutSet = WorkoutSet(
            id = 0L,
            workoutId = 1L,
            exerciseId = 1L,
            sets = 3,
            reps = 12,
            weight = 80f,
            duration = 0,
            exerciseName = "Bench Press",
        )

        // Act
        val result = workoutSetRepository.addWorkoutSet(workoutSet)

        // Assert
        assertTrue("Workout set creation should succeed", result.isSuccess)
        val saved = result.getOrNull()
        assertNotNull("Saved workout set should not be null", saved)
        assertEquals("Sets should match", 3, saved?.sets)
        assertEquals("Reps should match", 12, saved?.reps)
        assertEquals("Weight should match", 80f, saved?.weight)
        assertEquals("Exercise name should match", "Bench Press", saved?.exerciseName)
    }

    @Test
    fun workoutSetRepository_getWorkoutSets_returnsWorkoutSpecificSets() = runTest {
        // Arrange
        val workout1Set = WorkoutSet(0L, 1L, 1L, 3, 12, 80f, 0, "Bench Press")
        val workout2Set = WorkoutSet(0L, 2L, 1L, 4, 8, 100f, 0, "Bench Press")
        val anotherWorkout1Set = WorkoutSet(0L, 1L, 2L, 3, 15, 20f, 0, "Bicep Curl")

        workoutSetRepository.addWorkoutSet(workout1Set)
        workoutSetRepository.addWorkoutSet(workout2Set)
        workoutSetRepository.addWorkoutSet(anotherWorkout1Set)

        // Act
        val workout1Sets = workoutSetRepository.getWorkoutSetsFlow(1L).first()

        // Assert
        assertEquals("Workout 1 should have 2 sets", 2, workout1Sets.size)
        val exerciseNames = workout1Sets.map { it.exerciseName }
        assertTrue("Should include Bench Press", exerciseNames.contains("Bench Press"))
        assertTrue("Should include Bicep Curl", exerciseNames.contains("Bicep Curl"))
    }

    @Test
    fun workoutSetRepository_updateWorkoutSet_modifiesRepsAndWeight() = runTest {
        // Arrange
        val originalSet = WorkoutSet(0L, 1L, 1L, 3, 10, 70f, 0, "Squat")
        val createResult = workoutSetRepository.addWorkoutSet(originalSet)
        val savedSet = createResult.getOrNull()!!

        val updatedSet = savedSet.copy(reps = 12, weight = 80f)

        // Act
        val updateResult = workoutSetRepository.updateWorkoutSet(updatedSet)

        // Assert
        assertTrue("Workout set update should succeed", updateResult.isSuccess)
        val updated = updateResult.getOrNull()
        assertEquals("Reps should be updated", 12, updated?.reps)
        assertEquals("Weight should be updated", 80f, updated?.weight)
        assertEquals("Sets should remain unchanged", 3, updated?.sets)
    }

    // Integration Tests
    @Test
    fun integration_completeWorkoutFlow_worksEndToEnd() = runTest {
        // Arrange - Create user, workout, and exercises
        val user = User(0L, "Test User", "test@example.com", Date())
        val userResult = authRepository.register(user.name, user.email, "password")
        val savedUser = userResult.getOrNull()!!

        val exercise = Exercise(0L, "Push-up", "Bodyweight exercise", listOf("Chest"), "None", emptyList(), Exercise.Difficulty.BEGINNER)
        val exerciseResult = exerciseRepository.addExercise(exercise)
        val savedExercise = exerciseResult.getOrNull()!!

        // Act - Create workout and add sets
        val workout = Workout(0L, savedUser.id, WorkoutType.STRENGTH_TRAINING, "Upper Body", Date(), null, 0, 0, 0f, "")
        val workoutResult = workoutRepository.createWorkout(workout)
        val savedWorkout = workoutResult.getOrNull()!!

        val workoutSet = WorkoutSet(0L, savedWorkout.id, savedExercise.id, 3, 20, 0f, 0, savedExercise.name)
        val setResult = workoutSetRepository.addWorkoutSet(workoutSet)

        // Complete workout
        val completedWorkout = savedWorkout.copy(
            endTime = Date(),
            duration = 25,
            caloriesBurned = 150,
        )
        val updateResult = workoutRepository.updateWorkout(completedWorkout)

        // Assert - Verify all operations succeeded
        assertTrue("Workout set should be added", setResult.isSuccess)
        assertTrue("Workout should be completed", updateResult.isSuccess)

        val finalWorkout = updateResult.getOrNull()
        assertNotNull("Completed workout should not be null", finalWorkout)
        assertTrue("Workout should have duration", (finalWorkout?.duration ?: 0) > 0)
        assertTrue("Workout should have calories", (finalWorkout?.caloriesBurned ?: 0) > 0)

        val workoutSets = workoutSetRepository.getWorkoutSetsFlow(savedWorkout.id).first()
        assertEquals("Workout should have 1 set", 1, workoutSets.size)
        assertEquals("Set should have correct exercise", savedExercise.name, workoutSets[0].exerciseName)
    }

    @Test
    fun integration_goalTrackingWithSteps_updatesProgress() = runTest {
        // Arrange - Create goal and step data
        val stepGoal = Goal(0L, 1L, GoalType.STEPS, "Daily Steps", 10000f, 0f, Date(), null, true, "Walk more")
        val goalResult = goalRepository.createGoal(stepGoal)
        val savedGoal = goalResult.getOrNull()!!

        // Act - Record steps and update goal progress
        val stepEntry = StepEntry(0L, 1L, Date(), 7500, 10000, 10, 375f, 6000f)
        val stepResult = stepRepository.recordTodaysSteps(stepEntry)

        val updatedGoal = savedGoal.copy(currentValue = 7500f)
        val progressResult = goalRepository.updateGoal(updatedGoal)

        // Assert - Verify goal progress tracking
        assertTrue("Steps should be recorded", stepResult.isSuccess)
        assertTrue("Goal progress should be updated", progressResult.isSuccess)

        val finalGoal = progressResult.getOrNull()
        assertEquals("Goal progress should reflect step count", 7500f, finalGoal?.currentValue)
        assertFalse("Goal should not be completed yet", (finalGoal?.currentValue ?: 0f) >= (finalGoal?.targetValue ?: 0f))

        val todaysSteps = stepRepository.getTodaysStepsFlow(1L).first()
        assertNotNull("Today's steps should be available", todaysSteps)
        assertEquals("Step count should match goal progress", finalGoal?.currentValue?.toInt(), todaysSteps?.count)
    }
}
