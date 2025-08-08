/**
 * Data model for workout summary statistics used in UI components.
 *
 * This data class provides aggregated workout statistics for display
 * in progress cards, charts, and summary screens throughout the app.
 */

package com.example.fitnesstrackerapp.data.model

/**
 * Represents aggregated workout statistics for display in UI components.
 *
 * @property totalWorkouts Total number of workouts in the summary period
 * @property totalCalories Total calories burned across all workouts
 * @property averageDuration Average duration of workouts in minutes
 * @property totalDistance Total distance covered across all workouts in kilometers
 * @property totalDuration Total time spent working out in minutes
 * @property averageCalories Average calories burned per workout
 */
data class WorkoutSummary(
    val totalWorkouts: Int = 0,
    val totalCalories: Double = 0.0,
    val averageDuration: Double = 0.0,
    val totalDistance: Double = 0.0,
    val totalDuration: Long = 0L,
    val averageCalories: Double = 0.0,
) {
    /**
     * Calculates the average calories burned per minute across all workouts.
     *
     * @return Average calories per minute, or 0.0 if no duration recorded
     */
    fun getCaloriesPerMinute(): Double {
        return if (totalDuration > 0) totalCalories / totalDuration else 0.0
    }

    /**
     * Calculates the average distance per workout.
     *
     * @return Average distance in kilometers, or 0.0 if no workouts recorded
     */
    fun getAverageDistance(): Double {
        return if (totalWorkouts > 0) totalDistance / totalWorkouts else 0.0
    }

    /**
     * Gets the total duration formatted as hours and minutes.
     *
     * @return Formatted duration string (e.g., "2h 30m")
     */
    fun getFormattedTotalDuration(): String {
        val hours = totalDuration / 60
        val minutes = totalDuration % 60
        return when {
            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
            hours > 0 -> "${hours}h"
            minutes > 0 -> "${minutes}m"
            else -> "0m"
        }
    }

    /**
     * Checks if this summary contains any workout data.
     *
     * @return true if there is workout data, false otherwise
     */
    fun hasData(): Boolean {
        return totalWorkouts > 0
    }

    companion object {
        /**
         * Creates an empty WorkoutSummary instance.
         *
         * @return WorkoutSummary with all values set to zero
         */
        fun empty(): WorkoutSummary = WorkoutSummary()

        /**
         * Creates a WorkoutSummary from a list of workouts.
         *
         * @param workouts List of workout entities to summarize
         * @return WorkoutSummary containing aggregated statistics
         */
        fun fromWorkouts(workouts: List<com.example.fitnesstrackerapp.data.entity.Workout>): WorkoutSummary {
            if (workouts.isEmpty()) return empty()

            val totalWorkouts = workouts.size
            val totalCalories = workouts.sumOf { it.caloriesBurned.toDouble() }
            val totalDistance = workouts.sumOf { it.distance.toDouble() }
            val totalDuration = workouts.sumOf { it.duration.toLong() }

            return WorkoutSummary(
                totalWorkouts = totalWorkouts,
                totalCalories = totalCalories,
                averageDuration = if (totalWorkouts > 0) totalDuration.toDouble() / totalWorkouts else 0.0,
                totalDistance = totalDistance,
                totalDuration = totalDuration,
                averageCalories = if (totalWorkouts > 0) totalCalories / totalWorkouts else 0.0,
            )
        }
    }
}
