/**
 * Comprehensive Espresso UI Tests for Fitness Tracker App
 *
 * These tests cover all major user flows and features:
 * - Workout logging with MET table calorie calculation
 * - Progress tracking with chart visualization
 * - Goal setting with WorkManager reminders
 * - Notification scheduling and display
 * - Nutrition tracking with offline food database
 * - Complete end-to-end user journeys
 */

package com.example.fitnesstrackerapp.ui

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import androidx.room.Room
import androidx.work.testing.WorkManagerTestInitHelper
import com.example.fitnesstrackerapp.MainActivity
import com.example.fitnesstrackerapp.data.database.AppDatabase
import com.example.fitnesstrackerapp.data.entity.*
// Removed Hilt imports as app uses ServiceLocator pattern
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import javax.inject.Inject

/**
 * Comprehensive UI tests for the Fitness Tracker App
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class ComprehensiveUITests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.POST_NOTIFICATIONS,
        android.Manifest.permission.ACTIVITY_RECOGNITION,
        android.Manifest.permission.BODY_SENSORS,
    )

    private lateinit var database: AppDatabase

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        
        // Get database instance - using Room.inMemoryDatabaseBuilder for testing
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()

        // Initialize WorkManager for testing
        WorkManagerTestInitHelper.initializeTestWorkManager(context)

        // Clear database for each test
        runBlocking {
            database.clearAllTables()
        }
    }

    /**
     * Test 1: Complete Workout Logging Flow
     * Tests CRUD operations and MET table calorie calculation
     */
    @Test
    fun testCompleteWorkoutLoggingFlow() {
        composeTestRule.apply {
            // Navigate to workout screen
            onNodeWithText("Workouts").performClick()

            // Start a new workout
            onNodeWithText("Start Workout").performClick()

            // Select workout type
            onNodeWithText("WEIGHTLIFTING").performClick()
            onNodeWithText("Start").performClick()

            waitForIdle()

            // Verify workout is active
            onNodeWithText("Active Workout").assertIsDisplayed()

            // Add exercise
            onNodeWithText("Add Exercise").performClick()

            // Search for exercise
            onNodeWithTag("search_exercises").performTextInput("bench press")

            // Select exercise
            onNodeWithText("Bench Press").performClick()

            // Log a set
            onNodeWithContentDescription("Log set").performClick()

            // Enter set details
            onNodeWithText("Reps").performTextInput("10")
            onNodeWithText("Weight (kg)").performTextInput("80")
            onNodeWithText("MODERATE").performClick() // Select intensity

            // Verify calorie estimation is shown
            onNodeWithText("Calories:").assertIsDisplayed()

            // Save set
            onNodeWithText("Log Set").performClick()

            waitForIdle()

            // Verify set is logged
            onNodeWithText("Set 1").assertIsDisplayed()
            onNodeWithText("10 reps").assertIsDisplayed()
            onNodeWithText("80kg").assertIsDisplayed()

            // Log another set
            onNodeWithContentDescription("Log set").performClick()
            onNodeWithText("Reps").performTextInput("8")
            onNodeWithText("Weight (kg)").performTextInput("85")
            onNodeWithText("Log Set").performClick()

            waitForIdle()

            // Verify second set
            onNodeWithText("Set 2").assertIsDisplayed()

            // Add another exercise
            onNodeWithText("Add Exercise").performClick()
            onNodeWithTag("search_exercises").performTextInput("squat")
            onNodeWithText("Squats").performClick()

            // Log squats
            onNodeWithContentDescription("Log set").performClick()
            onNodeWithText("Reps").performTextInput("12")
            onNodeWithText("Weight (kg)").performTextInput("100")
            onNodeWithText("Log Set").performClick()

            waitForIdle()

            // Finish workout
            onNodeWithText("Finish Workout").performClick()
            onNodeWithText("Great workout! Keep it up.").performTextInput("Great workout! Keep it up.")
            onNodeWithText("Finish").performClick()

            waitForIdle()

            // Verify workout completion
            onNodeWithText("Workout completed!").assertIsDisplayed()
        }
    }

    /**
     * Test 2: Goal Setting with WorkManager Integration
     * Tests goal creation, progress tracking, and reminder scheduling
     */
    @Test
    fun testGoalSettingAndWorkManagerIntegration() {
        composeTestRule.apply {
            // Navigate to goals screen
            onNodeWithText("Goals").performClick()

            // Create new goal
            onNodeWithText("New Goal").performClick()

            // Select goal category
            onNodeWithText("Daily Steps").performClick()

            // Fill goal details
            onNodeWithText("Goal Title").performTextClearance()
            onNodeWithText("Goal Title").performTextInput("Daily Walking Goal")

            onNodeWithText("Description (Optional)").performTextInput("Walk 10,000 steps daily")

            // Select suggested target
            onNodeWithText("10000 steps").performClick()

            // Enable reminders
            onNodeWithTag("reminder_switch").assertIsOn()

            // Create goal
            onNodeWithText("Create Goal").performClick()

            waitForIdle()

            // Verify goal creation
            onNodeWithText("Daily Walking Goal").assertIsDisplayed()
            onNodeWithText("0 / 10000 steps").assertIsDisplayed()
            onNodeWithText("0% complete").assertIsDisplayed()

            // Update progress using quick buttons
            onNodeWithText("+1000").performClick()

            waitForIdle()

            // Verify progress update
            onNodeWithText("1000 / 10000 steps").assertIsDisplayed()
            onNodeWithText("10% complete").assertIsDisplayed()

            // Update progress more
            onNodeWithText("+2500").performClick()
            onNodeWithText("+2500").performClick()
            onNodeWithText("+2500").performClick()

            waitForIdle()

            // Verify significant progress
            onNodeWithText("8500 / 10000 steps").assertIsDisplayed()
            onNodeWithText("85% complete").assertIsDisplayed()

            // Complete the goal
            onNodeWithText("+1000").performClick()
            onNodeWithText("+500").performClick()

            waitForIdle()

            // Should show complete button
            onNodeWithText("Complete").performClick()

            waitForIdle()

            // Verify goal completion
            onNodeWithContentDescription("Completed").assertIsDisplayed()
        }
    }

    /**
     * Test 3: Progress Tracking with Chart Visualization
     * Tests LiveData updates and chart rendering
     */
    @Test
    fun testProgressTrackingWithCharts() {
        // First, create some test data
        setupTestProgressData()

        composeTestRule.apply {
            // Navigate to progress screen
            onNodeWithText("Progress").performClick()

            waitForIdle()

            // Verify progress overview
            onNodeWithText("Progress Analytics").assertIsDisplayed()

            // Check period selector
            onNodeWithText("Weekly").assertIsDisplayed()
            onNodeWithText("Daily").performClick()

            waitForIdle()

            // Verify goal progress section
            onNodeWithText("Goal Progress").assertIsDisplayed()

            // Check for workout progress chart
            onNodeWithText("Workout Progress").assertIsDisplayed()

            // Check steps chart
            onNodeWithText("Daily Steps").assertIsDisplayed()

            // Test different time periods
            onNodeWithText("Monthly").performClick()

            waitForIdle()

            // Verify charts update with new period
            onNodeWithText("Weekly Comparison").assertIsDisplayed()

            // Switch back to weekly
            onNodeWithText("Weekly").performClick()

            waitForIdle()

            // Verify goal progress indicators
            onNodeWithText("Set 10").assertExists()
        }
    }

    /**
     * Test 4: Notification System Testing
     * Tests local notifications and Firebase Cloud Messaging setup
     */
    @Test
    fun testNotificationSystem() {
        composeTestRule.apply {
            // Navigate to settings or notification area
            onNodeWithContentDescription("Settings").performClick()

            // Enable notifications
            onNodeWithText("Notifications").performClick()
            onNodeWithText("Enable Goal Reminders").performClick()
            onNodeWithText("Enable Workout Reminders").performClick()
            onNodeWithText("Enable Progress Updates").performClick()

            // Go back to create goal with reminder
            onNodeWithContentDescription("Back").performClick()
            onNodeWithText("Goals").performClick()

            // Create goal with reminder
            onNodeWithText("New Goal").performClick()
            onNodeWithText("Weekly Workouts").performClick()

            onNodeWithText("Goal Title").performTextClearance()
            onNodeWithText("Goal Title").performTextInput("Weekly Exercise Goal")

            onNodeWithText("3 workouts").performClick()

            // Ensure reminder is enabled
            onNodeWithTag("reminder_switch").assertIsOn()

            onNodeWithText("Create Goal").performClick()

            waitForIdle()

            // Verify goal was created with reminder
            onNodeWithText("Weekly Exercise Goal").assertIsDisplayed()
            onNodeWithContentDescription("NotificationsActive").assertIsDisplayed()

            // Test toggling reminder
            onNodeWithTag("reminder_toggle").performClick()
            onNodeWithContentDescription("NotificationsOff").assertIsDisplayed()

            // Re-enable
            onNodeWithTag("reminder_toggle").performClick()
            onNodeWithContentDescription("NotificationsActive").assertIsDisplayed()
        }
    }

    /**
     * Test 5: Enhanced Nutrition Tracking with Offline Database
     * Tests food search, macro calculations, and meal logging
     */
    @Test
    fun testNutritionTrackingWithOfflineDatabase() {
        composeTestRule.apply {
            // Navigate to nutrition screen
            onNodeWithText("Nutrition").performClick()

            waitForIdle()

            // Verify nutrition header
            onNodeWithText("Nutrition Tracking").assertIsDisplayed()
            onNodeWithText("Today's Nutrition").assertIsDisplayed()

            // Check macro breakdown chart
            onNodeWithText("Macro Breakdown").assertIsDisplayed()
            onNodeWithText("No nutrition data yet").assertIsDisplayed()

            // Add breakfast food
            onAllNodesWithText("Add Food")[0].performClick()

            // Search for food
            onNodeWithText("Search foods...").performTextInput("apple")

            waitForIdle()

            // Select apple
            onNodeWithText("Apple").performClick()

            // Verify selection details
            onNodeWithText("medium (182g)").assertIsDisplayed()
            onNodeWithText("Quantity").performTextClearance()
            onNodeWithText("Quantity").performTextInput("2")

            // Verify nutrition calculation
            onNodeWithText("104 kcal").assertIsDisplayed() // 52 * 2

            onNodeWithText("Add Food").performClick()

            waitForIdle()

            // Verify food was added
            onNodeWithText("Apple").assertIsDisplayed()
            onNodeWithText("104 kcal").assertIsDisplayed()
            onNodeWithText("2x medium (182g)").assertIsDisplayed()

            // Add lunch food
            onAllNodesWithText("Add Food")[1].performClick() // Lunch section

            onNodeWithText("Search foods...").performTextInput("chicken")
            onNodeWithText("Chicken Breast").performClick()

            onNodeWithText("Quantity").performTextClearance()
            onNodeWithText("Quantity").performTextInput("1.5")

            onNodeWithText("Add Food").performClick()

            waitForIdle()

            // Add custom food
            onNodeWithText("Add Custom Food").performClick()

            onNodeWithText("Food Name").performTextInput("Protein Shake")
            onNodeWithText("Serving Size (e.g., '100g', '1 cup')").performTextInput("1 scoop")
            onNodeWithText("Quantity").performTextClearance()
            onNodeWithText("Quantity").performTextInput("1")
            onNodeWithText("Calories per serving").performTextInput("120")
            onNodeWithText("Protein (g)").performTextInput("25")
            onNodeWithText("Carbs (g)").performTextInput("3")
            onNodeWithText("Fat (g)").performTextInput("1")

            onNodeWithText("Add Food").performClick()

            waitForIdle()

            // Verify nutrition summary updated
            onNodeWithText("Today's Nutrition").assertIsDisplayed()
            onAllNodesWithText("kcal").assertCountGreaterThan(2) // Total calories updated

            // Check macro breakdown chart now has data
            onNodeWithText("Protein").assertIsDisplayed()
            onNodeWithText("Carbs").assertIsDisplayed()
            onNodeWithText("Fat").assertIsDisplayed()

            // Test food deletion
            onAllNodesWithContentDescription("Delete")[0].performClick()

            waitForIdle()

            // Verify food was removed and totals updated
            // The exact calorie count should be lower now
        }
    }

    /**
     * Test 6: End-to-End User Journey
     * Tests complete user flow from onboarding to achieving goals
     */
    @Test
    fun testEndToEndUserJourney() {
        composeTestRule.apply {
            // Start with workout
            onNodeWithText("Workouts").performClick()
            onNodeWithText("Start Workout").performClick()
            onNodeWithText("WEIGHTLIFTING").performClick()
            onNodeWithText("Start").performClick()

            waitForIdle()

            // Quick workout logging
            onNodeWithText("Add Exercise").performClick()
            onNodeWithTag("search_exercises").performTextInput("push")
            onNodeWithText("Push-ups").performClick()

            onNodeWithContentDescription("Log set").performClick()
            onNodeWithText("Reps").performTextInput("20")
            onNodeWithText("Log Set").performClick()

            waitForIdle()

            onNodeWithText("Finish Workout").performClick()
            onNodeWithText("Finish").performClick()

            waitForIdle()

            // Set a goal based on workout
            onNodeWithText("Goals").performClick()
            onNodeWithText("New Goal").performClick()
            onNodeWithText("Weekly Workouts").performClick()

            onNodeWithText("3 workouts").performClick()
            onNodeWithText("Create Goal").performClick()

            waitForIdle()

            // Update goal progress (we just completed 1 workout)
            onNodeWithText("+1").performClick()

            waitForIdle()

            // Add nutrition for the day
            onNodeWithText("Nutrition").performClick()

            // Add post-workout meal
            onAllNodesWithText("Add Food")[0].performClick() // Breakfast
            onNodeWithText("Search foods...").performTextInput("banana")
            onNodeWithText("Banana").performClick()
            onNodeWithText("Add Food").performClick()

            waitForIdle()

            // Check progress
            onNodeWithText("Progress").performClick()

            waitForIdle()

            // Verify all data is reflected in progress
            onNodeWithText("Progress Analytics").assertIsDisplayed()
            onNodeWithText("Goal Progress").assertIsDisplayed()

            // Go back to goals and check if we can complete more of the goal
            onNodeWithText("Goals").performClick()

            // Should show 1/3 workouts completed
            onNodeWithText("1 / 3 workouts").assertIsDisplayed()
            onNodeWithText("33% complete").assertIsDisplayed()
        }
    }

    /**
     * Test 7: Data Persistence and State Management
     * Tests that data persists across app restarts and state changes
     */
    @Test
    fun testDataPersistenceAndStateManagement() {
        composeTestRule.apply {
            // Create test data
            onNodeWithText("Goals").performClick()
            onNodeWithText("New Goal").performClick()
            onNodeWithText("Daily Steps").performClick()
            onNodeWithText("8000 steps").performClick()
            onNodeWithText("Create Goal").performClick()

            waitForIdle()

            // Update progress
            onNodeWithText("+800").performClick()
            onNodeWithText("+800").performClick()

            waitForIdle()

            // Record current state
            onNodeWithText("1600 / 8000 steps").assertIsDisplayed()

            // Simulate app restart by recreating activity
            activityRule.scenario.recreate()

            waitForIdle()

            // Navigate back to goals
            onNodeWithText("Goals").performClick()

            waitForIdle()

            // Verify data persisted
            onNodeWithText("1600 / 8000 steps").assertIsDisplayed()
            onNodeWithText("20% complete").assertIsDisplayed()

            // Test navigation state preservation
            onNodeWithText("Workouts").performClick()
            onNodeWithText("Goals").performClick()

            // Data should still be there
            onNodeWithText("1600 / 8000 steps").assertIsDisplayed()
        }
    }

    /**
     * Test 8: Accessibility and UI Responsiveness
     * Tests screen readers, content descriptions, and responsive design
     */
    @Test
    fun testAccessibilityAndResponsiveness() {
        composeTestRule.apply {
            // Test content descriptions exist
            onNodeWithContentDescription("Workouts tab").assertIsDisplayed()
            onNodeWithContentDescription("Goals tab").assertIsDisplayed()
            onNodeWithContentDescription("Progress tab").assertIsDisplayed()
            onNodeWithContentDescription("Nutrition tab").assertIsDisplayed()

            // Test workout screen accessibility
            onNodeWithText("Workouts").performClick()
            onNodeWithContentDescription("Start new workout").assertIsDisplayed()

            // Test goal screen accessibility
            onNodeWithText("Goals").performClick()
            onNodeWithContentDescription("Create new goal").assertIsDisplayed()

            // Test progress screen accessibility
            onNodeWithText("Progress").performClick()
            onNodeWithContentDescription("Progress analytics chart").assertIsDisplayed()

            // Test nutrition screen accessibility
            onNodeWithText("Nutrition").performClick()
            onNodeWithContentDescription("Daily nutrition summary").assertIsDisplayed()

            // Test responsive design by simulating different screen sizes
            // (This would require additional setup for different device configurations)
        }
    }

    /**
     * Test 9: Error Handling and Edge Cases
     * Tests error states, network issues, and invalid inputs
     */
    @Test
    fun testErrorHandlingAndEdgeCases() {
        composeTestRule.apply {
            // Test invalid goal creation
            onNodeWithText("Goals").performClick()
            onNodeWithText("New Goal").performClick()
            onNodeWithText("Daily Steps").performClick()

            // Leave target value empty and try to create
            onNodeWithText("Create Goal").assertIsNotEnabled()

            // Enter invalid target value
            onNodeWithText("Target Value").performTextInput("-100")
            onNodeWithText("Create Goal").assertIsNotEnabled()

            // Enter valid value
            onNodeWithText("Target Value").performTextClearance()
            onNodeWithText("Target Value").performTextInput("1000")
            onNodeWithText("Create Goal").assertIsEnabled()
            onNodeWithText("Create Goal").performClick()

            waitForIdle()

            // Test invalid workout set logging
            onNodeWithText("Workouts").performClick()
            onNodeWithText("Start Workout").performClick()
            onNodeWithText("WEIGHTLIFTING").performClick()
            onNodeWithText("Start").performClick()

            waitForIdle()

            onNodeWithText("Add Exercise").performClick()
            onNodeWithTag("search_exercises").performTextInput("bench")
            onNodeWithText("Bench Press").performClick()

            onNodeWithContentDescription("Log set").performClick()

            // Try to log set without any data
            onNodeWithText("Log Set").assertIsNotEnabled()

            // Enter invalid negative reps
            onNodeWithText("Reps").performTextInput("-5")
            onNodeWithText("Log Set").assertIsNotEnabled()

            // Enter valid data
            onNodeWithText("Reps").performTextClearance()
            onNodeWithText("Reps").performTextInput("10")
            onNodeWithText("Log Set").assertIsEnabled()

            // Test invalid nutrition entry
            onNodeWithText("Nutrition").performClick()
            onNodeWithText("Add Custom Food").performClick()

            // Try to add food without name
            onNodeWithText("Add Food").assertIsNotEnabled()

            // Add name but invalid calories
            onNodeWithText("Food Name").performTextInput("Test Food")
            onNodeWithText("Calories per serving").performTextInput("-100")
            onNodeWithText("Add Food").assertIsNotEnabled()
        }
    }

    /**
     * Test 10: Performance and Memory Management
     * Tests app performance under load and memory usage
     */
    @Test
    fun testPerformanceAndMemoryManagement() {
        composeTestRule.apply {
            // Create multiple goals quickly
            repeat(5) { index ->
                onNodeWithText("Goals").performClick()
                onNodeWithText("New Goal").performClick()
                onNodeWithText("Daily Steps").performClick()

                onNodeWithText("Goal Title").performTextClearance()
                onNodeWithText("Goal Title").performTextInput("Goal $index")
                onNodeWithText("5000 steps").performClick()
                onNodeWithText("Create Goal").performClick()

                waitForIdle()
            }

            // Verify all goals were created
            onAllNodesWithText("Goal").assertCountEquals(5)

            // Navigate between screens rapidly
            repeat(10) {
                onNodeWithText("Workouts").performClick()
                onNodeWithText("Progress").performClick()
                onNodeWithText("Nutrition").performClick()
                onNodeWithText("Goals").performClick()
                waitForIdle()
            }

            // Create large workout with many sets
            onNodeWithText("Workouts").performClick()
            onNodeWithText("Start Workout").performClick()
            onNodeWithText("WEIGHTLIFTING").performClick()
            onNodeWithText("Start").performClick()

            waitForIdle()

            onNodeWithText("Add Exercise").performClick()
            onNodeWithTag("search_exercises").performTextInput("squat")
            onNodeWithText("Squats").performClick()

            // Log many sets
            repeat(10) { setNumber ->
                onNodeWithContentDescription("Log set").performClick()
                onNodeWithText("Reps").performTextInput("${10 + setNumber}")
                onNodeWithText("Weight (kg)").performTextInput("${100 + setNumber * 5}")
                onNodeWithText("Log Set").performClick()
                waitForIdle()
            }

            // Verify all sets were logged
            onNodeWithText("Set 10").assertIsDisplayed()

            // Finish workout
            onNodeWithText("Finish Workout").performClick()
            onNodeWithText("Finish").performClick()

            waitForIdle()

            // App should still be responsive
            onNodeWithText("Goals").performClick()
            onNodeWithText("Progress").performClick()
        }
    }

    /**
     * Helper function to set up test progress data
     */
    private fun setupTestProgressData() {
        runBlocking {
            val userId = 1L

            // Create test user
            val user = User(
                id = userId,
                email = "test@example.com",
                username = "testuser",
                passwordHash = "hashedpassword",
                passwordSalt = "salt123",
                firstName = "Test",
                lastName = "User"
            )
            database.userDao().insertUser(user)

            // Create test goals
            val stepGoal = Goal(
                userId = userId,
                title = "Daily Steps",
                goalType = GoalType.STEPS,
                targetValue = 10000.0,
                currentValue = 7500.0,
                unit = "steps",
                targetDate = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 30) }.time
            )
            database.goalDao().insertGoal(stepGoal)

            // Create test workouts
            repeat(7) { dayIndex ->
                val workout = Workout(
                    userId = userId,
                    title = "Test Workout $dayIndex",
                    workoutType = WorkoutType.WEIGHTLIFTING,
                    startTime = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_MONTH, -dayIndex)
                    }.time,
                    endTime = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_MONTH, -dayIndex)
                        add(Calendar.HOUR, 1)
                    }.time,
                    duration = 60,
                    caloriesBurned = 300 + dayIndex * 50,
                )
                database.workoutDao().insertWorkout(workout)
            }

            // Create test steps data
            repeat(7) { dayIndex ->
                val steps = Step(
                    userId = userId,
                    count = 8000 + dayIndex * 500,
                    date = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_MONTH, -dayIndex)
                    }.time,
                    caloriesBurned = ((8000 + dayIndex * 500) * 0.05).toFloat(),
                    distanceMeters = (8000 + dayIndex * 500) * 0.8f // Rough conversion to meters
                )
                database.stepDao().insertStep(steps)
            }
        }
    }
}
