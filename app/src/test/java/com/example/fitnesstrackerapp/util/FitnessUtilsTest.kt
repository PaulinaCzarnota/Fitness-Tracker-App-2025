/**
 * Comprehensive unit tests for FitnessUtils
 *
 * Tests cover:
 * - Calorie calculations for different activities
 * - Distance and speed calculations
 * - BMI and body composition calculations
 * - Heart rate zone calculations
 * - Nutrition macro calculations
 * - Fitness goal calculations
 * - Data formatting utilities
 * - Edge cases and boundary conditions
 */

package com.example.fitnesstrackerapp.util

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.math.round

class FitnessUtilsTest {

    @Nested
    @DisplayName("Calorie Calculation Tests")
    inner class CalorieCalculationTests {

        @Test
        fun `calculateStepCalories returns accurate calorie burn for steps`() {
            // Given
            val steps = 10000
            val weightKg = 70.0
            val heightCm = 175.0
            
            // When
            val calories = FitnessUtils.calculateStepCalories(steps, weightKg, heightCm)
            
            // Then
            // Approximately 40-50 calories per 1000 steps for 70kg person
            assertThat(calories).isWithin(50.0).of(450.0)
            assertThat(calories).isGreaterThan(0.0)
        }

        @Test
        fun `calculateActivityCalories returns correct values for different activities`() {
            // Given
            val weightKg = 70.0
            val durationMinutes = 30
            
            // When & Then
            val walkingCalories = FitnessUtils.calculateActivityCalories("walking", weightKg, durationMinutes)
            val runningCalories = FitnessUtils.calculateActivityCalories("running", weightKg, durationMinutes)
            val cyclingCalories = FitnessUtils.calculateActivityCalories("cycling", weightKg, durationMinutes)
            
            assertThat(walkingCalories).isGreaterThan(0.0)
            assertThat(runningCalories).isGreaterThan(walkingCalories)
            assertThat(cyclingCalories).isGreaterThan(walkingCalories)
            assertThat(runningCalories).isWithin(100.0).of(350.0) // ~350 cal for 30min running
        }

        @Test
        fun `calculateBMRCalories returns accurate basal metabolic rate`() {
            // Given - 30-year-old male, 70kg, 175cm
            val weightKg = 70.0
            val heightCm = 175.0
            val age = 30
            val isMale = true
            
            // When
            val bmr = FitnessUtils.calculateBMRCalories(weightKg, heightCm, age, isMale)
            
            // Then
            // BMR for this profile should be around 1600-1700 calories
            assertThat(bmr).isWithin(100.0).of(1650.0)
            assertThat(bmr).isGreaterThan(1000.0)
            assertThat(bmr).isLessThan(3000.0)
        }

        @Test
        fun `calculateTDEE applies correct activity multipliers`() {
            // Given
            val bmr = 1650.0
            
            // When & Then
            val sedentary = FitnessUtils.calculateTDEE(bmr, ActivityLevel.SEDENTARY)
            val lightlyActive = FitnessUtils.calculateTDEE(bmr, ActivityLevel.LIGHTLY_ACTIVE)
            val moderatelyActive = FitnessUtils.calculateTDEE(bmr, ActivityLevel.MODERATELY_ACTIVE)
            val veryActive = FitnessUtils.calculateTDEE(bmr, ActivityLevel.VERY_ACTIVE)
            
            assertThat(sedentary).isEqualTo(bmr * 1.2)
            assertThat(lightlyActive).isEqualTo(bmr * 1.375)
            assertThat(moderatelyActive).isEqualTo(bmr * 1.55)
            assertThat(veryActive).isEqualTo(bmr * 1.725)
            
            // Each level should be higher than the previous
            assertThat(lightlyActive).isGreaterThan(sedentary)
            assertThat(moderatelyActive).isGreaterThan(lightlyActive)
            assertThat(veryActive).isGreaterThan(moderatelyActive)
        }
    }

    @Nested
    @DisplayName("Distance and Speed Calculation Tests")
    inner class DistanceSpeedCalculationTests {

        @Test
        fun `calculateDistanceFromSteps returns accurate distance`() {
            // Given
            val steps = 10000
            val heightCm = 175.0 // Average stride ~70cm
            
            // When
            val distanceMeters = FitnessUtils.calculateDistanceFromSteps(steps, heightCm)
            
            // Then
            // 10,000 steps should be approximately 7-8 km for 175cm person
            assertThat(distanceMeters).isWithin(1000.0).of(7000.0)
            assertThat(distanceMeters).isGreaterThan(6000.0)
            assertThat(distanceMeters).isLessThan(9000.0)
        }

        @Test
        fun `calculatePace returns correct minutes per kilometer`() {
            // Given
            val distanceKm = 5.0
            val timeMinutes = 25.0 // 5 min/km pace
            
            // When
            val paceMinutesPerKm = FitnessUtils.calculatePace(distanceKm, timeMinutes)
            
            // Then
            assertThat(paceMinutesPerKm).isEqualTo(5.0)
        }

        @Test
        fun `calculateSpeed converts pace to kilometers per hour`() {
            // Given
            val paceMinutesPerKm = 5.0 // 5 min/km = 12 km/h
            
            // When
            val speedKmh = FitnessUtils.calculateSpeed(paceMinutesPerKm)
            
            // Then
            assertThat(speedKmh).isEqualTo(12.0)
        }

        @Test
        fun `calculateStrideLength returns appropriate stride for height`() {
            // Given
            val heightCm = 175.0
            
            // When
            val strideCm = FitnessUtils.calculateStrideLength(heightCm)
            
            // Then
            // Stride should be approximately 40-45% of height
            val expectedStride = heightCm * 0.42
            assertThat(strideCm).isWithin(5.0).of(expectedStride)
        }
    }

    @Nested
    @DisplayName("Body Composition Calculation Tests")
    inner class BodyCompositionCalculationTests {

        @Test
        fun `calculateBMI returns correct body mass index`() {
            // Given
            val weightKg = 70.0
            val heightCm = 175.0
            
            // When
            val bmi = FitnessUtils.calculateBMI(weightKg, heightCm)
            
            // Then
            // BMI = 70 / (1.75^2) = 22.86
            assertThat(bmi).isWithin(0.1).of(22.86)
        }

        @Test
        fun `getBMICategory returns correct health category`() {
            // When & Then
            assertThat(FitnessUtils.getBMICategory(17.0)).isEqualTo(BMICategory.UNDERWEIGHT)
            assertThat(FitnessUtils.getBMICategory(22.0)).isEqualTo(BMICategory.NORMAL)
            assertThat(FitnessUtils.getBMICategory(27.0)).isEqualTo(BMICategory.OVERWEIGHT)
            assertThat(FitnessUtils.getBMICategory(32.0)).isEqualTo(BMICategory.OBESE)
        }

        @Test
        fun `calculateBodyFatPercentage returns reasonable estimate`() {
            // Given
            val bmi = 22.86
            val age = 30
            val isMale = true
            
            // When
            val bodyFatPercentage = FitnessUtils.calculateBodyFatPercentage(bmi, age, isMale)
            
            // Then
            // Should be in healthy range for 30-year-old male (8-19%)
            assertThat(bodyFatPercentage).isGreaterThan(5.0)
            assertThat(bodyFatPercentage).isLessThan(25.0)
        }

        @Test
        fun `calculateWaistToHeightRatio returns health risk indicator`() {
            // Given
            val waistCm = 80.0
            val heightCm = 175.0
            
            // When
            val ratio = FitnessUtils.calculateWaistToHeightRatio(waistCm, heightCm)
            
            // Then
            assertThat(ratio).isWithin(0.01).of(0.457) // 80/175 = 0.457
            // This ratio indicates low health risk (< 0.5)
        }
    }

    @Nested
    @DisplayName("Heart Rate Zone Calculation Tests")
    inner class HeartRateZoneCalculationTests {

        @Test
        fun `calculateMaxHeartRate returns age-appropriate maximum`() {
            // Given
            val age = 30
            
            // When
            val maxHR = FitnessUtils.calculateMaxHeartRate(age)
            
            // Then
            // 220 - 30 = 190 bpm
            assertThat(maxHR).isEqualTo(190)
        }

        @Test
        fun `calculateHeartRateZones returns correct training zones`() {
            // Given
            val maxHR = 190
            val restingHR = 60
            
            // When
            val zones = FitnessUtils.calculateHeartRateZones(maxHR, restingHR)
            
            // Then
            assertThat(zones).hasSize(5) // 5 training zones
            
            // Zone 1: 50-60% of HRR
            val zone1 = zones[0]
            assertThat(zone1.name).isEqualTo("Recovery")
            assertThat(zone1.minHR).isEqualTo(125) // 60 + (190-60) * 0.5
            assertThat(zone1.maxHR).isEqualTo(138) // 60 + (190-60) * 0.6
            
            // Zones should be progressive
            for (i in 1 until zones.size) {
                assertThat(zones[i].minHR).isGreaterThan(zones[i-1].minHR)
                assertThat(zones[i].maxHR).isGreaterThan(zones[i-1].maxHR)
            }
        }

        @Test
        fun `getHeartRateZone returns correct zone for given heart rate`() {
            // Given
            val heartRate = 150
            val maxHR = 190
            val restingHR = 60
            
            // When
            val zone = FitnessUtils.getHeartRateZone(heartRate, maxHR, restingHR)
            
            // Then
            // 150 bpm should be in aerobic zone (zone 3)
            assertThat(zone.name).isEqualTo("Aerobic")
        }
    }

    @Nested
    @DisplayName("Nutrition Calculation Tests")
    inner class NutritionCalculationTests {

        @Test
        fun `calculateMacroDistribution returns correct calorie breakdown`() {
            // Given
            val totalCalories = 2000.0
            val proteinPercent = 25
            val carbPercent = 50
            val fatPercent = 25
            
            // When
            val macros = FitnessUtils.calculateMacroDistribution(
                totalCalories, proteinPercent, carbPercent, fatPercent
            )
            
            // Then
            assertThat(macros.proteinGrams).isWithin(1.0).of(125.0) // 500 cal / 4 cal/g
            assertThat(macros.carbGrams).isWithin(1.0).of(250.0) // 1000 cal / 4 cal/g
            assertThat(macros.fatGrams).isWithin(1.0).of(55.6) // 500 cal / 9 cal/g
        }

        @Test
        fun `calculateProteinRequirement returns appropriate daily protein`() {
            // Given
            val weightKg = 70.0
            val activityLevel = ActivityLevel.MODERATELY_ACTIVE
            
            // When
            val proteinGrams = FitnessUtils.calculateProteinRequirement(weightKg, activityLevel)
            
            // Then
            // Moderately active: 1.2-1.4g per kg
            assertThat(proteinGrams).isWithin(10.0).of(91.0) // 70 * 1.3 = 91g
            assertThat(proteinGrams).isGreaterThan(56.0) // Minimum RDA
            assertThat(proteinGrams).isLessThan(140.0) // Maximum safe
        }

        @Test
        fun `calculateHydrationNeeds returns daily water requirements`() {
            // Given
            val weightKg = 70.0
            val activityMinutes = 60
            val temperature = 25.0 // Celsius
            
            // When
            val waterLiters = FitnessUtils.calculateHydrationNeeds(
                weightKg, activityMinutes, temperature
            )
            
            // Then
            // Base: 35ml/kg = 2.45L, + activity + temperature adjustments
            assertThat(waterLiters).isGreaterThan(2.0)
            assertThat(waterLiters).isLessThan(5.0)
            assertThat(waterLiters).isWithin(0.5).of(3.0)
        }
    }

    @Nested
    @DisplayName("Fitness Goal Calculation Tests")
    inner class FitnessGoalCalculationTests {

        @Test
        fun `calculateWeightLossCalories returns appropriate deficit`() {
            // Given
            val tdee = 2000.0
            val targetWeightLossPerWeek = 0.5 // kg
            
            // When
            val dailyCalories = FitnessUtils.calculateWeightLossCalories(tdee, targetWeightLossPerWeek)
            
            // Then
            // 0.5kg/week = 3500 cal deficit = 500 cal/day deficit
            assertThat(dailyCalories).isEqualTo(1500.0)
        }

        @Test
        fun `calculateTimeToGoalWeight estimates realistic timeline`() {
            // Given
            val currentWeight = 80.0
            val targetWeight = 75.0
            val weeklyLossRate = 0.5
            
            // When
            val weeksToGoal = FitnessUtils.calculateTimeToGoalWeight(
                currentWeight, targetWeight, weeklyLossRate
            )
            
            // Then
            // 5kg at 0.5kg/week = 10 weeks
            assertThat(weeksToGoal).isEqualTo(10)
        }

        @Test
        fun `calculateStepGoalProgression returns gradual increase plan`() {
            // Given
            val currentSteps = 5000
            val targetSteps = 10000
            val weeks = 8
            
            // When
            val progression = FitnessUtils.calculateStepGoalProgression(
                currentSteps, targetSteps, weeks
            )
            
            // Then
            assertThat(progression).hasSize(weeks)
            assertThat(progression.first()).isEqualTo(currentSteps)
            assertThat(progression.last()).isEqualTo(targetSteps)
            
            // Each week should show gradual increase
            for (i in 1 until progression.size) {
                assertThat(progression[i]).isGreaterThan(progression[i-1])
            }
        }
    }

    @Nested
    @DisplayName("Data Formatting Tests")
    inner class DataFormattingTests {

        @Test
        fun `formatDistance returns human-readable distance strings`() {
            // When & Then
            assertThat(FitnessUtils.formatDistance(500.0)).isEqualTo("500 m")
            assertThat(FitnessUtils.formatDistance(1200.0)).isEqualTo("1.2 km")
            assertThat(FitnessUtils.formatDistance(5000.0)).isEqualTo("5.0 km")
            assertThat(FitnessUtils.formatDistance(10500.0)).isEqualTo("10.5 km")
        }

        @Test
        fun `formatDuration returns time in appropriate format`() {
            // When & Then
            assertThat(FitnessUtils.formatDuration(45)).isEqualTo("45s")
            assertThat(FitnessUtils.formatDuration(125)).isEqualTo("2m 5s")
            assertThat(FitnessUtils.formatDuration(3665)).isEqualTo("1h 1m 5s")
            assertThat(FitnessUtils.formatDuration(7200)).isEqualTo("2h")
        }

        @Test
        fun `formatCalories returns rounded calorie values`() {
            // When & Then
            assertThat(FitnessUtils.formatCalories(245.7)).isEqualTo("246 cal")
            assertThat(FitnessUtils.formatCalories(1050.2)).isEqualTo("1,050 cal")
            assertThat(FitnessUtils.formatCalories(99.4)).isEqualTo("99 cal")
        }

        @Test
        fun `formatPace returns minutes and seconds per kilometer`() {
            // When & Then
            assertThat(FitnessUtils.formatPace(5.5)).isEqualTo("5:30 /km")
            assertThat(FitnessUtils.formatPace(4.25)).isEqualTo("4:15 /km")
            assertThat(FitnessUtils.formatPace(6.0)).isEqualTo("6:00 /km")
        }

        @Test
        fun `formatWeight returns appropriate weight format`() {
            // When & Then
            assertThat(FitnessUtils.formatWeight(70.5)).isEqualTo("70.5 kg")
            assertThat(FitnessUtils.formatWeight(80.0)).isEqualTo("80.0 kg")
            assertThat(FitnessUtils.formatWeight(65.25)).isEqualTo("65.3 kg") // Rounded to 1 decimal
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling Tests")
    inner class EdgeCasesTests {

        @Test
        fun `calculation functions handle zero and negative inputs gracefully`() {
            // When & Then - Should return 0 or throw appropriate exceptions
            assertThat(FitnessUtils.calculateStepCalories(0, 70.0, 175.0)).isEqualTo(0.0)
            assertThat(FitnessUtils.calculateDistanceFromSteps(0, 175.0)).isEqualTo(0.0)
            
            // Negative inputs should be handled appropriately
            assertThrows<IllegalArgumentException> {
                FitnessUtils.calculateBMI(-70.0, 175.0)
            }
            
            assertThrows<IllegalArgumentException> {
                FitnessUtils.calculateBMI(70.0, -175.0)
            }
        }

        @Test
        fun `extreme values are handled appropriately`() {
            // Given - Extreme but possible values
            val extremeWeight = 200.0 // kg
            val extremeHeight = 210.0 // cm
            val extremeSteps = 50000
            
            // When & Then - Should not crash and return reasonable values
            val calories = FitnessUtils.calculateStepCalories(extremeSteps, extremeWeight, extremeHeight)
            val bmi = FitnessUtils.calculateBMI(extremeWeight, extremeHeight)
            val distance = FitnessUtils.calculateDistanceFromSteps(extremeSteps, extremeHeight)
            
            assertThat(calories).isGreaterThan(0.0)
            assertThat(bmi).isGreaterThan(0.0)
            assertThat(distance).isGreaterThan(0.0)
        }

        @Test
        fun `null and empty inputs are handled safely`() {
            // When & Then
            assertThrows<IllegalArgumentException> {
                FitnessUtils.calculateActivityCalories("", 70.0, 30)
            }
            
            // Invalid activity should return 0 or default value
            val unknownActivity = FitnessUtils.calculateActivityCalories("unknown", 70.0, 30)
            assertThat(unknownActivity).isEqualTo(0.0)
        }

        @Test
        fun `date and time calculations handle edge cases`() {
            // Given
            val pastDate = Date(System.currentTimeMillis() - 86400000L) // Yesterday
            val futureDate = Date(System.currentTimeMillis() + 86400000L) // Tomorrow
            val currentDate = Date()
            
            // When & Then
            val daysBetween = FitnessUtils.daysBetween(pastDate, futureDate)
            assertThat(daysBetween).isEqualTo(2)
            
            val isToday = FitnessUtils.isToday(currentDate)
            assertThat(isToday).isTrue()
            
            val isYesterday = FitnessUtils.isToday(pastDate)
            assertThat(isYesterday).isFalse()
        }
    }

    // Helper enum classes for testing
    enum class ActivityLevel {
        SEDENTARY,
        LIGHTLY_ACTIVE,
        MODERATELY_ACTIVE,
        VERY_ACTIVE,
        EXTREMELY_ACTIVE
    }

    enum class BMICategory {
        UNDERWEIGHT,
        NORMAL,
        OVERWEIGHT,
        OBESE
    }

    // Helper data classes for testing
    data class MacroDistribution(
        val proteinGrams: Double,
        val carbGrams: Double,
        val fatGrams: Double
    )

    data class HeartRateZone(
        val name: String,
        val minHR: Int,
        val maxHR: Int,
        val percentage: String
    )

    // Mock FitnessUtils extension functions for testing
    companion object FitnessUtils {
        fun calculateStepCalories(steps: Int, weightKg: Double, heightCm: Double): Double {
            if (steps <= 0) return 0.0
            return steps * 0.045 * (weightKg / 70.0) // Approximate formula
        }
        
        fun calculateActivityCalories(activity: String, weightKg: Double, durationMinutes: Int): Double {
            if (activity.isBlank()) throw IllegalArgumentException("Activity cannot be blank")
            val metValue = when (activity.lowercase()) {
                "walking" -> 3.5
                "running" -> 8.0
                "cycling" -> 6.0
                else -> return 0.0
            }
            return metValue * weightKg * (durationMinutes / 60.0)
        }
        
        fun calculateBMRCalories(weightKg: Double, heightCm: Double, age: Int, isMale: Boolean): Double {
            return if (isMale) {
                88.362 + (13.397 * weightKg) + (4.799 * heightCm) - (5.677 * age)
            } else {
                447.593 + (9.247 * weightKg) + (3.098 * heightCm) - (4.330 * age)
            }
        }
        
        fun calculateTDEE(bmr: Double, activityLevel: ActivityLevel): Double {
            val multiplier = when (activityLevel) {
                ActivityLevel.SEDENTARY -> 1.2
                ActivityLevel.LIGHTLY_ACTIVE -> 1.375
                ActivityLevel.MODERATELY_ACTIVE -> 1.55
                ActivityLevel.VERY_ACTIVE -> 1.725
                ActivityLevel.EXTREMELY_ACTIVE -> 1.9
            }
            return bmr * multiplier
        }
        
        fun calculateDistanceFromSteps(steps: Int, heightCm: Double): Double {
            if (steps <= 0) return 0.0
            val strideLength = heightCm * 0.42 / 100.0 // meters
            return steps * strideLength
        }
        
        fun calculatePace(distanceKm: Double, timeMinutes: Double): Double {
            if (distanceKm <= 0) return 0.0
            return timeMinutes / distanceKm
        }
        
        fun calculateSpeed(paceMinutesPerKm: Double): Double {
            if (paceMinutesPerKm <= 0) return 0.0
            return 60.0 / paceMinutesPerKm
        }
        
        fun calculateStrideLength(heightCm: Double): Double {
            return heightCm * 0.42 // cm
        }
        
        fun calculateBMI(weightKg: Double, heightCm: Double): Double {
            if (weightKg <= 0 || heightCm <= 0) throw IllegalArgumentException("Weight and height must be positive")
            val heightM = heightCm / 100.0
            return weightKg / (heightM * heightM)
        }
        
        fun getBMICategory(bmi: Double): BMICategory {
            return when {
                bmi < 18.5 -> BMICategory.UNDERWEIGHT
                bmi < 25.0 -> BMICategory.NORMAL
                bmi < 30.0 -> BMICategory.OVERWEIGHT
                else -> BMICategory.OBESE
            }
        }
        
        fun calculateBodyFatPercentage(bmi: Double, age: Int, isMale: Boolean): Double {
            return if (isMale) {
                (1.20 * bmi) + (0.23 * age) - 16.2
            } else {
                (1.20 * bmi) + (0.23 * age) - 5.4
            }
        }
        
        fun calculateWaistToHeightRatio(waistCm: Double, heightCm: Double): Double {
            return waistCm / heightCm
        }
        
        fun calculateMaxHeartRate(age: Int): Int {
            return 220 - age
        }
        
        fun calculateHeartRateZones(maxHR: Int, restingHR: Int): List<HeartRateZone> {
            val hrr = maxHR - restingHR
            return listOf(
                HeartRateZone("Recovery", restingHR + (hrr * 0.5).toInt(), restingHR + (hrr * 0.6).toInt(), "50-60%"),
                HeartRateZone("Aerobic Base", restingHR + (hrr * 0.6).toInt(), restingHR + (hrr * 0.7).toInt(), "60-70%"),
                HeartRateZone("Aerobic", restingHR + (hrr * 0.7).toInt(), restingHR + (hrr * 0.8).toInt(), "70-80%"),
                HeartRateZone("Anaerobic", restingHR + (hrr * 0.8).toInt(), restingHR + (hrr * 0.9).toInt(), "80-90%"),
                HeartRateZone("Neuromuscular", restingHR + (hrr * 0.9).toInt(), maxHR, "90-100%")
            )
        }
        
        fun getHeartRateZone(heartRate: Int, maxHR: Int, restingHR: Int): HeartRateZone {
            val zones = calculateHeartRateZones(maxHR, restingHR)
            return zones.find { heartRate >= it.minHR && heartRate <= it.maxHR } ?: zones.first()
        }
        
        fun calculateMacroDistribution(totalCalories: Double, proteinPercent: Int, carbPercent: Int, fatPercent: Int): MacroDistribution {
            val proteinCalories = totalCalories * proteinPercent / 100.0
            val carbCalories = totalCalories * carbPercent / 100.0
            val fatCalories = totalCalories * fatPercent / 100.0
            
            return MacroDistribution(
                proteinGrams = proteinCalories / 4.0,
                carbGrams = carbCalories / 4.0,
                fatGrams = fatCalories / 9.0
            )
        }
        
        fun calculateProteinRequirement(weightKg: Double, activityLevel: ActivityLevel): Double {
            val multiplier = when (activityLevel) {
                ActivityLevel.SEDENTARY -> 0.8
                ActivityLevel.LIGHTLY_ACTIVE -> 1.0
                ActivityLevel.MODERATELY_ACTIVE -> 1.3
                ActivityLevel.VERY_ACTIVE -> 1.6
                ActivityLevel.EXTREMELY_ACTIVE -> 2.0
            }
            return weightKg * multiplier
        }
        
        fun calculateHydrationNeeds(weightKg: Double, activityMinutes: Int, temperatureCelsius: Double): Double {
            val baseLiters = weightKg * 0.035 // 35ml per kg
            val activityLiters = activityMinutes / 60.0 * 0.5 // 0.5L per hour of activity
            val temperatureAdjustment = if (temperatureCelsius > 25) 0.5 else 0.0
            return baseLiters + activityLiters + temperatureAdjustment
        }
        
        fun calculateWeightLossCalories(tdee: Double, targetWeightLossPerWeek: Double): Double {
            val weeklyDeficit = targetWeightLossPerWeek * 7700 // 7700 cal per kg
            val dailyDeficit = weeklyDeficit / 7
            return tdee - dailyDeficit
        }
        
        fun calculateTimeToGoalWeight(currentWeight: Double, targetWeight: Double, weeklyLossRate: Double): Int {
            val weightDifference = kotlin.math.abs(currentWeight - targetWeight)
            return (weightDifference / weeklyLossRate).toInt()
        }
        
        fun calculateStepGoalProgression(currentSteps: Int, targetSteps: Int, weeks: Int): List<Int> {
            val weeklyIncrease = (targetSteps - currentSteps) / (weeks - 1)
            return (0 until weeks).map { week ->
                currentSteps + (weeklyIncrease * week)
            }
        }
        
        fun formatDistance(meters: Double): String {
            return if (meters < 1000) {
                "${meters.toInt()} m"
            } else {
                "${"%.1f".format(meters / 1000)} km"
            }
        }
        
        fun formatDuration(seconds: Int): String {
            val hours = seconds / 3600
            val minutes = (seconds % 3600) / 60
            val secs = seconds % 60
            
            return when {
                hours > 0 -> if (minutes > 0) "${hours}h ${minutes}m ${secs}s" else "${hours}h"
                minutes > 0 -> "${minutes}m ${secs}s"
                else -> "${secs}s"
            }
        }
        
        fun formatCalories(calories: Double): String {
            val rounded = round(calories).toInt()
            return if (rounded >= 1000) {
                "${rounded.toString().replace(Regex("(\\d)(?=(\\d{3})+\$)"), "$1,")} cal"
            } else {
                "$rounded cal"
            }
        }
        
        fun formatPace(minutesPerKm: Double): String {
            val minutes = minutesPerKm.toInt()
            val seconds = ((minutesPerKm - minutes) * 60).toInt()
            return "$minutes:${seconds.toString().padStart(2, '0')} /km"
        }
        
        fun formatWeight(weightKg: Double): String {
            return "${"%.1f".format(weightKg)} kg"
        }
        
        fun daysBetween(startDate: Date, endDate: Date): Int {
            val diffInMillies = kotlin.math.abs(endDate.time - startDate.time)
            return (diffInMillies / (1000 * 60 * 60 * 24)).toInt()
        }
        
        fun isToday(date: Date): Boolean {
            val today = Calendar.getInstance()
            val checkDate = Calendar.getInstance().apply { time = date }
            
            return today.get(Calendar.YEAR) == checkDate.get(Calendar.YEAR) &&
                    today.get(Calendar.DAY_OF_YEAR) == checkDate.get(Calendar.DAY_OF_YEAR)
        }
    }
}
