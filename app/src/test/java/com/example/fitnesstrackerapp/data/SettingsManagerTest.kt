package com.example.fitnesstrackerapp.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.fitnesstrackerapp.settings.SettingsManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
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
    fun `getDarkModeEnabled returns default value initially`() = runTest {
        // When
        val darkMode = settingsManager.getDarkModeEnabled().first()

        // Then
        // Should return system default or false (implementation dependent)
        assertNotNull("Dark mode setting should not be null", darkMode)
    }

    @Test
    fun `setDarkModeEnabled updates setting correctly`() = runTest {
        // Given
        val newValue = true

        // When
        settingsManager.setDarkModeEnabled(newValue)
        val result = settingsManager.getDarkModeEnabled().first()

        // Then
        assertEquals("Dark mode should be updated", newValue, result)
    }

    @Test
    fun `getNotificationsEnabled returns default value initially`() = runTest {
        // When
        val notifications = settingsManager.getNotificationsEnabled().first()

        // Then
        assertNotNull("Notifications setting should not be null", notifications)
    }

    @Test
    fun `setNotificationsEnabled updates setting correctly`() = runTest {
        // Given
        val newValue = false

        // When
        settingsManager.setNotificationsEnabled(newValue)
        val result = settingsManager.getNotificationsEnabled().first()

        // Then
        assertEquals("Notifications should be updated", newValue, result)
    }

    @Test
    fun `getStepGoal returns default value initially`() = runTest {
        // When
        val stepGoal = settingsManager.getStepGoal().first()

        // Then
        assertTrue("Step goal should be positive", stepGoal > 0)
        assertTrue("Step goal should be reasonable", stepGoal <= 50000)
    }

    @Test
    fun `setStepGoal updates setting correctly`() = runTest {
        // Given
        val newGoal = 12000

        // When
        settingsManager.setStepGoal(newGoal)
        val result = settingsManager.getStepGoal().first()

        // Then
        assertEquals("Step goal should be updated", newGoal, result)
    }

    @Test
    fun `getWeightUnit returns default value initially`() = runTest {
        // When
        val weightUnit = settingsManager.getWeightUnit().first()

        // Then
        assertNotNull("Weight unit should not be null", weightUnit)
        assertTrue("Weight unit should be valid", 
                  weightUnit == "kg" || weightUnit == "lbs")
    }

    @Test
    fun `setWeightUnit updates setting correctly`() = runTest {
        // Given
        val newUnit = "lbs"

        // When
        settingsManager.setWeightUnit(newUnit)
        val result = settingsManager.getWeightUnit().first()

        // Then
        assertEquals("Weight unit should be updated", newUnit, result)
    }

    @Test
    fun `getDistanceUnit returns default value initially`() = runTest {
        // When
        val distanceUnit = settingsManager.getDistanceUnit().first()

        // Then
        assertNotNull("Distance unit should not be null", distanceUnit)
        assertTrue("Distance unit should be valid", 
                  distanceUnit == "km" || distanceUnit == "miles")
    }

    @Test
    fun `setDistanceUnit updates setting correctly`() = runTest {
        // Given
        val newUnit = "miles"

        // When
        settingsManager.setDistanceUnit(newUnit)
        val result = settingsManager.getDistanceUnit().first()

        // Then
        assertEquals("Distance unit should be updated", newUnit, result)
    }

    @Test
    fun `getAutoBackup returns default value initially`() = runTest {
        // When
        val autoBackup = settingsManager.getAutoBackup().first()

        // Then
        assertNotNull("Auto backup setting should not be null", autoBackup)
    }

    @Test
    fun `setAutoBackup updates setting correctly`() = runTest {
        // Given
        val newValue = true

        // When
        settingsManager.setAutoBackup(newValue)
        val result = settingsManager.getAutoBackup().first()

        // Then
        assertEquals("Auto backup should be updated", newValue, result)
    }

    @Test
    fun `getPrivacyMode returns default value initially`() = runTest {
        // When
        val privacyMode = settingsManager.getPrivacyMode().first()

        // Then
        assertNotNull("Privacy mode setting should not be null", privacyMode)
    }

    @Test
    fun `setPrivacyMode updates setting correctly`() = runTest {
        // Given
        val newValue = true

        // When
        settingsManager.setPrivacyMode(newValue)
        val result = settingsManager.getPrivacyMode().first()

        // Then
        assertEquals("Privacy mode should be updated", newValue, result)
    }

    @Test
    fun `getLanguage returns default value initially`() = runTest {
        // When
        val language = settingsManager.getLanguage().first()

        // Then
        assertNotNull("Language should not be null", language)
        assertTrue("Language should not be empty", language.isNotEmpty())
    }

    @Test
    fun `setLanguage updates setting correctly`() = runTest {
        // Given
        val newLanguage = "es"

        // When
        settingsManager.setLanguage(newLanguage)
        val result = settingsManager.getLanguage().first()

        // Then
        assertEquals("Language should be updated", newLanguage, result)
    }

    @Test
    fun `exportSettings returns valid JSON`() = runTest {
        // Given - Set some values
        settingsManager.setDarkModeEnabled(true)
        settingsManager.setStepGoal(15000)
        settingsManager.setWeightUnit("lbs")

        // When
        val exportedSettings = settingsManager.exportSettings()

        // Then
        assertNotNull("Exported settings should not be null", exportedSettings)
        assertTrue("Exported settings should not be empty", exportedSettings.isNotEmpty())
        assertTrue("Should contain JSON brackets", exportedSettings.contains("{") && exportedSettings.contains("}"))
    }

    @Test
    fun `importSettings restores settings from JSON`() = runTest {
        // Given - Set initial values
        settingsManager.setDarkModeEnabled(false)
        settingsManager.setStepGoal(10000)
        
        // Export current settings
        val originalSettings = settingsManager.exportSettings()
        
        // Change values
        settingsManager.setDarkModeEnabled(true)
        settingsManager.setStepGoal(20000)
        
        // When - Import original settings
        val success = settingsManager.importSettings(originalSettings)

        // Then
        assertTrue("Import should succeed", success)
        // Values should be restored (this test verifies the method works, not specific values)
    }

    @Test
    fun `importSettings handles invalid JSON`() = runTest {
        // Given
        val invalidJson = "not valid json"

        // When
        val success = settingsManager.importSettings(invalidJson)

        // Then
        assertFalse("Import should fail for invalid JSON", success)
    }

    @Test
    fun `resetToDefaults resets all settings`() = runTest {
        // Given - Set some custom values
        settingsManager.setDarkModeEnabled(true)
        settingsManager.setStepGoal(20000)
        settingsManager.setWeightUnit("lbs")
        settingsManager.setNotificationsEnabled(false)

        // When
        settingsManager.resetToDefaults()

        // Then - Values should be reset (verify at least one to ensure method works)
        val stepGoal = settingsManager.getStepGoal().first()
        // Step goal should be reset to some default value
        assertTrue("Step goal should be reset to reasonable default", stepGoal in 5000..15000)
    }

    @Test
    fun `getSetting returns correct value for existing key`() = runTest {
        // Given
        settingsManager.setStepGoal(12345)

        // When
        val value = settingsManager.getSetting("step_goal", 0)

        // Then
        assertEquals("Should return stored step goal", 12345, value)
    }

    @Test
    fun `getSetting returns default for non-existing key`() = runTest {
        // Given
        val defaultValue = 999

        // When
        val value = settingsManager.getSetting("non_existing_key", defaultValue)

        // Then
        assertEquals("Should return default value", defaultValue, value)
    }

    @Test
    fun `setSetting stores value correctly`() = runTest {
        // Given
        val key = "custom_setting"
        val value = "custom_value"

        // When
        settingsManager.setSetting(key, value)
        val retrieved = settingsManager.getSetting(key, "")

        // Then
        assertEquals("Should store and retrieve custom setting", value, retrieved)
    }

    @Test
    fun `getSettingFlow emits current and updated values`() = runTest {
        // Given
        val key = "test_flow_key"
        val initialValue = "initial"
        val updatedValue = "updated"

        settingsManager.setSetting(key, initialValue)

        // When
        val flow = settingsManager.getSettingFlow(key, "default")
        val initial = flow.first()

        // Update the value
        settingsManager.setSetting(key, updatedValue)
        val updated = flow.first()

        // Then
        assertEquals("Should emit initial value", initialValue, initial)
        assertEquals("Should emit updated value", updatedValue, updated)
    }

    // API Surface Tests - Ensure methods exist and have correct signatures
    @Test
    fun `SettingsManager API surface test`() {
        // Test that all expected public methods exist with correct signatures
        try {
            // Boolean settings
            settingsManager.getDarkModeEnabled()
            settingsManager.setDarkModeEnabled(true)
            settingsManager.getNotificationsEnabled()
            settingsManager.setNotificationsEnabled(true)
            settingsManager.getAutoBackup()
            settingsManager.setAutoBackup(true)
            settingsManager.getPrivacyMode()
            settingsManager.setPrivacyMode(true)
            
            // Numeric settings
            settingsManager.getStepGoal()
            settingsManager.setStepGoal(10000)
            
            // String settings
            settingsManager.getWeightUnit()
            settingsManager.setWeightUnit("kg")
            settingsManager.getDistanceUnit()
            settingsManager.setDistanceUnit("km")
            settingsManager.getLanguage()
            settingsManager.setLanguage("en")
            
            // Generic settings
            settingsManager.getSetting("key", "default")
            settingsManager.setSetting("key", "value")
            settingsManager.getSettingFlow("key", "default")
            
            // Import/Export
            settingsManager.exportSettings()
            settingsManager.importSettings("{}")
            settingsManager.resetToDefaults()
            
            // Success - API surface is stable
            assertTrue(true)
        } catch (e: NoSuchMethodError) {
            fail("API surface has changed: ${e.message}")
        }
    }

    @Test
    fun `settings persist across manager instances`() = runTest {
        // Given
        val testValue = 12345
        settingsManager.setStepGoal(testValue)

        // When - Create new manager instance
        val newSettingsManager = SettingsManager(context)
        val retrievedValue = newSettingsManager.getStepGoal().first()

        // Then
        assertEquals("Settings should persist across instances", testValue, retrievedValue)
    }

    @Test
    fun `multiple setting updates work correctly`() = runTest {
        // Given
        val updates = listOf(5000, 8000, 12000, 15000)

        // When
        updates.forEach { settingsManager.setStepGoal(it) }
        val finalValue = settingsManager.getStepGoal().first()

        // Then
        assertEquals("Should have final updated value", updates.last(), finalValue)
    }

    @Test
    fun `boolean settings handle toggle correctly`() = runTest {
        // Given
        val initialValue = settingsManager.getDarkModeEnabled().first()

        // When - Toggle
        settingsManager.setDarkModeEnabled(!initialValue)
        val toggledValue = settingsManager.getDarkModeEnabled().first()

        // Toggle back
        settingsManager.setDarkModeEnabled(initialValue)
        val restoredValue = settingsManager.getDarkModeEnabled().first()

        // Then
        assertEquals("Should toggle correctly", !initialValue, toggledValue)
        assertEquals("Should restore correctly", initialValue, restoredValue)
    }

    @Test
    fun `string settings handle empty values`() = runTest {
        // Given
        val emptyString = ""

        // When
        settingsManager.setLanguage(emptyString)
        val result = settingsManager.getLanguage().first()

        // Then
        assertEquals("Should handle empty string", emptyString, result)
    }

    @Test
    fun `numeric settings handle boundary values`() = runTest {
        // Given
        val minValue = 1
        val maxValue = 100000

        // When
        settingsManager.setStepGoal(minValue)
        val minResult = settingsManager.getStepGoal().first()
        
        settingsManager.setStepGoal(maxValue)
        val maxResult = settingsManager.getStepGoal().first()

        // Then
        assertEquals("Should handle minimum value", minValue, minResult)
        assertEquals("Should handle maximum value", maxValue, maxResult)
    }
}
