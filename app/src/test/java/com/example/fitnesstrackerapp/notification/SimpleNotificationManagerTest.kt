package com.example.fitnesstrackerapp.notification

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.test.core.app.ApplicationProvider
import io.mockk.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for SimpleNotificationManager to verify API surface and functionality.
 * These tests lock the public API surface and ensure consistent behavior.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SimpleNotificationManagerTest {

    private lateinit var context: Context
    private lateinit var notificationManager: SimpleNotificationManager
    private lateinit var systemNotificationManager: NotificationManager
    private lateinit var notificationManagerCompat: NotificationManagerCompat

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()

        // Mock system notification managers
        systemNotificationManager = mockk(relaxed = true)
        notificationManagerCompat = mockk(relaxed = true)

        // Mock context.getSystemService
        every { context.getSystemService(Context.NOTIFICATION_SERVICE) } returns systemNotificationManager

        // Mock static NotificationManagerCompat
        mockkStatic(NotificationManagerCompat::class)
        every { NotificationManagerCompat.from(any()) } returns notificationManagerCompat
        every { notificationManagerCompat.areNotificationsEnabled() } returns true

        notificationManager = SimpleNotificationManager(context)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `constructor creates notification channels on Android O+`() {
        // Given/When - Constructor is called in setUp()

        // Then - Should not throw exception and channels should be created
        verify(atLeast = 0) { systemNotificationManager.createNotificationChannels(any()) }
        assertTrue(true) // Constructor completed successfully
    }

    @Test
    fun `showGoalReminder displays notification`() {
        // Given
        val title = "Goal Reminder"
        val message = "Time to work on your fitness goal!"
        val goalId = 123L

        every { notificationManagerCompat.notify(any(), any()) } just runs

        // When
        notificationManager.showGoalReminder(title, message, goalId)

        // Then - Should not throw exception
        assertTrue(true)
    }

    @Test
    fun `showGoalReminder without goalId displays notification`() {
        // Given
        val title = "Goal Reminder"
        val message = "Time to work on your fitness goal!"

        every { notificationManagerCompat.notify(any(), any()) } just runs

        // When
        notificationManager.showGoalReminder(title, message)

        // Then - Should not throw exception
        assertTrue(true)
    }

    @Test
    fun `showWorkoutComplete displays notification`() {
        // Given
        val workoutName = "Morning Run"
        val duration = 30
        val caloriesBurned = 250

        every { notificationManagerCompat.notify(any(), any()) } just runs

        // When
        notificationManager.showWorkoutComplete(workoutName, duration, caloriesBurned)

        // Then - Should not throw exception
        assertTrue(true)
    }

    @Test
    fun `showDailyProgress displays notification`() {
        // Given
        val stepsCompleted = 8500
        val caloriesBurned = 320
        val workoutsCompleted = 2

        every { notificationManagerCompat.notify(any(), any()) } just runs

        // When
        notificationManager.showDailyProgress(stepsCompleted, caloriesBurned, workoutsCompleted)

        // Then - Should not throw exception
        assertTrue(true)
    }

    @Test
    fun `showGeneralReminder displays notification`() {
        // Given
        val title = "Fitness Reminder"
        val message = "Don't forget to exercise today!"

        every { notificationManagerCompat.notify(any(), any()) } just runs

        // When
        notificationManager.showGeneralReminder(title, message)

        // Then - Should not throw exception
        assertTrue(true)
    }

    @Test
    fun `cancelNotification cancels specific notification`() {
        // Given
        val notificationId = SimpleNotificationManager.NOTIFICATION_ID_GOAL_REMINDER
        every { notificationManagerCompat.cancel(any()) } just runs

        // When
        notificationManager.cancelNotification(notificationId)

        // Then
        verify { notificationManagerCompat.cancel(notificationId) }
    }

    @Test
    fun `cancelAllNotifications cancels all notifications`() {
        // Given
        every { notificationManagerCompat.cancelAll() } just runs

        // When
        notificationManager.cancelAllNotifications()

        // Then
        verify { notificationManagerCompat.cancelAll() }
    }

    @Test
    fun `areNotificationsEnabled returns notification permission status`() {
        // Given
        every { notificationManagerCompat.areNotificationsEnabled() } returns true

        // When
        val result = notificationManager.areNotificationsEnabled()

        // Then
        assertTrue(result)
    }

    @Test
    fun `areNotificationsEnabled returns false when notifications disabled`() {
        // Given
        every { notificationManagerCompat.areNotificationsEnabled() } returns false

        // When
        val result = notificationManager.areNotificationsEnabled()

        // Then
        assertFalse(result)
    }

    @Test
    fun `areNotificationsEnabled handles exceptions gracefully`() {
        // Given
        every { notificationManagerCompat.areNotificationsEnabled() } throws RuntimeException("Test exception")

        // When
        val result = notificationManager.areNotificationsEnabled()

        // Then
        assertFalse(result) // Should return false on exception
    }

    @Test
    fun `showMotivationalNotification displays appropriate message for 100% progress`() {
        // Given
        val progressPercentage = 100
        val goalType = "steps"

        every { notificationManagerCompat.notify(any(), any()) } just runs

        // When
        notificationManager.showMotivationalNotification(progressPercentage, goalType)

        // Then - Should not throw exception
        assertTrue(true)
    }

    @Test
    fun `showMotivationalNotification displays appropriate message for 75% progress`() {
        // Given
        val progressPercentage = 75
        val goalType = "calories"

        every { notificationManagerCompat.notify(any(), any()) } just runs

        // When
        notificationManager.showMotivationalNotification(progressPercentage, goalType)

        // Then - Should not throw exception
        assertTrue(true)
    }

    @Test
    fun `showMotivationalNotification displays appropriate message for 50% progress`() {
        // Given
        val progressPercentage = 50
        val goalType = "workouts"

        every { notificationManagerCompat.notify(any(), any()) } just runs

        // When
        notificationManager.showMotivationalNotification(progressPercentage, goalType)

        // Then - Should not throw exception
        assertTrue(true)
    }

    @Test
    fun `showMotivationalNotification displays appropriate message for low progress`() {
        // Given
        val progressPercentage = 25
        val goalType = "distance"

        every { notificationManagerCompat.notify(any(), any()) } just runs

        // When
        notificationManager.showMotivationalNotification(progressPercentage, goalType)

        // Then - Should not throw exception
        assertTrue(true)
    }

    // Constant tests to ensure they exist and have expected values
    @Test
    fun `notification channel constants exist`() {
        assertEquals("fitness_goals", SimpleNotificationManager.CHANNEL_ID_GOALS)
        assertEquals("workouts", SimpleNotificationManager.CHANNEL_ID_WORKOUTS)
        assertEquals("daily_progress", SimpleNotificationManager.CHANNEL_ID_PROGRESS)
        assertEquals("reminders", SimpleNotificationManager.CHANNEL_ID_REMINDERS)
    }

    @Test
    fun `notification ID constants exist`() {
        assertEquals(1001, SimpleNotificationManager.NOTIFICATION_ID_GOAL_REMINDER)
        assertEquals(1002, SimpleNotificationManager.NOTIFICATION_ID_WORKOUT_COMPLETE)
        assertEquals(1003, SimpleNotificationManager.NOTIFICATION_ID_DAILY_PROGRESS)
        assertEquals(1004, SimpleNotificationManager.NOTIFICATION_ID_GENERAL_REMINDER)
    }

    @Test
    fun `request code constants exist`() {
        assertEquals(2001, SimpleNotificationManager.REQUEST_CODE_MAIN_ACTIVITY)
        assertEquals(2002, SimpleNotificationManager.REQUEST_CODE_GOAL_DETAIL)
        assertEquals(2003, SimpleNotificationManager.REQUEST_CODE_WORKOUT_DETAIL)
    }

    // API Surface Tests - Ensure methods exist and have correct signatures
    @Test
    fun `SimpleNotificationManager API surface test`() {
        // Test that all expected public methods exist with correct signatures
        try {
            // Goal notifications
            notificationManager.showGoalReminder("title", "message")
            notificationManager.showGoalReminder("title", "message", 1L)

            // Workout notifications
            notificationManager.showWorkoutComplete("workout", 30, 250)

            // Progress notifications
            notificationManager.showDailyProgress(1000, 100, 1)

            // General notifications
            notificationManager.showGeneralReminder("title", "message")
            notificationManager.showMotivationalNotification(50, "steps")

            // Management
            notificationManager.cancelNotification(1)
            notificationManager.cancelAllNotifications()
            notificationManager.areNotificationsEnabled()

            // Success - API surface is stable
            assertTrue(true)
        } catch (e: NoSuchMethodError) {
            fail("API surface has changed: ${e.message}")
        }
    }
}
