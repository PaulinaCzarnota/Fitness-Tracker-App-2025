package com.example.fitnesstrackerapp.util

/**
 * Unit tests for FitnessUtils
 *
 * Tests cover:
 * - Calorie calculations for different activities
 * - BMI calculations
 * - Data formatting utilities
 * - Distance and weight conversions
 */

import com.example.fitnesstrackerapp.settings.MeasurementUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class FitnessUtilsTest {

    @Test
    fun `calculateCaloriesBurned should return correct calories for running`() {
        // Given
        val durationMinutes = 30
        val weightKg = 70.0
        val metValue = FitnessUtils.MetValues.RUNNING

        // When
        val calories = FitnessUtils.calculateCaloriesBurned(durationMinutes, weightKg, metValue)

        // Then
        assertTrue("Calories should be positive", calories > 0)
        assertTrue("Calories should be reasonable for 30min running", calories > 200)
    }

    @Test
    fun `calculateCaloriesBurned should return correct calories for cycling`() {
        // Given
        val durationMinutes = 60
        val weightKg = 70.0
        val metValue = FitnessUtils.MetValues.CYCLING_MODERATE

        // When
        val calories = FitnessUtils.calculateCaloriesBurned(durationMinutes, weightKg, metValue)

        // Then
        assertTrue("Calories should be positive", calories > 0)
        assertTrue("Calories should be reasonable for 60min cycling", calories > 200)
    }

    @Test
    fun `calculateBMI should return correct BMI value`() {
        // Given
        val weightKg = 70.0
        val heightM = 1.75

        // When
        val bmi = FitnessUtils.calculateBMI(weightKg, heightM)

        // Then
        val expectedBMI = 22.9 // 70 / (1.75 * 1.75) rounded to 1 decimal
        assertEquals("BMI should be calculated correctly", expectedBMI, bmi, 0.1)
    }

    @Test
    fun `formatDuration should format minutes correctly`() {
        // Given
        val minutes = 125

        // When
        val formatted = FitnessUtils.formatDuration(minutes)

        // Then
        assertEquals("Should format as hours and minutes", "2h 5m", formatted)
    }

    @Test
    fun `formatDuration should handle zero minutes`() {
        // Given
        val minutes = 0

        // When
        val formatted = FitnessUtils.formatDuration(minutes)

        // Then
        assertEquals("Should format zero as 0m", "0m", formatted)
    }

    @Test
    fun `formatDuration should handle less than hour`() {
        // Given
        val minutes = 45

        // When
        val formatted = FitnessUtils.formatDuration(minutes)

        // Then
        assertEquals("Should format as minutes only", "45m", formatted)
    }

    @Test
    fun `calculatePace should return correct pace string`() {
        // Given
        val distanceKm = 5.0
        val timeMinutes = 25

        // When
        val pace = FitnessUtils.calculatePace(distanceKm, timeMinutes)

        // Then
        assertEquals("Pace should be formatted correctly", "5:00", pace)
    }

    @Test
    fun `convertDistance should convert km to miles correctly`() {
        // Given
        val distanceKm = 10.0

        // When
        val distanceMiles = FitnessUtils.convertDistance(distanceKm, MeasurementUnit.METRIC, MeasurementUnit.IMPERIAL)

        // Then
        assertEquals("Should convert km to miles", 6.21, distanceMiles, 0.01)
    }

    @Test
    fun `convertWeight should convert kg to lbs correctly`() {
        // Given
        val weightKg = 70.0

        // When
        val weightLbs = FitnessUtils.convertWeight(weightKg, MeasurementUnit.METRIC, MeasurementUnit.IMPERIAL)

        // Then
        assertEquals("Should convert kg to lbs", 154.3, weightLbs, 0.1)
    }

    @Test
    fun `formatDistance should format metric distance correctly`() {
        // Given
        val distance = 5.5

        // When
        val formatted = FitnessUtils.formatDistance(distance, MeasurementUnit.METRIC)

        // Then
        assertEquals("Should format metric distance", "5.5 km", formatted)
    }

    @Test
    fun `formatWeight should format metric weight correctly`() {
        // Given
        val weight = 70.5

        // When
        val formatted = FitnessUtils.formatWeight(weight, MeasurementUnit.METRIC)

        // Then
        assertEquals("Should format metric weight", "70.5 kg", formatted)
    }

    @Test
    fun `calculateHeartRateZones should return correct zones`() {
        // Given
        val maxHeartRate = 200

        // When
        val zones = FitnessUtils.calculateHeartRateZones(maxHeartRate)

        // Then
        assertEquals("Should return 5 zones", 5, zones.size)
        assertEquals("Zone 1 should be 50-60%", 100..120, zones[0])
        assertEquals("Zone 5 should be 90-100%", 180..200, zones[4])
    }
}
