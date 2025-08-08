package com.example.fitnesstrackerapp.ui.goal

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.fitnesstrackerapp.data.entity.*
import com.example.fitnesstrackerapp.repository.GoalRepository
import com.example.fitnesstrackerapp.util.test.TestData
import com.example.fitnesstrackerapp.util.test.TestHelper
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * Comprehensive UI tests for goal completion flow.
 *
 * Tests critical goal management functionality including:
 * - Goal creation with various types and targets
 * - Progress tracking and updates
 * - Goal completion and achievement celebration
 * - Goal editing and deletion
 * - Goal status management (active, paused, completed)
 * - Progress visualization and charts
 * - Goal reminder settings
 * - Multiple goal management
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class GoalCompletionFlowTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeTestRule = createComposeRule()

    @Inject
    lateinit var goalRepository: GoalRepository

    private lateinit var goalViewModel: GoalViewModel

    @Before
    fun setup() {
        hiltRule.inject()

        // Mock the goal repository for consistent testing
        goalRepository = mockk {
            every { getActiveGoals(any()) } returns flowOf(emptyList())
            every { getCompletedGoals(any()) } returns flowOf(emptyList())
            every { getAllGoals(any()) } returns flowOf(emptyList())
        }

        goalViewModel = GoalViewModel(goalRepository, TestData.TEST_USER_ID)
    }

    @Test
    fun goalScreen_displaysCorrectElements() {
        composeTestRule.setContent {
            GoalScreen(
                viewModel = goalViewModel,
                onNavigateToCreateGoal = { },
                onNavigateToGoalDetail = { },
            )
        }

        // Verify main UI elements are displayed
        composeTestRule.onNodeWithText("Your Goals").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Add new goal").assertIsDisplayed()
        composeTestRule.onNodeWithText("Active Goals").assertIsDisplayed()
        composeTestRule.onNodeWithText("Completed Goals").assertIsDisplayed()
    }

    @Test
    fun goalScreen_withNoGoals_showsEmptyState() {
        composeTestRule.setContent {
            GoalScreen(
                viewModel = goalViewModel,
                onNavigateToCreateGoal = { },
                onNavigateToGoalDetail = { },
            )
        }

        // Verify empty state is displayed
        composeTestRule.onNodeWithText("No goals yet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Create your first goal to get started!")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Create Goal").assertIsDisplayed()
    }

    @Test
    fun goalScreen_withActiveGoals_displaysGoalList() {
        val activeGoals = listOf(
            TestHelper.createTestGoal(
                title = "Lose 10 kg",
                targetValue = 10.0,
                currentValue = 3.0,
                unit = "kg",
            ).copy(status = GoalStatus.ACTIVE),

            TestHelper.createTestGoal(
                title = "Run 5k daily",
                targetValue = 5.0,
                currentValue = 2.0,
                unit = "km",
            ).copy(status = GoalStatus.ACTIVE),
        )

        every { goalRepository.getActiveGoals(any()) } returns flowOf(activeGoals)
        goalViewModel = GoalViewModel(goalRepository, TestData.TEST_USER_ID)

        composeTestRule.setContent {
            GoalScreen(
                viewModel = goalViewModel,
                onNavigateToCreateGoal = { },
                onNavigateToGoalDetail = { },
            )
        }

        // Verify goals are displayed
        composeTestRule.onNodeWithText("Lose 10 kg").assertIsDisplayed()
        composeTestRule.onNodeWithText("Run 5k daily").assertIsDisplayed()

        // Verify progress indicators
        composeTestRule.onNodeWithText("3.0 / 10.0 kg").assertIsDisplayed()
        composeTestRule.onNodeWithText("2.0 / 5.0 km").assertIsDisplayed()
    }

    @Test
    fun goalCreation_withValidData_createsGoal() = runTest {
        var navigateToCreateGoalCalled = false

        coEvery { goalRepository.createGoal(any()) } returns 1L

        composeTestRule.setContent {
            GoalScreen(
                viewModel = goalViewModel,
                onNavigateToCreateGoal = { navigateToCreateGoalCalled = true },
                onNavigateToGoalDetail = { },
            )
        }

        // Click create goal button
        composeTestRule.onNodeWithContentDescription("Add new goal")
            .performClick()

        assertThat(navigateToCreateGoalCalled).isTrue()
    }

    @Test
    fun createGoalScreen_displaysAllFields() {
        composeTestRule.setContent {
            CreateGoalScreen(
                viewModel = goalViewModel,
                onGoalCreated = { },
                onNavigateBack = { },
            )
        }

        // Verify all form fields are displayed
        composeTestRule.onNodeWithText("Create New Goal").assertIsDisplayed()
        composeTestRule.onNodeWithText("Goal Title").assertIsDisplayed()
        composeTestRule.onNodeWithText("Goal Type").assertIsDisplayed()
        composeTestRule.onNodeWithText("Target Value").assertIsDisplayed()
        composeTestRule.onNodeWithText("Unit").assertIsDisplayed()
        composeTestRule.onNodeWithText("Target Date").assertIsDisplayed()
        composeTestRule.onNodeWithText("Description").assertIsDisplayed()
        composeTestRule.onNodeWithText("Create Goal").assertIsDisplayed()
    }

    @Test
    fun createGoalScreen_withValidData_submitsGoal() = runTest {
        var goalCreatedCalled = false

        coEvery { goalRepository.createGoal(any()) } returns 1L

        composeTestRule.setContent {
            CreateGoalScreen(
                viewModel = goalViewModel,
                onGoalCreated = { goalCreatedCalled = true },
                onNavigateBack = { },
            )
        }

        // Fill out the form
        composeTestRule.onNodeWithText("Goal Title")
            .performTextInput("Lose Weight")

        composeTestRule.onNodeWithText("Target Value")
            .performTextInput("10")

        composeTestRule.onNodeWithText("Unit")
            .performTextInput("kg")

        composeTestRule.onNodeWithText("Description")
            .performTextInput("Lose 10kg in 3 months")

        // Select goal type
        composeTestRule.onNodeWithText("Goal Type")
            .performClick()
        composeTestRule.onNodeWithText("Weight Loss")
            .performClick()

        // Submit form
        composeTestRule.onNodeWithText("Create Goal")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify goal creation was called
        coVerify { goalRepository.createGoal(any()) }
        assertThat(goalCreatedCalled).isTrue()
    }

    @Test
    fun createGoalScreen_withEmptyFields_showsValidationErrors() {
        composeTestRule.setContent {
            CreateGoalScreen(
                viewModel = goalViewModel,
                onGoalCreated = { },
                onNavigateBack = { },
            )
        }

        // Submit form without filling fields
        composeTestRule.onNodeWithText("Create Goal")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify validation errors
        composeTestRule.onNodeWithText("Goal title is required")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Target value is required")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Unit is required")
            .assertIsDisplayed()
    }

    @Test
    fun goalProgress_canBeUpdated() = runTest {
        val goal = TestHelper.createTestGoal(
            id = 1L,
            title = "Daily Steps",
            targetValue = 10000.0,
            currentValue = 5000.0,
            unit = "steps",
        ).copy(status = GoalStatus.ACTIVE)

        coEvery { goalRepository.updateGoalProgress(any(), any()) } just Runs
        coEvery { goalRepository.getGoalById(1L) } returns goal

        composeTestRule.setContent {
            GoalDetailScreen(
                goalId = 1L,
                viewModel = goalViewModel,
                onNavigateBack = { },
            )
        }

        // Verify current progress is displayed
        composeTestRule.onNodeWithText("5000 / 10000 steps").assertIsDisplayed()

        // Find and click progress update button
        composeTestRule.onNodeWithContentDescription("Update progress")
            .performClick()

        // Enter new progress value
        composeTestRule.onNodeWithText("Progress")
            .performTextInput("7500")

        // Confirm update
        composeTestRule.onNodeWithText("Update")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify repository was called to update progress
        coVerify { goalRepository.updateGoalProgress(1L, 7500.0) }
    }

    @Test
    fun goalCompletion_showsAchievementCelebration() = runTest {
        val completedGoal = TestHelper.createTestGoal(
            id = 1L,
            title = "Marathon Training",
            targetValue = 42.0,
            currentValue = 42.0,
            unit = "km",
        ).copy(status = GoalStatus.COMPLETED)

        coEvery { goalRepository.getGoalById(1L) } returns completedGoal
        coEvery { goalRepository.markGoalAsCompleted(any()) } just Runs

        composeTestRule.setContent {
            GoalDetailScreen(
                goalId = 1L,
                viewModel = goalViewModel,
                onNavigateBack = { },
            )
        }

        // Verify achievement celebration elements
        composeTestRule.onNodeWithText("🎉 Goal Completed! 🎉").assertIsDisplayed()
        composeTestRule.onNodeWithText("Marathon Training").assertIsDisplayed()
        composeTestRule.onNodeWithText("Congratulations! You've achieved your goal!")
            .assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Share achievement")
            .assertIsDisplayed()
    }

    @Test
    fun goalProgress_visualizationDisplaysCorrectly() = runTest {
        val goal = TestHelper.createTestGoal(
            title = "Weight Loss",
            targetValue = 20.0,
            currentValue = 12.0,
            unit = "kg",
        )

        composeTestRule.setContent {
            GoalProgressCard(
                goal = goal,
                onUpdateProgress = { },
                onGoalClick = { },
            )
        }

        // Verify progress visualization elements
        composeTestRule.onNodeWithText("Weight Loss").assertIsDisplayed()
        composeTestRule.onNodeWithText("12.0 / 20.0 kg").assertIsDisplayed()
        composeTestRule.onNodeWithText("60%").assertIsDisplayed() // Progress percentage

        // Verify progress bar is displayed
        composeTestRule.onNodeWithTag("progress_bar").assertIsDisplayed()
    }

    @Test
    fun goalsList_canBeFilteredByStatus() {
        val allGoals = listOf(
            TestHelper.createTestGoal(title = "Active Goal 1").copy(status = GoalStatus.ACTIVE),
            TestHelper.createTestGoal(title = "Active Goal 2").copy(status = GoalStatus.ACTIVE),
            TestHelper.createTestGoal(title = "Completed Goal").copy(status = GoalStatus.COMPLETED),
            TestHelper.createTestGoal(title = "Paused Goal").copy(status = GoalStatus.PAUSED),
        )

        every { goalRepository.getAllGoals(any()) } returns flowOf(allGoals)
        every { goalRepository.getActiveGoals(any()) } returns flowOf(allGoals.filter { it.status == GoalStatus.ACTIVE })
        every { goalRepository.getCompletedGoals(any()) } returns flowOf(allGoals.filter { it.status == GoalStatus.COMPLETED })

        goalViewModel = GoalViewModel(goalRepository, TestData.TEST_USER_ID)

        composeTestRule.setContent {
            GoalScreen(
                viewModel = goalViewModel,
                onNavigateToCreateGoal = { },
                onNavigateToGoalDetail = { },
            )
        }

        // Initially show active goals
        composeTestRule.onNodeWithText("Active Goal 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Active Goal 2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Completed Goal").assertDoesNotExist()

        // Switch to completed goals
        composeTestRule.onNodeWithText("Completed Goals")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify completed goals are shown
        composeTestRule.onNodeWithText("Completed Goal").assertIsDisplayed()
        composeTestRule.onNodeWithText("Active Goal 1").assertDoesNotExist()
    }

    @Test
    fun goalEdit_allowsModification() = runTest {
        val goal = TestHelper.createTestGoal(
            id = 1L,
            title = "Original Title",
            targetValue = 100.0,
            unit = "units",
        )

        coEvery { goalRepository.getGoalById(1L) } returns goal
        coEvery { goalRepository.updateGoal(any()) } just Runs

        composeTestRule.setContent {
            EditGoalScreen(
                goalId = 1L,
                viewModel = goalViewModel,
                onGoalUpdated = { },
                onNavigateBack = { },
            )
        }

        // Verify current values are pre-filled
        composeTestRule.onNodeWithText("Original Title").assertIsDisplayed()

        // Edit the title
        composeTestRule.onNodeWithText("Goal Title")
            .performTextClearance()
            .performTextInput("Updated Title")

        // Edit target value
        composeTestRule.onNodeWithText("Target Value")
            .performTextClearance()
            .performTextInput("150")

        // Save changes
        composeTestRule.onNodeWithText("Update Goal")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify repository was called to update goal
        coVerify { goalRepository.updateGoal(any()) }
    }

    @Test
    fun goalDeletion_requiresConfirmation() = runTest {
        val goal = TestHelper.createTestGoal(
            id = 1L,
            title = "Goal to Delete",
        )

        coEvery { goalRepository.getGoalById(1L) } returns goal
        coEvery { goalRepository.deleteGoal(any()) } just Runs

        composeTestRule.setContent {
            GoalDetailScreen(
                goalId = 1L,
                viewModel = goalViewModel,
                onNavigateBack = { },
            )
        }

        // Click delete button
        composeTestRule.onNodeWithContentDescription("Delete goal")
            .performClick()

        // Verify confirmation dialog appears
        composeTestRule.onNodeWithText("Delete Goal")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Are you sure you want to delete this goal?")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete")
            .assertIsDisplayed()

        // Confirm deletion
        composeTestRule.onNodeWithText("Delete")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify repository was called to delete goal
        coVerify { goalRepository.deleteGoal(any()) }
    }

    @Test
    fun goalReminder_canBeSetAndModified() = runTest {
        val goal = TestHelper.createTestGoal(
            id = 1L,
            title = "Daily Exercise",
        )

        coEvery { goalRepository.getGoalById(1L) } returns goal
        coEvery { goalRepository.updateGoalReminder(any(), any()) } just Runs

        composeTestRule.setContent {
            GoalDetailScreen(
                goalId = 1L,
                viewModel = goalViewModel,
                onNavigateBack = { },
            )
        }

        // Click reminder settings
        composeTestRule.onNodeWithText("Set Reminder")
            .performClick()

        // Set reminder time
        composeTestRule.onNodeWithText("Reminder Time")
            .performClick()

        // Select time (implementation specific)
        composeTestRule.onNodeWithText("9:00 AM")
            .performClick()

        // Enable daily reminder
        composeTestRule.onNodeWithText("Daily Reminder")
            .performClick()

        // Save reminder settings
        composeTestRule.onNodeWithText("Save Reminder")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify repository was called to update reminder
        coVerify { goalRepository.updateGoalReminder(any(), any()) }
    }

    @Test
    fun goalSearch_filtersGoalsCorrectly() {
        val goals = listOf(
            TestHelper.createTestGoal(title = "Weight Loss Journey"),
            TestHelper.createTestGoal(title = "Marathon Training"),
            TestHelper.createTestGoal(title = "Weight Gain Plan"),
            TestHelper.createTestGoal(title = "Daily Reading"),
        )

        every { goalRepository.getAllGoals(any()) } returns flowOf(goals)
        every { goalRepository.searchGoals(any(), any()) } returns flowOf(
            goals.filter { it.title.contains("Weight", ignoreCase = true) },
        )

        goalViewModel = GoalViewModel(goalRepository, TestData.TEST_USER_ID)

        composeTestRule.setContent {
            GoalScreen(
                viewModel = goalViewModel,
                onNavigateToCreateGoal = { },
                onNavigateToGoalDetail = { },
            )
        }

        // Use search functionality
        composeTestRule.onNodeWithContentDescription("Search goals")
            .performClick()

        composeTestRule.onNodeWithText("Search goals")
            .performTextInput("Weight")

        composeTestRule.waitForIdle()

        // Verify filtered results
        composeTestRule.onNodeWithText("Weight Loss Journey").assertIsDisplayed()
        composeTestRule.onNodeWithText("Weight Gain Plan").assertIsDisplayed()
        composeTestRule.onNodeWithText("Marathon Training").assertDoesNotExist()
        composeTestRule.onNodeWithText("Daily Reading").assertDoesNotExist()
    }

    @Test
    fun goalStatistics_displayCorrectData() {
        val goals = listOf(
            TestHelper.createTestGoal().copy(status = GoalStatus.ACTIVE),
            TestHelper.createTestGoal().copy(status = GoalStatus.ACTIVE),
            TestHelper.createTestGoal().copy(status = GoalStatus.COMPLETED),
            TestHelper.createTestGoal().copy(status = GoalStatus.PAUSED),
        )

        every { goalRepository.getAllGoals(any()) } returns flowOf(goals)
        goalViewModel = GoalViewModel(goalRepository, TestData.TEST_USER_ID)

        composeTestRule.setContent {
            GoalStatisticsScreen(
                viewModel = goalViewModel,
                onNavigateBack = { },
            )
        }

        // Verify statistics are displayed correctly
        composeTestRule.onNodeWithText("Goal Statistics").assertIsDisplayed()
        composeTestRule.onNodeWithText("Total Goals: 4").assertIsDisplayed()
        composeTestRule.onNodeWithText("Active: 2").assertIsDisplayed()
        composeTestRule.onNodeWithText("Completed: 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Paused: 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Completion Rate: 25%").assertIsDisplayed()
    }

    @Test
    fun goalPauseAndResume_worksCorrectly() = runTest {
        val activeGoal = TestHelper.createTestGoal(
            id = 1L,
            title = "Active Goal",
        ).copy(status = GoalStatus.ACTIVE)

        coEvery { goalRepository.getGoalById(1L) } returns activeGoal
        coEvery { goalRepository.pauseGoal(any()) } just Runs
        coEvery { goalRepository.resumeGoal(any()) } just Runs

        composeTestRule.setContent {
            GoalDetailScreen(
                goalId = 1L,
                viewModel = goalViewModel,
                onNavigateBack = { },
            )
        }

        // Pause the goal
        composeTestRule.onNodeWithText("Pause Goal")
            .performClick()

        composeTestRule.waitForIdle()

        coVerify { goalRepository.pauseGoal(1L) }

        // Mock paused state
        val pausedGoal = activeGoal.copy(status = GoalStatus.PAUSED)
        coEvery { goalRepository.getGoalById(1L) } returns pausedGoal

        composeTestRule.setContent {
            GoalDetailScreen(
                goalId = 1L,
                viewModel = goalViewModel,
                onNavigateBack = { },
            )
        }

        // Resume the goal
        composeTestRule.onNodeWithText("Resume Goal")
            .performClick()

        composeTestRule.waitForIdle()

        coVerify { goalRepository.resumeGoal(1L) }
    }
}
