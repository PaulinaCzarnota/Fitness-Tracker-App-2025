package com.example.fitnesstrackerapp.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.fitnesstrackerapp.settings.MeasurementUnit
import com.example.fitnesstrackerapp.settings.SettingsManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for SettingsManager to verify API surface and functionality.
 * These tests lock the public API surface and ensure consistent settings management behavior.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsManagerTest {
    private lateinit var context: Context
    private lateinit var settingsManager: SettingsManager

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        settingsManager = SettingsManager(context)
    }

    @Test
    fun `themeMode returns default value initially`() = runTest {
        // When
        val themeMode = settingsManager.themeMode.first()

        // Then
        // Should return system default
        assertNotNull("Theme mode setting should not be null", themeMode)
        assertEquals("Should default to system", "system", themeMode)
    }

    @Test
    fun `updateThemeMode updates setting correctly`() = runTest {
        // Given
        val newValue = "dark"

        // When
        settingsManager.updateThemeMode(newValue)
        val result = settingsManager.themeMode.first()

        // Then
        assertEquals("Theme mode should be updated", newValue, result)
    }

    @Test
    fun `notificationEnabled returns default value initially`() = runTest {
        // When
        val notifications = settingsManager.notificationEnabled.first()

        // Then
        assertNotNull("Notifications setting should not be null", notifications)
        assertTrue("Should default to enabled", notifications)
    }

    @Test
    fun `updateNotificationsEnabled updates setting correctly`() = runTest {
        // Given
        val newValue = false

        // When
        settingsManager.updateNotificationsEnabled(newValue)
        val result = settingsManager.notificationEnabled.first()

        // Then
        assertEquals("Notifications should be updated", newValue, result)
    }

    @Test
    fun `stepGoal returns default value initially`() = runTest {
        // When
        val stepGoal = settingsManager.stepGoal.first()

        // Then
        assertTrue("Step goal should be positive", stepGoal > 0)
        assertEquals("Should default to 10000", 10000, stepGoal)
    }

    @Test
    fun `updateStepGoal updates setting correctly`() = runTest {
        // Given
        val newGoal = 12000

        // When
        settingsManager.updateStepGoal(newGoal)
        val result = settingsManager.stepGoal.first()

        // Then
        assertEquals("Step goal should be updated", newGoal, result)
    }

    @Test
    fun `measurementUnit returns default value initially`() = runTest {
        // When
        val measurementUnit = settingsManager.measurementUnit.first()

        // Then
        assertNotNull("Measurement unit should not be null", measurementUnit)
        assertTrue(
            "Measurement unit should be valid",
            measurementUnit.name == "METRIC" || measurementUnit.name == "IMPERIAL",
        )
    }

    @Test
    fun `updateMeasurementUnit updates setting correctly`() = runTest {
        // Given
        val newUnit = MeasurementUnit.IMPERIAL

        // When
        settingsManager.updateMeasurementUnit(newUnit)
        val result = settingsManager.measurementUnit.first()

        // Then
        assertEquals("Measurement unit should be updated", newUnit, result)
    }

    @Test
    fun `dailyCalorieTarget returns default value initially`() = runTest {
        // When
        val calorieTarget = settingsManager.dailyCalorieTarget.first()

        // Then
        assertTrue("Calorie target should be positive", calorieTarget > 0)
        assertEquals("Should default to 2000", 2000, calorieTarget)
    }

    @Test
    fun `updateDailyCalorieTarget updates setting correctly`() = runTest {
        // Given
        val newTarget = 2500

        // When
        settingsManager.updateDailyCalorieTarget(newTarget)
        val result = settingsManager.dailyCalorieTarget.first()

        // Then
        assertEquals("Calorie target should be updated", newTarget, result)
    }

    @Test
    fun `workoutReminders returns default value initially`() = runTest {
        // When
        val workoutReminders = settingsManager.workoutReminders.first()

        // Then
        assertNotNull("Workout reminders setting should not be null", workoutReminders)
        assertTrue("Should default to enabled", workoutReminders)
    }

    @Test
    fun `updateWorkoutReminders updates setting correctly`() = runTest {
        // Given
        val newValue = false

        // When
        settingsManager.updateWorkoutReminders(newValue)
        val result = settingsManager.workoutReminders.first()

        // Then
        assertEquals("Workout reminders should be updated", newValue, result)
    }

    @Test
    fun `notificationTime returns default value initially`() = runTest {
        // When
        val notificationTime = settingsManager.notificationTime.first()

        // Then
        assertNotNull("Notification time should not be null", notificationTime)
        assertEquals("Should default to 09:00", "09:00", notificationTime)
    }

    @Test
    fun `updateNotificationTime updates setting correctly`() = runTest {
        // Given
        val newTime = "18:30"

        // When
        settingsManager.updateNotificationTime(newTime)
        val result = settingsManager.notificationTime.first()

        // Then
        assertEquals("Notification time should be updated", newTime, result)
    }

    @Test
    fun `settings persist across manager instances`() = runTest {
        // Given
        val testValue = 12345
        settingsManager.updateStepGoal(testValue)

        // When - Create new manager instance
        val newSettingsManager = SettingsManager(context)
        val retrievedValue = newSettingsManager.stepGoal.first()

        // Then
        assertEquals("Settings should persist across instances", testValue, retrievedValue)
    }

    @Test
    fun `multiple setting updates work correctly`() = runTest {
        // Given
        val updates = listOf(5000, 8000, 12000, 15000)

        // When
        updates.forEach { settingsManager.updateStepGoal(it) }
        val finalValue = settingsManager.stepGoal.first()

        // Then
        assertEquals("Should have final updated value", updates.last(), finalValue)
    }

    @Test
    fun `boolean settings handle toggle correctly`() = runTest {
        // Given
        val initialValue = settingsManager.notificationEnabled.first()

        // When - Toggle
        settingsManager.updateNotificationsEnabled(!initialValue)
        val toggledValue = settingsManager.notificationEnabled.first()

        // Toggle back
        settingsManager.updateNotificationsEnabled(initialValue)
        val restoredValue = settingsManager.notificationEnabled.first()

        // Then
        assertEquals("Should toggle correctly", !initialValue, toggledValue)
        assertEquals("Should restore correctly", initialValue, restoredValue)
    }

    // API Surface Tests - Ensure methods exist and have correct signatures
    @Test
    fun `SettingsManager API surface test`() = runTest {
        // Test that all expected public methods exist with correct signatures
        try {
            // Flow properties
            settingsManager.measurementUnit
            settingsManager.dailyCalorieTarget
            settingsManager.stepGoal
            settingsManager.workoutReminders
            settingsManager.notificationEnabled
            settingsManager.notificationTime
            settingsManager.themeMode

            // Update methods
            settingsManager.updateMeasurementUnit(MeasurementUnit.METRIC)
            settingsManager.updateDailyCalorieTarget(2000)
            settingsManager.updateStepGoal(10000)
            settingsManager.updateWorkoutReminders(true)
            settingsManager.updateNotificationsEnabled(true)
            settingsManager.updateNotificationTime("09:00")
            settingsManager.updateThemeMode("system")

            // Success - API surface is stable
            assertTrue(true)
        } catch (e: NoSuchMethodError) {
            fail("API surface has changed: ${e.message}")
        }
    }

    @Test
    fun `string settings handle empty values`() = runTest {
        // Given
        val emptyString = ""

        // When
        settingsManager.updateNotificationTime(emptyString)
        val result = settingsManager.notificationTime.first()

        // Then
        assertEquals("Should handle empty string", emptyString, result)
    }

    @Test
    fun `numeric settings handle boundary values`() = runTest {
        // Given
        val minValue = 1
        val maxValue = 100000

        // When
        settingsManager.updateStepGoal(minValue)
        val minResult = settingsManager.stepGoal.first()

        settingsManager.updateStepGoal(maxValue)
        val maxResult = settingsManager.stepGoal.first()

        // Then
        assertEquals("Should handle minimum value", minValue, minResult)
        assertEquals("Should handle maximum value", maxValue, maxResult)
    }
}
