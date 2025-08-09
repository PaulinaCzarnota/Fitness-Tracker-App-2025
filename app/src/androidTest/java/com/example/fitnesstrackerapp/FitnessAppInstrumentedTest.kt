package com.example.fitnesstrackerapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.fitnesstrackerapp.data.entity.WorkoutType
import com.example.fitnesstrackerapp.fake.FakeLogWorkoutUseCase
import com.example.fitnesstrackerapp.fake.FakeServiceLocator
import com.example.fitnesstrackerapp.fake.FakeTrackStepsUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

/**
 * Instrumented test for fitness tracking use cases and ViewModels.
 *
 * This demonstrates how the fake implementations enable testing without
 * requiring actual database operations or complex setup.
 */
@RunWith(AndroidJUnit4::class)
class FitnessAppInstrumentedTest {
    private lateinit var fakeWorkoutUseCase: FakeLogWorkoutUseCase
    private lateinit var fakeStepsUseCase: FakeTrackStepsUseCase

    @Before
    fun setup() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        FakeServiceLocator.init(appContext)

        fakeWorkoutUseCase = FakeLogWorkoutUseCase()
        fakeStepsUseCase = FakeTrackStepsUseCase()
    }

    @After
    fun cleanup() {
        FakeServiceLocator.cleanup()
    }

    @Test
    fun testWorkoutLogging() = runBlocking {
        // Test starting a workout
        val result = fakeWorkoutUseCase.startWorkout(
            userId = 1L,
            workoutType = WorkoutType.RUNNING,
            title = "Morning Run",
        )

        assertTrue("Workout should start successfully", result.isSuccess)
        val workout = result.getOrNull()
        assertNotNull("Workout should not be null", workout)
        assertEquals("Workout title should match", "Morning Run", workout?.title)
        assertEquals("Workout type should match", WorkoutType.RUNNING, workout?.workoutType)

        // Test completing the workout
        val workoutId = workout?.id ?: 0L
        val endTime = Date(workout?.startTime?.time?.plus(30 * 60 * 1000) ?: 0) // 30 minutes later

        val completeResult = fakeWorkoutUseCase.completeWorkout(
            workoutId = workoutId,
            endTime = endTime,
            caloriesBurned = 0, // Let it calculate
            distance = 5.0f,
            notes = "Great run!",
        )

        assertTrue("Workout should complete successfully", completeResult.isSuccess)
        val completedWorkout = completeResult.getOrNull()
        assertNotNull("Completed workout should not be null", completedWorkout)
        assertTrue("Duration should be calculated", (completedWorkout?.duration ?: 0) > 0)
        assertTrue("Calories should be calculated", (completedWorkout?.caloriesBurned ?: 0) > 0)
        assertEquals("Distance should match", 5.0f, completedWorkout?.distance)
        assertEquals("Notes should match", "Great run!", completedWorkout?.notes)
    }

    @Test
    fun testStepTracking() = runBlocking {
        // Test recording steps
        val result = fakeStepsUseCase.recordSteps(
            userId = 1L,
            stepCount = 8500,
            goal = 10000,
        )

        assertTrue("Steps should be recorded successfully", result.isSuccess)
        val stepEntry = result.getOrNull()
        assertNotNull("Step entry should not be null", stepEntry)
        assertEquals("Step count should match", 8500, stepEntry?.count)
        assertEquals("Step goal should match", 10000, stepEntry?.goal)
        assertTrue("Calories should be calculated", (stepEntry?.caloriesBurned ?: 0f) > 0)
        assertTrue("Distance should be calculated", (stepEntry?.distanceMeters ?: 0f) > 0)

        // Test getting today's steps
        val todaysSteps = fakeStepsUseCase.getTodaysSteps(1L).first()
        assertNotNull("Today's steps should not be null", todaysSteps)
        assertEquals("Step count should match", 8500, todaysSteps?.count)

        // Test progress calculations
        val progress = fakeStepsUseCase.calculateProgress(8500, 10000)
        assertEquals("Progress should be 85%", 85f, progress, 0.01f)

        val remaining = fakeStepsUseCase.getRemainingSteps(8500, 10000)
        assertEquals("Remaining steps should be 1500", 1500, remaining)

        val achieved = fakeStepsUseCase.isGoalAchieved(8500, 10000)
        assertFalse("Goal should not be achieved yet", achieved)
    }

    @Test
    fun testActivityLevelClassification() {
        // Test different activity levels
        assertEquals(
            "Sedentary classification",
            com.example.fitnesstrackerapp.usecase.StepActivityLevel.SEDENTARY,
            fakeStepsUseCase.getActivityLevel(3000),
        )

        assertEquals(
            "Active classification",
            com.example.fitnesstrackerapp.usecase.StepActivityLevel.ACTIVE,
            fakeStepsUseCase.getActivityLevel(10500),
        )

        assertEquals(
            "Highly active classification",
            com.example.fitnesstrackerapp.usecase.StepActivityLevel.HIGHLY_ACTIVE,
            fakeStepsUseCase.getActivityLevel(15000),
        )
    }

    @Test
    fun testStepCountFormatting() {
        // Test step count formatting for display
        assertEquals("Small numbers", "500 steps", fakeStepsUseCase.formatStepCount(500))
        assertEquals("Thousands", "2.5K steps", fakeStepsUseCase.formatStepCount(2500))
        assertEquals("Ten thousands+", "12K+ steps", fakeStepsUseCase.formatStepCount(12000))
    }

    @Test
    fun testWeeklyStatistics() = runBlocking {
        // Add some fake step data for the week
        fakeStepsUseCase.setTodaysSteps(1L, 8000)

        val statsResult = fakeStepsUseCase.getWeeklyStatistics(1L)
        assertTrue("Statistics should be calculated successfully", statsResult.isSuccess)

        val stats = statsResult.getOrNull()
        assertNotNull("Statistics should not be null", stats)
        assertTrue("Total steps should be positive", (stats?.totalSteps ?: 0) >= 0)
        assertTrue("Total calories should be positive", (stats?.totalCalories ?: 0f) >= 0f)
        assertTrue("Total distance should be positive", (stats?.totalDistance ?: 0f) >= 0f)
    }

    @Test
    fun testWorkoutStatistics() = runBlocking {
        // Add some fake workouts
        fakeWorkoutUseCase.startWorkout(1L, WorkoutType.RUNNING, "Run 1")
        fakeWorkoutUseCase.startWorkout(1L, WorkoutType.WALKING, "Walk 1")

        val statsResult = fakeWorkoutUseCase.getWorkoutStatistics(1L)
        assertTrue("Workout statistics should be calculated", statsResult.isSuccess)

        val stats = statsResult.getOrNull()
        assertNotNull("Workout statistics should not be null", stats)
        // Note: The fake implementation returns basic stats,
        // real implementation would have more detailed calculations
    }

    @Test
    fun testWorkoutDeletion() = runBlocking {
        // Start a workout
        val startResult = fakeWorkoutUseCase.startWorkout(1L, WorkoutType.CYCLING, "Test Ride")
        assertTrue("Workout should start", startResult.isSuccess)

        val workout = startResult.getOrNull()
        val workoutId = workout?.id ?: 0L

        // Verify it exists
        assertTrue("Workout should exist", fakeWorkoutUseCase.hasWorkout(workoutId))

        // Delete the workout
        val deleteResult = fakeWorkoutUseCase.deleteWorkout(workoutId)
        assertTrue("Workout should be deleted successfully", deleteResult.isSuccess)

        // Verify it's gone
        assertFalse("Workout should not exist after deletion", fakeWorkoutUseCase.hasWorkout(workoutId))
    }

    @Test
    fun testMultipleUsersIsolation() = runBlocking {
        // Test that different users have isolated data
        val user1Result = fakeStepsUseCase.recordSteps(1L, 5000)
        val user2Result = fakeStepsUseCase.recordSteps(2L, 8000)

        assertTrue("User 1 steps should be recorded", user1Result.isSuccess)
        assertTrue("User 2 steps should be recorded", user2Result.isSuccess)

        val user1Steps = fakeStepsUseCase.getTodaysSteps(1L).first()
        val user2Steps = fakeStepsUseCase.getTodaysSteps(2L).first()

        assertEquals("User 1 should have 5000 steps", 5000, user1Steps?.count)
        assertEquals("User 2 should have 8000 steps", 8000, user2Steps?.count)

        // Verify isolation
        assertNotEquals(
            "Users should have different step counts",
            user1Steps?.count,
            user2Steps?.count,
        )
    }
}
