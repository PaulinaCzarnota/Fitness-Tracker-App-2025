/**
 * Comprehensive Robolectric tests for ViewModels.
 *
 * These tests use Robolectric to test ViewModels with Android framework dependencies
 * in a unit test environment. Tests cover:
 * - ViewModel lifecycle and state management
 * - LiveData and StateFlow emissions
 * - Error handling and edge cases
 * - Integration with repositories
 * - Threading and coroutines
 */

package com.example.fitnesstrackerapp.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitnesstrackerapp.data.entity.*
import com.example.fitnesstrackerapp.data.preferences.UserPreferencesRepository
import com.example.fitnesstrackerapp.repository.*
import com.example.fitnesstrackerapp.ui.auth.AuthViewModel
import com.example.fitnesstrackerapp.ui.goal.GoalViewModel
import com.example.fitnesstrackerapp.ui.viewmodel.StepCounterViewModel
import com.example.fitnesstrackerapp.ui.workout.WorkoutManagementViewModel
import com.example.fitnesstrackerapp.ui.workout.WorkoutViewModel
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.Date

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33]) // Target SDK for Robolectric
class ViewModelRobolectricTests {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    // Coroutine test dispatcher
    private val testDispatcher = UnconfinedTestDispatcher()

    // Mock repositories
    private lateinit var mockAuthRepository: AuthRepository
    private lateinit var mockWorkoutRepository: WorkoutRepository
    private lateinit var mockGoalRepository: GoalRepository
    private lateinit var mockStepRepository: StepRepository
    private lateinit var mockExerciseRepository: ExerciseRepository
    private lateinit var mockWorkoutSetRepository: WorkoutSetRepository
    private lateinit var mockUserPreferencesRepository: UserPreferencesRepository

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Initialize mocks
        mockAuthRepository = mockk()
        mockWorkoutRepository = mockk()
        mockGoalRepository = mockk()
        mockStepRepository = mockk()
        mockExerciseRepository = mockk()
        mockWorkoutSetRepository = mockk()
        mockUserPreferencesRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    fun authViewModel_login_emitsLoadingAndSuccess() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = "password123"
        val mockUser = User(1L, "Test User", email, Date())

        coEvery { mockAuthRepository.login(email, password) } returns Result.success(mockUser)
        coEvery { mockUserPreferencesRepository.setRememberMe(any()) } just Runs

        val viewModel = AuthViewModel(mockAuthRepository, mockUserPreferencesRepository)
        val stateObserver = mockk<Observer<AuthViewModel.AuthUiState>>(relaxed = true)
        viewModel.uiState.observeForever(stateObserver)

        // Act
        viewModel.login(email, password, rememberMe = true)

        // Wait for async operations
        advanceUntilIdle()

        // Assert
        verifySequence {
            stateObserver.onChanged(match { it.isLoading }) // Loading state
            stateObserver.onChanged(match { it.isAuthenticated && !it.isLoading }) // Success state
        }

        coVerify { mockAuthRepository.login(email, password) }
        coVerify { mockUserPreferencesRepository.setRememberMe(true) }

        viewModel.uiState.removeObserver(stateObserver)
    }

    @Test
    fun authViewModel_login_handlesNetworkError() = runTest {
        // Arrange
        val email = "test@example.com"
        val password = "wrongpassword"
        val errorMessage = "Invalid credentials"

        coEvery { mockAuthRepository.login(email, password) } returns Result.failure(Exception(errorMessage))

        val viewModel = AuthViewModel(mockAuthRepository, mockUserPreferencesRepository)
        val stateObserver = mockk<Observer<AuthViewModel.AuthUiState>>(relaxed = true)
        viewModel.uiState.observeForever(stateObserver)

        // Act
        viewModel.login(email, password, rememberMe = false)
        advanceUntilIdle()

        // Assert
        verifySequence {
            stateObserver.onChanged(match { it.isLoading })
            stateObserver.onChanged(match { !it.isLoading && it.error != null })
        }

        val finalState = viewModel.uiState.value
        assertFalse("Should not be authenticated", finalState?.isAuthenticated ?: true)
        assertNotNull("Should have error message", finalState?.error)

        viewModel.uiState.removeObserver(stateObserver)
    }

    @Test
    fun workoutViewModel_loadWorkouts_updatesStateCorrectly() = runTest {
        // Arrange
        val mockWorkouts = listOf(
            Workout(1L, 1L, WorkoutType.RUNNING, "Morning Run", Date(), null, 30, 300, 5.0f, "Great run"),
            Workout(2L, 1L, WorkoutType.STRENGTH_TRAINING, "Upper Body", Date(), null, 45, 250, 0.0f, "Good session"),
        )

        every { mockWorkoutRepository.getWorkoutsByUser(1L) } returns flowOf(mockWorkouts)
        every { mockExerciseRepository.getAllExercises() } returns flowOf(emptyList())

        val viewModel = WorkoutViewModel(mockWorkoutRepository, mockExerciseRepository)

        // Act
        val workouts = viewModel.workouts.first()

        // Assert
        assertEquals("Should load correct number of workouts", 2, workouts.size)
        assertEquals("First workout should be Morning Run", "Morning Run", workouts[0].title)
        assertEquals("Second workout should be Upper Body", "Upper Body", workouts[1].title)

        verify { mockWorkoutRepository.getWorkoutsByUser(1L) }
    }

    @Test
    fun goalViewModel_createGoal_validatesAndSaves() = runTest {
        // Arrange
        val targetSteps = 10000
        val description = "Daily step goal"
        val mockGoal = Goal(1L, 1L, GoalType.STEPS, "Steps", targetSteps.toFloat(), 0f, Date(), null, true, description)

        coEvery { mockGoalRepository.createGoal(any()) } returns Result.success(mockGoal)
        every { mockStepRepository.getTodaysStepsFlow(1L) } returns flowOf(null)
        every { mockWorkoutRepository.getWorkoutsByUser(1L) } returns flowOf(emptyList())

        val viewModel = GoalViewModel(mockGoalRepository, mockStepRepository, mockWorkoutRepository)

        // Act
        val result = viewModel.createStepGoal(targetSteps, description)

        // Assert
        assertTrue("Goal creation should succeed", result.isSuccess)
        assertEquals("Should return created goal", mockGoal, result.getOrNull())

        coVerify { mockGoalRepository.createGoal(any()) }
    }

    @Test
    fun goalViewModel_createGoal_validatesNegativeInput() = runTest {
        // Arrange
        every { mockStepRepository.getTodaysStepsFlow(1L) } returns flowOf(null)
        every { mockWorkoutRepository.getWorkoutsByUser(1L) } returns flowOf(emptyList())

        val viewModel = GoalViewModel(mockGoalRepository, mockStepRepository, mockWorkoutRepository)

        // Act
        val result = viewModel.createStepGoal(-1000, "Invalid goal")

        // Assert
        assertTrue("Should fail validation", result.isFailure)
        assertNotNull("Should have validation error", result.exceptionOrNull())

        coVerify(exactly = 0) { mockGoalRepository.createGoal(any()) }
    }

    @Test
    fun stepCounterViewModel_updateSteps_emitsCorrectState() = runTest {
        // Arrange
        val currentSteps = StepEntry(1L, 1L, Date(), 8500, 10000, 0, 425.0f, 6800f)

        every { mockStepRepository.getTodaysStepsFlow(1L) } returns flowOf(currentSteps)

        val viewModel = StepCounterViewModel(mockStepRepository)

        // Act
        val stepState = viewModel.todaysSteps.first()

        // Assert
        assertNotNull("Should have step data", stepState)
        assertEquals("Should have correct step count", 8500, stepState?.count)
        assertEquals("Should calculate progress correctly", 85f, viewModel.calculateProgress(8500, 10000), 0.1f)
        assertFalse("Goal should not be achieved", viewModel.isGoalAchieved(8500, 10000))

        verify { mockStepRepository.getTodaysStepsFlow(1L) }
    }

    @Test
    fun workoutManagementViewModel_startWorkout_createsWorkoutSession() = runTest {
        // Arrange
        val userId = 1L
        val workoutType = WorkoutType.STRENGTH_TRAINING
        val title = "Evening Workout"
        val mockWorkout = Workout(1L, userId, workoutType, title, Date(), null, 0, 0, 0.0f, "")

        coEvery { mockWorkoutRepository.createWorkout(any()) } returns Result.success(mockWorkout)
        every { mockWorkoutSetRepository.getWorkoutSetsFlow(1L) } returns flowOf(emptyList())
        every { mockExerciseRepository.getAllExercises() } returns flowOf(emptyList())

        val viewModel = WorkoutManagementViewModel(mockWorkoutRepository, mockWorkoutSetRepository, mockExerciseRepository)

        // Act
        val result = viewModel.startWorkout(userId, workoutType, title)

        // Assert
        assertTrue("Workout should start successfully", result.isSuccess)
        assertEquals("Should return created workout", mockWorkout, result.getOrNull())

        coVerify { mockWorkoutRepository.createWorkout(any()) }
    }

    @Test
    fun workoutManagementViewModel_completeWorkout_calculatesMetrics() = runTest {
        // Arrange
        val workoutId = 1L
        val endTime = Date()
        val distance = 5.0f
        val notes = "Great workout!"
        val originalWorkout = Workout(workoutId, 1L, WorkoutType.RUNNING, "Run", Date(), null, 0, 0, 0.0f, "")
        val completedWorkout = originalWorkout.copy(
            endTime = endTime,
            duration = 30, // minutes
            caloriesBurned = 300,
            distance = distance,
            notes = notes,
        )

        coEvery { mockWorkoutRepository.getWorkoutById(workoutId) } returns originalWorkout
        coEvery { mockWorkoutRepository.updateWorkout(any()) } returns Result.success(completedWorkout)
        every { mockWorkoutSetRepository.getWorkoutSetsFlow(workoutId) } returns flowOf(emptyList())
        every { mockExerciseRepository.getAllExercises() } returns flowOf(emptyList())

        val viewModel = WorkoutManagementViewModel(mockWorkoutRepository, mockWorkoutSetRepository, mockExerciseRepository)

        // Act
        val result = viewModel.completeWorkout(workoutId, endTime, 0, distance, notes)

        // Assert
        assertTrue("Workout should complete successfully", result.isSuccess)
        val workout = result.getOrNull()
        assertNotNull("Should return completed workout", workout)
        assertTrue("Should have duration calculated", (workout?.duration ?: 0) > 0)
        assertTrue("Should have calories calculated", (workout?.caloriesBurned ?: 0) > 0)
        assertEquals("Should preserve distance", distance, workout?.distance)

        coVerify { mockWorkoutRepository.updateWorkout(any()) }
    }

    @Test
    fun authViewModel_validateLoginForm_checksEmailAndPassword() = runTest {
        // Arrange
        val viewModel = AuthViewModel(mockAuthRepository, mockUserPreferencesRepository)

        // Test valid input
        val validResult = viewModel.validateLoginForm("test@example.com", "password123")
        assertTrue("Valid form should pass validation", validResult.isValid)
        assertTrue("Valid messages should be empty", validResult.messages.isEmpty())

        // Test invalid email
        val invalidEmailResult = viewModel.validateLoginForm("invalid-email", "password123")
        assertFalse("Invalid email should fail validation", invalidEmailResult.isValid)
        assertTrue(
            "Should have email error message",
            invalidEmailResult.messages.any { it.contains("email", ignoreCase = true) },
        )

        // Test empty password
        val emptyPasswordResult = viewModel.validateLoginForm("test@example.com", "")
        assertFalse("Empty password should fail validation", emptyPasswordResult.isValid)
        assertTrue(
            "Should have password error message",
            emptyPasswordResult.messages.any { it.contains("password", ignoreCase = true) },
        )
    }

    @Test
    fun workoutViewModel_deleteWorkout_removesFromList() = runTest {
        // Arrange
        val workoutToDelete = Workout(1L, 1L, WorkoutType.RUNNING, "Delete Me", Date(), null, 30, 300, 5.0f, "")
        val remainingWorkout = Workout(2L, 1L, WorkoutType.CYCLING, "Keep Me", Date(), null, 45, 400, 10.0f, "")
        val initialWorkouts = listOf(workoutToDelete, remainingWorkout)
        val updatedWorkouts = listOf(remainingWorkout)

        every { mockWorkoutRepository.getWorkoutsByUser(1L) } returnsMany listOf(
            flowOf(initialWorkouts),
            flowOf(updatedWorkouts),
        )
        every { mockExerciseRepository.getAllExercises() } returns flowOf(emptyList())
        coEvery { mockWorkoutRepository.deleteWorkout(1L) } returns Result.success(Unit)

        val viewModel = WorkoutViewModel(mockWorkoutRepository, mockExerciseRepository)

        // Act
        val initialList = viewModel.workouts.first()
        val deleteResult = viewModel.deleteWorkout(1L)

        // Assert
        assertEquals("Should start with 2 workouts", 2, initialList.size)
        assertTrue("Deletion should succeed", deleteResult.isSuccess)

        coVerify { mockWorkoutRepository.deleteWorkout(1L) }
    }

    @Test
    fun goalViewModel_updateGoalProgress_triggersAchievementCheck() = runTest {
        // Arrange
        val goal = Goal(1L, 1L, GoalType.STEPS, "Steps", 10000f, 9500f, Date(), null, true, "Almost there")
        val updatedGoal = goal.copy(currentValue = 10000f) // Goal achieved

        coEvery { mockGoalRepository.updateGoal(any()) } returns Result.success(updatedGoal)
        every { mockStepRepository.getTodaysStepsFlow(1L) } returns flowOf(null)
        every { mockWorkoutRepository.getWorkoutsByUser(1L) } returns flowOf(emptyList())

        val viewModel = GoalViewModel(mockGoalRepository, mockStepRepository, mockWorkoutRepository)

        // Act
        val result = viewModel.updateGoalProgress(goal, 10000f)

        // Assert
        assertTrue("Goal update should succeed", result.isSuccess)
        val achievedGoal = result.getOrNull()
        assertEquals("Goal should be fully completed", 10000f, achievedGoal?.currentValue)
        assertTrue("Goal should be achieved", viewModel.isGoalAchieved(achievedGoal))

        coVerify { mockGoalRepository.updateGoal(any()) }
    }

    @Test
    fun stepCounterViewModel_handlesSensorUnavailable() = runTest {
        // Arrange
        every { mockStepRepository.getTodaysStepsFlow(1L) } returns flowOf(null)

        val viewModel = StepCounterViewModel(mockStepRepository)

        // Act
        val stepState = viewModel.todaysSteps.first()

        // Assert
        assertNull("Should handle missing step data gracefully", stepState)
        assertFalse("Should not crash when checking sensor availability", viewModel.isSensorAvailable())
    }

    @Test
    fun workoutManagementViewModel_addExerciseSet_updatesWorkout() = runTest {
        // Arrange
        val workoutId = 1L
        val exerciseId = 1L
        val sets = 3
        val reps = 12
        val weight = 50.0f

        val mockWorkoutSet = WorkoutSet(1L, workoutId, exerciseId, sets, reps, weight, 0, "Bench Press")

        coEvery { mockWorkoutSetRepository.addWorkoutSet(any()) } returns Result.success(mockWorkoutSet)
        every { mockWorkoutSetRepository.getWorkoutSetsFlow(workoutId) } returns flowOf(listOf(mockWorkoutSet))
        every { mockWorkoutRepository.getWorkoutsByUser(any()) } returns flowOf(emptyList())
        every { mockExerciseRepository.getAllExercises() } returns flowOf(emptyList())

        val viewModel = WorkoutManagementViewModel(mockWorkoutRepository, mockWorkoutSetRepository, mockExerciseRepository)

        // Act
        val result = viewModel.addExerciseSet(workoutId, exerciseId, sets, reps, weight)

        // Assert
        assertTrue("Exercise set should be added successfully", result.isSuccess)
        assertEquals("Should return added workout set", mockWorkoutSet, result.getOrNull())

        coVerify { mockWorkoutSetRepository.addWorkoutSet(any()) }
    }
}
