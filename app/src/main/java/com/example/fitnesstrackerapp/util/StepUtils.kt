package com.example.fitnesstrackerapp.util

/**
 * Step Tracking Utility Functions
 *
 * Provides common helper functions for step-related calculations
 * shared across sensor services and other components.
 */

object StepUtils {
    // Default constants for step calculations
    private const val DEFAULT_STEP_LENGTH_METERS = 0.76f
    private const val DEFAULT_CALORIES_PER_STEP = 0.04f

    /**
     * Calculates distance traveled based on step count
     * @param steps Number of steps taken
     * @param stepLengthMeters Average step length in meters (optional, uses default if not provided)
     * @return Distance in meters
     */
    fun calculateDistance(steps: Int, stepLengthMeters: Float = DEFAULT_STEP_LENGTH_METERS): Float {
        return steps * stepLengthMeters
    }

    /**
     * Calculates calories burned from walking/running steps
     * @param steps Number of steps taken
     * @param caloriesPerStep Calories burned per step (optional, uses default if not provided)
     * @return Calories burned
     */
    fun calculateCalories(steps: Int, caloriesPerStep: Float = DEFAULT_CALORIES_PER_STEP): Float {
        return steps * caloriesPerStep
    }

    /**
     * Calculates progress percentage toward a goal
     * @param currentSteps Current step count
     * @param goalSteps Target step goal
     * @return Progress percentage (0-100), capped at 100
     */
    fun calculateProgress(currentSteps: Int, goalSteps: Int): Float {
        return if (goalSteps > 0) {
            (currentSteps.toFloat() / goalSteps * 100).coerceIn(0f, 100f)
        } else {
            0f
        }
    }

    /**
     * Estimates active minutes based on step count
     * Uses rough estimate of 100 steps = 1 minute of activity
     * @param steps Number of steps taken
     * @return Estimated active minutes, capped at 1440 (24 hours)
     */
    fun estimateActiveMinutes(steps: Int): Int {
        return (steps / 100).coerceAtMost(1440)
    }

    /**
     * Formats step count with appropriate units
     * @param steps Number of steps
     * @return Formatted string (e.g., "1,234 steps")
     */
    fun formatStepCount(steps: Int): String {
        return when {
            steps >= 1000 -> String.format("%,d steps", steps)
            else -> "$steps steps"
        }
    }

    /**
     * Formats distance with appropriate units and precision
     * @param distanceMeters Distance in meters
     * @param useKilometers Whether to convert to kilometers for large distances
     * @return Formatted distance string
     */
    fun formatDistance(distanceMeters: Float, useKilometers: Boolean = true): String {
        return if (useKilometers && distanceMeters >= 1000) {
            val kilometers = distanceMeters / 1000f
            String.format("%.2f km", kilometers)
        } else {
            String.format("%.0f m", distanceMeters)
        }
    }

    /**
     * Formats calories with appropriate precision
     * @param calories Number of calories
     * @return Formatted calories string
     */
    fun formatCalories(calories: Float): String {
        return String.format("%.0f cal", calories)
    }
}
