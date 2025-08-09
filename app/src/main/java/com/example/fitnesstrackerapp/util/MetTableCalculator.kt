/**
 * MET Table Calculator for comprehensive calorie calculations.
 *
 * This utility provides accurate calorie calculations based on MET (Metabolic Equivalent of Task)
 * values for various workout types and intensities. MET values represent the energy cost of
 * physical activities as multiples of resting metabolic rate.
 *
 * Key Features:
 * - Comprehensive MET values for different workout types
 * - Intensity-based calculations for more accurate results
 * - Support for running, cycling, weightlifting, and other activities
 * - User weight and duration-based calorie calculations
 */

package com.example.fitnesstrackerapp.util

import com.example.fitnesstrackerapp.data.entity.WorkoutType
import kotlin.math.max

object MetTableCalculator {

    /**
     * Calculates calories burned based on MET value, user weight, and duration.
     *
     * Formula: Calories = MET × Weight(kg) × Duration(hours) × 3.5 / 200
     * This is the standard formula used by fitness professionals.
     *
     * @param metValue MET value for the specific activity and intensity
     * @param weightKg User's weight in kilograms
     * @param durationMinutes Duration of activity in minutes
     * @return Estimated calories burned (rounded to nearest integer)
     */
    fun calculateCaloriesBurned(
        metValue: Double,
        weightKg: Double,
        durationMinutes: Int,
    ): Int {
        val durationHours = durationMinutes / 60.0
        val calories = metValue * weightKg * durationHours * 3.5 / 200.0
        return max(0, calories.toInt())
    }

    /**
     * Gets MET value for a specific workout type based on intensity level.
     *
     * @param workoutType Type of workout being performed
     * @param intensity Intensity level (LOW, MODERATE, HIGH, VERY_HIGH)
     * @param specificDetails Additional details for more accurate MET calculation
     * @return MET value for the activity
     */
    fun getMetValue(
        workoutType: WorkoutType,
        intensity: WorkoutIntensity = WorkoutIntensity.MODERATE,
        specificDetails: WorkoutDetails = WorkoutDetails(),
    ): Double {
        return when (workoutType) {
            WorkoutType.RUNNING -> getRunningMet(intensity, specificDetails)
            WorkoutType.CYCLING -> getCyclingMet(intensity, specificDetails)
            WorkoutType.WEIGHTLIFTING -> getWeightliftingMet(intensity)
            WorkoutType.WALKING -> getWalkingMet(intensity, specificDetails)
            WorkoutType.SWIMMING -> getSwimmingMet(intensity)
            WorkoutType.YOGA -> getYogaMet(intensity)
            WorkoutType.PILATES -> getPilatesMet(intensity)
            WorkoutType.HIIT -> getHiitMet(intensity)
            WorkoutType.CROSSFIT -> getCrossfitMet(intensity)
            WorkoutType.BOXING -> getBoxingMet(intensity)
            WorkoutType.BASKETBALL -> getSportsMet(workoutType, intensity)
            WorkoutType.SOCCER -> getSportsMet(workoutType, intensity)
            WorkoutType.TENNIS -> getSportsMet(workoutType, intensity)
            WorkoutType.GOLF -> getSportsMet(workoutType, intensity)
            WorkoutType.DANCE -> getDanceMet(intensity)
            WorkoutType.ROWING -> getRowingMet(intensity)
            WorkoutType.CLIMBING -> getClimbingMet(intensity)
            WorkoutType.SKIING -> getSkiingMet(intensity)
            WorkoutType.SNOWBOARDING -> getSnowboardingMet(intensity)
            WorkoutType.VOLLEYBALL -> getSportsMet(workoutType, intensity)
            WorkoutType.BADMINTON -> getSportsMet(workoutType, intensity)
            WorkoutType.TABLE_TENNIS -> getSportsMet(workoutType, intensity)
            WorkoutType.GYMNASTICS -> getGymnasticsMet(intensity)
            WorkoutType.CARDIO -> getCardioMet(intensity)
            WorkoutType.OTHER -> getDefaultMet(intensity)
        }
    }

    /**
     * Calculates calories for a complete workout with automatic MET selection.
     *
     * @param workoutType Type of workout
     * @param durationMinutes Duration in minutes
     * @param weightKg User weight in kg
     * @param intensity Workout intensity
     * @param distance Distance covered (for running/cycling)
     * @param avgHeartRate Average heart rate (for intensity adjustment)
     * @return Estimated calories burned
     */
    fun calculateWorkoutCalories(
        workoutType: WorkoutType,
        durationMinutes: Int,
        weightKg: Double,
        intensity: WorkoutIntensity = WorkoutIntensity.MODERATE,
        distance: Float? = null,
        avgHeartRate: Int? = null,
    ): Int {
        val details = WorkoutDetails(
            distance = distance,
            avgHeartRate = avgHeartRate,
        )

        val adjustedIntensity = avgHeartRate?.let { hr ->
            adjustIntensityByHeartRate(hr, intensity)
        } ?: intensity

        val metValue = getMetValue(workoutType, adjustedIntensity, details)
        return calculateCaloriesBurned(metValue, weightKg, durationMinutes)
    }

    private fun getRunningMet(intensity: WorkoutIntensity, details: WorkoutDetails): Double {
        return when (intensity) {
            WorkoutIntensity.LOW -> 6.0 // Light jogging
            WorkoutIntensity.MODERATE -> 9.0 // Running 5 mph
            WorkoutIntensity.HIGH -> 11.0 // Running 6 mph
            WorkoutIntensity.VERY_HIGH -> 15.0 // Running 8+ mph
        }
    }

    private fun getCyclingMet(intensity: WorkoutIntensity, details: WorkoutDetails): Double {
        return when (intensity) {
            WorkoutIntensity.LOW -> 4.0 // Casual cycling
            WorkoutIntensity.MODERATE -> 6.8 // Cycling 12-14 mph
            WorkoutIntensity.HIGH -> 10.0 // Cycling 16-19 mph
            WorkoutIntensity.VERY_HIGH -> 16.0 // Racing
        }
    }

    private fun getWeightliftingMet(intensity: WorkoutIntensity): Double {
        return when (intensity) {
            WorkoutIntensity.LOW -> 3.0 // Light weights
            WorkoutIntensity.MODERATE -> 5.0 // Moderate intensity
            WorkoutIntensity.HIGH -> 6.0 // High intensity
            WorkoutIntensity.VERY_HIGH -> 8.0 // Power lifting
        }
    }

    private fun getWalkingMet(intensity: WorkoutIntensity, details: WorkoutDetails): Double {
        return when (intensity) {
            WorkoutIntensity.LOW -> 2.5 // Slow walk
            WorkoutIntensity.MODERATE -> 3.5 // Brisk walk
            WorkoutIntensity.HIGH -> 4.5 // Fast walk
            WorkoutIntensity.VERY_HIGH -> 6.0 // Race walking
        }
    }

    private fun getSwimmingMet(intensity: WorkoutIntensity): Double {
        return when (intensity) {
            WorkoutIntensity.LOW -> 4.0 // Swimming laps, light
            WorkoutIntensity.MODERATE -> 7.0 // Swimming laps, moderate
            WorkoutIntensity.HIGH -> 10.0 // Swimming laps, vigorous
            WorkoutIntensity.VERY_HIGH -> 14.0 // Competitive swimming
        }
    }

    private fun getYogaMet(intensity: WorkoutIntensity): Double {
        return when (intensity) {
            WorkoutIntensity.LOW -> 2.0 // Gentle yoga
            WorkoutIntensity.MODERATE -> 3.0 // Hatha yoga
            WorkoutIntensity.HIGH -> 4.0 // Power yoga
            WorkoutIntensity.VERY_HIGH -> 5.0 // Hot yoga
        }
    }

    private fun getPilatesMet(intensity: WorkoutIntensity): Double {
        return when (intensity) {
            WorkoutIntensity.LOW -> 3.0
            WorkoutIntensity.MODERATE -> 4.0
            WorkoutIntensity.HIGH -> 5.0
            WorkoutIntensity.VERY_HIGH -> 6.0
        }
    }

    private fun getHiitMet(intensity: WorkoutIntensity): Double {
        return when (intensity) {
            WorkoutIntensity.LOW -> 8.0 // Low intensity intervals
            WorkoutIntensity.MODERATE -> 10.0 // Moderate HIIT
            WorkoutIntensity.HIGH -> 12.0 // High intensity
            WorkoutIntensity.VERY_HIGH -> 15.0 // Maximum effort
        }
    }

    private fun getCrossfitMet(intensity: WorkoutIntensity): Double {
        return when (intensity) {
            WorkoutIntensity.LOW -> 6.0
            WorkoutIntensity.MODERATE -> 8.0
            WorkoutIntensity.HIGH -> 10.0
            WorkoutIntensity.VERY_HIGH -> 12.0
        }
    }

    private fun getBoxingMet(intensity: WorkoutIntensity): Double {
        return when (intensity) {
            WorkoutIntensity.LOW -> 6.0 // Boxing training
            WorkoutIntensity.MODERATE -> 9.0 // Boxing sparring
            WorkoutIntensity.HIGH -> 12.0 // Competitive boxing
            WorkoutIntensity.VERY_HIGH -> 15.0 // Professional level
        }
    }

    private fun getSportsMet(workoutType: WorkoutType, intensity: WorkoutIntensity): Double {
        val baseMet = when (workoutType) {
            WorkoutType.BASKETBALL -> 8.0
            WorkoutType.SOCCER -> 7.0
            WorkoutType.TENNIS -> 6.0
            WorkoutType.GOLF -> 3.5
            WorkoutType.VOLLEYBALL -> 4.0
            WorkoutType.BADMINTON -> 5.5
            WorkoutType.TABLE_TENNIS -> 4.0
            else -> 5.0
        }

        return when (intensity) {
            WorkoutIntensity.LOW -> baseMet * 0.7
            WorkoutIntensity.MODERATE -> baseMet
            WorkoutIntensity.HIGH -> baseMet * 1.3
            WorkoutIntensity.VERY_HIGH -> baseMet * 1.6
        }
    }

    private fun getDanceMet(intensity: WorkoutIntensity): Double {
        return when (intensity) {
            WorkoutIntensity.LOW -> 3.0 // Slow dancing
            WorkoutIntensity.MODERATE -> 5.0 // General dancing
            WorkoutIntensity.HIGH -> 7.0 // Aerobic dance
            WorkoutIntensity.VERY_HIGH -> 9.0 // Fast dancing
        }
    }

    private fun getRowingMet(intensity: WorkoutIntensity): Double {
        return when (intensity) {
            WorkoutIntensity.LOW -> 4.0 // Light effort
            WorkoutIntensity.MODERATE -> 7.0 // Moderate effort
            WorkoutIntensity.HIGH -> 10.0 // Vigorous effort
            WorkoutIntensity.VERY_HIGH -> 14.0 // Very vigorous
        }
    }

    private fun getClimbingMet(intensity: WorkoutIntensity): Double {
        return when (intensity) {
            WorkoutIntensity.LOW -> 5.0 // Light climbing
            WorkoutIntensity.MODERATE -> 8.0 // Moderate climbing
            WorkoutIntensity.HIGH -> 11.0 // Vigorous climbing
            WorkoutIntensity.VERY_HIGH -> 14.0 // Mountain climbing
        }
    }

    private fun getSkiingMet(intensity: WorkoutIntensity): Double {
        return when (intensity) {
            WorkoutIntensity.LOW -> 5.0 // Light skiing
            WorkoutIntensity.MODERATE -> 7.0 // Moderate skiing
            WorkoutIntensity.HIGH -> 9.0 // Vigorous skiing
            WorkoutIntensity.VERY_HIGH -> 14.0 // Racing
        }
    }

    private fun getSnowboardingMet(intensity: WorkoutIntensity): Double {
        return when (intensity) {
            WorkoutIntensity.LOW -> 4.0 // Light snowboarding
            WorkoutIntensity.MODERATE -> 6.0 // Moderate snowboarding
            WorkoutIntensity.HIGH -> 8.0 // Vigorous snowboarding
            WorkoutIntensity.VERY_HIGH -> 12.0 // Racing/freestyle
        }
    }

    private fun getGymnasticsMet(intensity: WorkoutIntensity): Double {
        return when (intensity) {
            WorkoutIntensity.LOW -> 3.0 // General gymnastics
            WorkoutIntensity.MODERATE -> 4.0 // Moderate gymnastics
            WorkoutIntensity.HIGH -> 6.0 // Vigorous gymnastics
            WorkoutIntensity.VERY_HIGH -> 8.0 // Competitive gymnastics
        }
    }

    private fun getCardioMet(intensity: WorkoutIntensity): Double {
        return when (intensity) {
            WorkoutIntensity.LOW -> 4.0 // Light cardio
            WorkoutIntensity.MODERATE -> 6.0 // Moderate cardio
            WorkoutIntensity.HIGH -> 8.0 // High cardio
            WorkoutIntensity.VERY_HIGH -> 10.0 // Maximum cardio
        }
    }

    private fun getDefaultMet(intensity: WorkoutIntensity): Double {
        return when (intensity) {
            WorkoutIntensity.LOW -> 3.0
            WorkoutIntensity.MODERATE -> 5.0
            WorkoutIntensity.HIGH -> 7.0
            WorkoutIntensity.VERY_HIGH -> 9.0
        }
    }

    /**
     * Adjusts intensity based on heart rate zones.
     */
    private fun adjustIntensityByHeartRate(heartRate: Int, currentIntensity: WorkoutIntensity): WorkoutIntensity {
        return when {
            heartRate < 100 -> WorkoutIntensity.LOW
            heartRate < 130 -> WorkoutIntensity.MODERATE
            heartRate < 160 -> WorkoutIntensity.HIGH
            else -> WorkoutIntensity.VERY_HIGH
        }
    }
}

/**
 * Represents different workout intensity levels.
 */
enum class WorkoutIntensity {
    LOW, // 50-60% max HR, light effort
    MODERATE, // 60-70% max HR, moderate effort
    HIGH, // 70-85% max HR, vigorous effort
    VERY_HIGH, // 85%+ max HR, maximum effort
}

/**
 * Additional workout details for more accurate MET calculations.
 */
data class WorkoutDetails(
    val distance: Float? = null, // Distance in kilometers
    val avgHeartRate: Int? = null, // Average heart rate
    val maxHeartRate: Int? = null, // Maximum heart rate
    val elevation: Float? = null, // Elevation gain in meters
    val temperature: Float? = null, // Temperature in Celsius
)
