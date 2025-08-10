package com.example.fitnesstrackerapp.data.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.fitnesstrackerapp.data.database.AppDatabase
import com.example.fitnesstrackerapp.data.entity.GoalStatus
import com.example.fitnesstrackerapp.data.entity.GoalType
import com.example.fitnesstrackerapp.util.test.TestHelper
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.Date

/**
 * Comprehensive unit tests for GoalDao.
 *
 * Tests all goal-related database operations including:
 * - CRUD operations (Create, Read, Update, Delete)
 * - Status and progress tracking
 * - Date-based queries and filtering
 * - Goal completion tracking
 * - Data integrity and constraints
 */
@ExperimentalCoroutinesApi
@RunWith(org.junit.runners.JUnit4::class)
class GoalDaoTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AppDatabase
    private lateinit var goalDao: GoalDao
    private lateinit var userDao: UserDao

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        )
            .allowMainThreadQueries()
            .build()

        goalDao = database.goalDao()
        userDao = database.userDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndGetGoal() = runTest {
        // Create user first (foreign key requirement)
        val user = TestHelper.createTestUser(email = "goal@test.com", username = "goaluser")
        val userId = userDao.insertUser(user)

        // Create and insert goal
        val goal = TestHelper.createTestGoal(
            userId = userId,
            title = "Weight Loss Goal",
            targetValue = 10.0,
            currentValue = 2.0,
            unit = "kg",
        )

        val goalId = goalDao.insertGoal(goal)
        assertThat(goalId).isGreaterThan(0)

        val retrievedGoal = goalDao.getGoalById(goalId)
        assertThat(retrievedGoal).isNotNull()
        assertThat(retrievedGoal?.title).isEqualTo("Weight Loss Goal")
        assertThat(retrievedGoal?.targetValue).isEqualTo(10.0)
        assertThat(retrievedGoal?.currentValue).isEqualTo(2.0)
        assertThat(retrievedGoal?.unit).isEqualTo("kg")
        assertThat(retrievedGoal?.userId).isEqualTo(userId)
    }

    @Test
    fun getGoalsByUser() = runTest {
        val user = TestHelper.createTestUser(email = "user@test.com", username = "testuser")
        val userId = userDao.insertUser(user)

        // Insert multiple goals for the user
        val goals = listOf(
            TestHelper.createTestGoal(userId = userId, title = "Goal 1", targetValue = 100.0),
            TestHelper.createTestGoal(userId = userId, title = "Goal 2", targetValue = 200.0),
            TestHelper.createTestGoal(userId = userId, title = "Goal 3", targetValue = 300.0),
        )

        goals.forEach { goal ->
            goalDao.insertGoal(goal)
        }

        val userGoals = goalDao.getGoalsByUser(userId).first()
        assertThat(userGoals).hasSize(3)

        // Verify goals are ordered by creation date (DESC)
        val titles = userGoals.map { it.title }
        assertThat(titles).containsExactly("Goal 3", "Goal 2", "Goal 1")
    }

    @Test
    fun getActiveGoals() = runTest {
        val user = TestHelper.createTestUser(email = "active@test.com", username = "activeuser")
        val userId = userDao.insertUser(user)

        // Insert goals with different statuses
        val activeGoal1 = TestHelper.createTestGoal(
            userId = userId,
            title = "Active Goal 1",
        ).copy(status = GoalStatus.ACTIVE)

        val activeGoal2 = TestHelper.createTestGoal(
            userId = userId,
            title = "Active Goal 2",
        ).copy(status = GoalStatus.ACTIVE)

        val completedGoal = TestHelper.createTestGoal(
            userId = userId,
            title = "Completed Goal",
        ).copy(status = GoalStatus.COMPLETED)

        goalDao.insertGoal(activeGoal1)
        goalDao.insertGoal(activeGoal2)
        goalDao.insertGoal(completedGoal)

        val activeGoals = goalDao.getActiveGoals(userId).first()
        assertThat(activeGoals).hasSize(2)
        assertThat(activeGoals.map { it.title }).containsExactly("Active Goal 1", "Active Goal 2")
        assertThat(activeGoals.all { it.status == GoalStatus.ACTIVE }).isTrue()
    }

    @Test
    fun getCompletedGoals() = runTest {
        val user = TestHelper.createTestUser(email = "completed@test.com", username = "completeduser")
        val userId = userDao.insertUser(user)

        // Insert goals with different statuses
        val completedGoal1 = TestHelper.createTestGoal(
            userId = userId,
            title = "Completed Goal 1",
        ).copy(status = GoalStatus.COMPLETED)

        val completedGoal2 = TestHelper.createTestGoal(
            userId = userId,
            title = "Completed Goal 2",
        ).copy(status = GoalStatus.COMPLETED)

        val activeGoal = TestHelper.createTestGoal(
            userId = userId,
            title = "Active Goal",
        ).copy(status = GoalStatus.ACTIVE)

        goalDao.insertGoal(completedGoal1)
        goalDao.insertGoal(completedGoal2)
        goalDao.insertGoal(activeGoal)

        val completedGoals = goalDao.getCompletedGoals(userId).first()
        assertThat(completedGoals).hasSize(2)
        assertThat(completedGoals.map { it.title }).containsExactly("Completed Goal 2", "Completed Goal 1")
        assertThat(completedGoals.all { it.status == GoalStatus.COMPLETED }).isTrue()
    }

    @Test
    fun getGoalsByType() = runTest {
        val user = TestHelper.createTestUser(email = "type@test.com", username = "typeuser")
        val userId = userDao.insertUser(user)

        // Insert goals with different types
        val weightGoal1 = TestHelper.createTestGoal(
            userId = userId,
            title = "Weight Goal 1",
        ).copy(goalType = GoalType.WEIGHT_LOSS)

        val weightGoal2 = TestHelper.createTestGoal(
            userId = userId,
            title = "Weight Goal 2",
        ).copy(goalType = GoalType.WEIGHT_LOSS)

        val fitnessGoal = TestHelper.createTestGoal(
            userId = userId,
            title = "Fitness Goal",
        ).copy(goalType = GoalType.FITNESS)

        goalDao.insertGoal(weightGoal1)
        goalDao.insertGoal(weightGoal2)
        goalDao.insertGoal(fitnessGoal)

        val weightGoals = goalDao.getGoalsByType(userId, GoalType.WEIGHT_LOSS.name).first()
        assertThat(weightGoals).hasSize(2)
        assertThat(weightGoals.map { it.title }).containsExactly("Weight Goal 2", "Weight Goal 1")
        assertThat(weightGoals.all { it.goalType == GoalType.WEIGHT_LOSS }).isTrue()
    }

    @Test
    fun getGoalsByStatus() = runTest {
        val user = TestHelper.createTestUser(email = "status@test.com", username = "statususer")
        val userId = userDao.insertUser(user)

        // Insert goals with different statuses
        val activeGoal1 = TestHelper.createTestGoal(
            userId = userId,
            title = "Active Goal 1",
        ).copy(status = GoalStatus.ACTIVE)

        val activeGoal2 = TestHelper.createTestGoal(
            userId = userId,
            title = "Active Goal 2",
        ).copy(status = GoalStatus.ACTIVE)

        val pausedGoal = TestHelper.createTestGoal(
            userId = userId,
            title = "Paused Goal",
        ).copy(status = GoalStatus.PAUSED)

        goalDao.insertGoal(activeGoal1)
        goalDao.insertGoal(activeGoal2)
        goalDao.insertGoal(pausedGoal)

        val activeGoals = goalDao.getGoalsByStatus(userId, GoalStatus.ACTIVE.name).first()
        assertThat(activeGoals).hasSize(2)
        assertThat(activeGoals.all { it.status == GoalStatus.ACTIVE }).isTrue()
    }

    @Test
    fun getActiveGoalCount() = runTest {
        val user = TestHelper.createTestUser(email = "count@test.com", username = "countuser")
        val userId = userDao.insertUser(user)

        // Insert goals with different statuses
        goalDao.insertGoal(TestHelper.createTestGoal(userId = userId).copy(status = GoalStatus.ACTIVE))
        goalDao.insertGoal(TestHelper.createTestGoal(userId = userId).copy(status = GoalStatus.ACTIVE))
        goalDao.insertGoal(TestHelper.createTestGoal(userId = userId).copy(status = GoalStatus.COMPLETED))
        goalDao.insertGoal(TestHelper.createTestGoal(userId = userId).copy(status = GoalStatus.PAUSED))

        val activeCount = goalDao.getActiveGoalCount(userId)
        assertThat(activeCount).isEqualTo(2)
    }

    @Test
    fun updateGoalProgress() = runTest {
        val user = TestHelper.createTestUser(email = "progress@test.com", username = "progressuser")
        val userId = userDao.insertUser(user)

        val goal = TestHelper.createTestGoal(
            userId = userId,
            currentValue = 0.0,
        )
        val goalId = goalDao.insertGoal(goal)

        // Update progress
        val newProgress = 5.5
        val updatedAt = System.currentTimeMillis()
        goalDao.updateGoalProgress(goalId, newProgress, updatedAt)

        val updatedGoal = goalDao.getGoalById(goalId)
        assertThat(updatedGoal?.currentValue).isEqualTo(newProgress)
        assertThat(updatedGoal?.updatedAt?.time).isEqualTo(updatedAt)
    }

    @Test
    fun incrementGoalProgress() = runTest {
        val user = TestHelper.createTestUser(email = "increment@test.com", username = "incrementuser")
        val userId = userDao.insertUser(user)

        val goal = TestHelper.createTestGoal(
            userId = userId,
            currentValue = 10.0,
        )
        val goalId = goalDao.insertGoal(goal)

        // Increment progress
        val increment = 2.5
        val updatedAt = System.currentTimeMillis()
        goalDao.incrementGoalProgress(goalId, increment, updatedAt)

        val updatedGoal = goalDao.getGoalById(goalId)
        assertThat(updatedGoal?.currentValue).isEqualTo(12.5)
        assertThat(updatedGoal?.updatedAt?.time).isEqualTo(updatedAt)
    }

    @Test
    fun markGoalAsAchieved() = runTest {
        val user = TestHelper.createTestUser(email = "achieved@test.com", username = "achieveduser")
        val userId = userDao.insertUser(user)

        val goal = TestHelper.createTestGoal(userId = userId).copy(status = GoalStatus.ACTIVE)
        val goalId = goalDao.insertGoal(goal)

        // Mark as achieved
        val updatedAt = System.currentTimeMillis()
        goalDao.markGoalAsAchieved(goalId, updatedAt)

        val updatedGoal = goalDao.getGoalById(goalId)
        assertThat(updatedGoal?.status).isEqualTo(GoalStatus.COMPLETED)
        assertThat(updatedGoal?.updatedAt?.time).isEqualTo(updatedAt)
    }

    @Test
    fun updateGoalStatus() = runTest {
        val user = TestHelper.createTestUser(email = "statusupdate@test.com", username = "statusupdateuser")
        val userId = userDao.insertUser(user)

        val goal = TestHelper.createTestGoal(userId = userId).copy(status = GoalStatus.ACTIVE)
        val goalId = goalDao.insertGoal(goal)

        // Update status to paused
        val updatedAt = System.currentTimeMillis()
        goalDao.updateGoalStatus(goalId, GoalStatus.PAUSED.name, updatedAt)

        val updatedGoal = goalDao.getGoalById(goalId)
        assertThat(updatedGoal?.status).isEqualTo(GoalStatus.PAUSED)
        assertThat(updatedGoal?.updatedAt?.time).isEqualTo(updatedAt)
    }

    @Test
    fun getGoalsDueSoon() = runTest {
        val user = TestHelper.createTestUser(email = "duesoon@test.com", username = "duesoonuser")
        val userId = userDao.insertUser(user)

        val currentTime = System.currentTimeMillis()
        val soonDate = currentTime + (5 * 24 * 60 * 60 * 1000L) // 5 days from now
        val laterDate = currentTime + (15 * 24 * 60 * 60 * 1000L) // 15 days from now

        // Insert goals with different target dates
        val soonGoal = TestHelper.createTestGoal(
            userId = userId,
            title = "Due Soon",
        ).copy(
            targetDate = Date(soonDate),
            status = GoalStatus.ACTIVE,
        )

        val laterGoal = TestHelper.createTestGoal(
            userId = userId,
            title = "Due Later",
        ).copy(
            targetDate = Date(laterDate),
            status = GoalStatus.ACTIVE,
        )

        goalDao.insertGoal(soonGoal)
        goalDao.insertGoal(laterGoal)

        // Get goals due within 7 days
        val endDate = currentTime + (7 * 24 * 60 * 60 * 1000L)
        val dueSoonGoals = goalDao.getGoalsDueSoon(userId, endDate).first()

        assertThat(dueSoonGoals).hasSize(1)
        assertThat(dueSoonGoals[0].title).isEqualTo("Due Soon")
    }

    @Test
    fun getGoalsByDateRange() = runTest {
        val user = TestHelper.createTestUser(email = "daterange@test.com", username = "daterangeuser")
        val userId = userDao.insertUser(user)

        val currentTime = System.currentTimeMillis()
        val startDate = currentTime + (2 * 24 * 60 * 60 * 1000L) // 2 days from now
        val midDate = currentTime + (5 * 24 * 60 * 60 * 1000L) // 5 days from now
        val endDate = currentTime + (10 * 24 * 60 * 60 * 1000L) // 10 days from now
        val outsideDate = currentTime + (15 * 24 * 60 * 60 * 1000L) // 15 days from now

        // Insert goals with different target dates
        goalDao.insertGoal(
            TestHelper.createTestGoal(userId = userId, title = "Within Range 1")
                .copy(targetDate = Date(midDate)),
        )
        goalDao.insertGoal(
            TestHelper.createTestGoal(userId = userId, title = "Within Range 2")
                .copy(targetDate = Date(midDate + 1000)),
        )
        goalDao.insertGoal(
            TestHelper.createTestGoal(userId = userId, title = "Outside Range")
                .copy(targetDate = Date(outsideDate)),
        )

        val goalsInRange = goalDao.getGoalsByDateRange(userId, startDate, endDate).first()
        assertThat(goalsInRange).hasSize(2)
        assertThat(goalsInRange.map { it.title }).containsExactly("Within Range 1", "Within Range 2")
    }

    @Test
    fun updateGoal() = runTest {
        val user = TestHelper.createTestUser(email = "update@test.com", username = "updateuser")
        val userId = userDao.insertUser(user)

        val goal = TestHelper.createTestGoal(
            userId = userId,
            title = "Original Title",
            targetValue = 100.0,
        )
        val goalId = goalDao.insertGoal(goal)

        // Update the goal
        val updatedGoal = goal.copy(
            id = goalId,
            title = "Updated Title",
            targetValue = 150.0,
            updatedAt = Date(),
        )
        goalDao.updateGoal(updatedGoal)

        val retrievedGoal = goalDao.getGoalById(goalId)
        assertThat(retrievedGoal?.title).isEqualTo("Updated Title")
        assertThat(retrievedGoal?.targetValue).isEqualTo(150.0)
    }

    @Test
    fun deleteGoal() = runTest {
        val user = TestHelper.createTestUser(email = "delete@test.com", username = "deleteuser")
        val userId = userDao.insertUser(user)

        val goal = TestHelper.createTestGoal(userId = userId, title = "To Be Deleted")
        val goalId = goalDao.insertGoal(goal)

        // Verify goal exists
        var retrievedGoal = goalDao.getGoalById(goalId)
        assertThat(retrievedGoal).isNotNull()

        // Delete the goal
        goalDao.deleteGoal(goal.copy(id = goalId))

        // Verify goal is deleted
        retrievedGoal = goalDao.getGoalById(goalId)
        assertThat(retrievedGoal).isNull()
    }

    @Test
    fun deleteGoalById() = runTest {
        val user = TestHelper.createTestUser(email = "deletebyid@test.com", username = "deletebyiduser")
        val userId = userDao.insertUser(user)

        val goal = TestHelper.createTestGoal(userId = userId, title = "To Be Deleted By ID")
        val goalId = goalDao.insertGoal(goal)

        // Verify goal exists
        var retrievedGoal = goalDao.getGoalById(goalId)
        assertThat(retrievedGoal).isNotNull()

        // Delete by ID
        goalDao.deleteGoalById(goalId)

        // Verify goal is deleted
        retrievedGoal = goalDao.getGoalById(goalId)
        assertThat(retrievedGoal).isNull()
    }

    @Test
    fun deleteAllByUser() = runTest {
        val user = TestHelper.createTestUser(email = "deleteall@test.com", username = "deletealluser")
        val userId = userDao.insertUser(user)

        // Insert multiple goals
        goalDao.insertGoal(TestHelper.createTestGoal(userId = userId, title = "Goal 1"))
        goalDao.insertGoal(TestHelper.createTestGoal(userId = userId, title = "Goal 2"))
        goalDao.insertGoal(TestHelper.createTestGoal(userId = userId, title = "Goal 3"))

        // Verify goals exist
        var userGoals = goalDao.getGoalsByUser(userId).first()
        assertThat(userGoals).hasSize(3)

        // Delete all goals for user
        goalDao.deleteAllByUser(userId)

        // Verify all goals are deleted
        userGoals = goalDao.getGoalsByUser(userId).first()
        assertThat(userGoals).isEmpty()
    }

    @Test
    fun insertAllGoals() = runTest {
        val user = TestHelper.createTestUser(email = "insertall@test.com", username = "insertalluser")
        val userId = userDao.insertUser(user)

        val goals = listOf(
            TestHelper.createTestGoal(userId = userId, title = "Bulk Goal 1"),
            TestHelper.createTestGoal(userId = userId, title = "Bulk Goal 2"),
            TestHelper.createTestGoal(userId = userId, title = "Bulk Goal 3"),
        )

        val goalIds = goalDao.insertAll(goals)
        assertThat(goalIds).hasSize(3)
        assertThat(goalIds.all { it > 0 }).isTrue()

        val userGoals = goalDao.getGoalsByUser(userId).first()
        assertThat(userGoals).hasSize(3)
    }

    @Test
    fun testForeignKeyConstraint() = runTest {
        val goal = TestHelper.createTestGoal(
            userId = 999L, // Non-existent user
            title = "Invalid Goal",
        )

        try {
            goalDao.insertGoal(goal)
            Assert.fail("Should throw foreign key constraint exception")
        } catch (e: Exception) {
            // Expected foreign key constraint violation
            assertThat(e.message).contains("FOREIGN KEY")
        }
    }

    @Test
    fun testGoalCascadeDelete() = runTest {
        // Create user and goal
        val user = TestHelper.createTestUser(email = "cascade@test.com", username = "cascadeuser")
        val userId = userDao.insertUser(user)

        val goal = TestHelper.createTestGoal(userId = userId, title = "Cascade Test")
        val goalId = goalDao.insertGoal(goal)

        // Verify goal exists
        var retrievedGoal = goalDao.getGoalById(goalId)
        assertThat(retrievedGoal).isNotNull()

        // Delete user (should cascade delete goal)
        userDao.deleteUserById(userId)

        // Verify goal is also deleted
        retrievedGoal = goalDao.getGoalById(goalId)
        assertThat(retrievedGoal).isNull()
    }

    @Test
    fun testGoalProgressCalculation() = runTest {
        val user = TestHelper.createTestUser(email = "calculation@test.com", username = "calculationuser")
        val userId = userDao.insertUser(user)

        val goal = TestHelper.createTestGoal(
            userId = userId,
            targetValue = 100.0,
            currentValue = 25.0,
        )
        val goalId = goalDao.insertGoal(goal)

        val retrievedGoal = goalDao.getGoalById(goalId)
        assertThat(retrievedGoal).isNotNull()

        // Calculate progress percentage
        val progressPercentage = (retrievedGoal!!.currentValue / retrievedGoal.targetValue) * 100
        assertThat(progressPercentage).isEqualTo(25.0)

        // Test goal completion check
        val isCompleted = retrievedGoal.currentValue >= retrievedGoal.targetValue
        assertThat(isCompleted).isFalse()
    }

    @Test
    fun testGoalSorting() = runTest {
        val user = TestHelper.createTestUser(email = "sorting@test.com", username = "sortinguser")
        val userId = userDao.insertUser(user)

        val currentTime = System.currentTimeMillis()
        val dates = listOf(
            currentTime + (1 * 24 * 60 * 60 * 1000L), // 1 day
            currentTime + (3 * 24 * 60 * 60 * 1000L), // 3 days
            currentTime + (2 * 24 * 60 * 60 * 1000L), // 2 days
        )

        dates.forEachIndexed { index, date ->
            goalDao.insertGoal(
                TestHelper.createTestGoal(
                    userId = userId,
                    title = "Goal ${index + 1}",
                ).copy(
                    targetDate = Date(date),
                    status = GoalStatus.ACTIVE,
                ),
            )
        }

        val activeGoals = goalDao.getActiveGoals(userId).first()
        assertThat(activeGoals).hasSize(3)

        // Should be ordered by target date ASC
        val targetDates = activeGoals.map { it.targetDate.time }
        assertThat(targetDates).isInOrder()
    }
}
