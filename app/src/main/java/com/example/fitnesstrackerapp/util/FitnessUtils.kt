/**
 * Fitness Calculations Utility
 *
 * Responsibilities:
 * - Performs fitness-related calculations
 * - Handles unit conversions
 * - Provides data formatting utilities
 */

package com.example.fitnesstrackerapp.util

import com.example.fitnesstrackerapp.settings.MeasurementUnit
import kotlin.math.pow
import kotlin.math.round

object FitnessUtils {
    // Distance Conversions
    fun convertDistance(value: Double, from: MeasurementUnit, to: MeasurementUnit): Double {
        return when (from) {
            to -> value
            MeasurementUnit.METRIC -> value * 0.621371 // km to miles
            else -> value * 1.60934 // miles to km
        }.roundToDecimals(2)
    }

    // Weight Conversions
    fun convertWeight(value: Double, from: MeasurementUnit, to: MeasurementUnit): Double {
        return when (from) {
            to -> value
            MeasurementUnit.METRIC -> value * 2.20462 // kg to lbs
            else -> value * 0.453592 // lbs to kg
        }.roundToDecimals(1)
    }

    // Calorie Calculations
    fun calculateCaloriesBurned(
        durationMinutes: Int,
        weightKg: Double,
        metValue: Double,
    ): Int {
        return ((metValue * 3.5 * weightKg * durationMinutes) / 200.0).toInt()
    }

    // Heart Rate Zone Calculations
    fun calculateHeartRateZones(maxHeartRate: Int): List<IntRange> {
        return listOf(
            (maxHeartRate * 0.5).toInt()..(maxHeartRate * 0.6).toInt(), // Zone 1
            (maxHeartRate * 0.6).toInt()..(maxHeartRate * 0.7).toInt(), // Zone 2
            (maxHeartRate * 0.7).toInt()..(maxHeartRate * 0.8).toInt(), // Zone 3
            (maxHeartRate * 0.8).toInt()..(maxHeartRate * 0.9).toInt(), // Zone 4
            (maxHeartRate * 0.9).toInt()..maxHeartRate, // Zone 5
        )
    }

    // BMI Calculation
    fun calculateBMI(weightKg: Double, heightMeters: Double): Double {
        return (weightKg / (heightMeters * heightMeters)).roundToDecimals(1)
    }

    // Pace Calculations
    fun calculatePace(distanceKm: Double, durationMinutes: Int): String {
        val paceMinutes = durationMinutes / distanceKm
        val paceSeconds = ((durationMinutes / distanceKm) % 1 * 60).toInt()
        return "${paceMinutes.toInt()}:${paceSeconds.toString().padStart(2, '0')}"
    }

    // Formatting Utilities
    fun formatDistance(distance: Double, unit: MeasurementUnit): String {
        return "${distance.roundToDecimals(2)} ${if (unit == MeasurementUnit.METRIC) "km" else "mi"}"
    }

    fun formatWeight(weight: Double, unit: MeasurementUnit): String {
        return "${weight.roundToDecimals(1)} ${if (unit == MeasurementUnit.METRIC) "kg" else "lbs"}"
    }

    fun formatDuration(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return if (hours > 0) {
            "${hours}h ${mins}m"
        } else {
            "${mins}m"
        }
    }

    // Helper Functions
    private fun Double.roundToDecimals(decimals: Int): Double {
        val factor = 10.0.pow(decimals.toDouble())
        return round(this * factor) / factor
    }

    // MET Values for Different Activities
    object MetValues {
        const val WALKING_SLOW = 2.5
        const val WALKING_BRISK = 3.5
        const val JOGGING = 7.0
        const val RUNNING = 9.0
        const val CYCLING_LIGHT = 4.0
        const val CYCLING_MODERATE = 6.0
        const val CYCLING_VIGOROUS = 8.0
        const val SWIMMING_LIGHT = 5.0
        const val SWIMMING_MODERATE = 7.0
        const val SWIMMING_VIGOROUS = 9.0
        const val WEIGHT_TRAINING = 3.5
        const val YOGA = 2.5
        const val PILATES = 3.0
    }
}
