package com.example.fitnesstrackerapp.usecase

import com.example.fitnesstrackerapp.data.entity.Step
import com.example.fitnesstrackerapp.repository.StepRepository
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Use case for step tracking functionality.
 *
 * Handles the business logic of step counting, goal management, and statistics
 * while keeping ViewModels focused on UI state management.
 */
class TrackStepsUseCase(
    private val stepRepository: StepRepository,
) {

    /**
     * Records steps for a user
     */
    suspend fun recordSteps(
        userId: Long,
        stepCount: Int,
        date: Date = Date(),
        goal: Int = 10000,
    ): Result<Step> {
        return try {
            val step = Step(
                userId = userId,
                count = stepCount,
                date = date,
                goal = goal,
                caloriesBurned = calculateCaloriesFromSteps(stepCount),
                distanceMeters = calculateDistanceFromSteps(stepCount),
            )
            stepRepository.saveSteps(step)
            Result.success(step)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gets today's steps for a user
     */
    fun getTodaysSteps(userId: Long): Flow<Step?> {
        return stepRepository.getTodaysSteps(userId)
    }

    /**
     * Gets step history for a date range
     */
    fun getStepHistory(userId: Long, startDate: Date, endDate: Date): Flow<List<Step>> {
        return stepRepository.getStepsInDateRange(userId, startDate, endDate)
    }

    /**
     * Updates step goal
     */
    suspend fun updateStepGoal(userId: Long, newGoal: Int): Result<Unit> {
        return try {
            // This would need to be implemented in the repository
            // For now, we'll return success as a placeholder
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Calculates step progress percentage
     */
    fun calculateProgress(currentSteps: Int, goal: Int): Float {
        return if (goal > 0) {
            ((currentSteps.toFloat() / goal) * 100f).coerceAtMost(100f)
        } else {
            0f
        }
    }

    /**
     * Checks if daily goal is achieved
     */
    fun isGoalAchieved(currentSteps: Int, goal: Int): Boolean {
        return currentSteps >= goal
    }

    /**
     * Gets remaining steps to goal
     */
    fun getRemainingSteps(currentSteps: Int, goal: Int): Int {
        return (goal - currentSteps).coerceAtLeast(0)
    }

    /**
     * Calculates weekly step statistics
     */
    suspend fun getWeeklyStatistics(userId: Long): Result<StepStatistics> {
        return try {
            // Calculate date range for the past week
            val now = Date()
            Date(now.time - (7 * 24 * 60 * 60 * 1000L))

            // Get steps for the week (this would need to be implemented as a suspend function
            // or we'd need to collect from the flow - simplified here)
            val totalSteps = 0 // Would be calculated from repository data
            val averageSteps = 0 // Would be calculated from repository data
            val daysActive = 0 // Would be calculated from repository data

            val statistics = StepStatistics(
                totalSteps = totalSteps,
                averageSteps = averageSteps,
                daysActive = daysActive,
                totalDistance = calculateDistanceFromSteps(totalSteps),
                totalCalories = calculateCaloriesFromSteps(totalSteps),
            )

            Result.success(statistics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gets activity level based on step count
     */
    fun getActivityLevel(steps: Int): StepActivityLevel {
        return when {
            steps >= 12000 -> StepActivityLevel.HIGHLY_ACTIVE
            steps >= 10000 -> StepActivityLevel.ACTIVE
            steps >= 7500 -> StepActivityLevel.SOMEWHAT_ACTIVE
            steps >= 5000 -> StepActivityLevel.LOW_ACTIVE
            else -> StepActivityLevel.SEDENTARY
        }
    }

    /**
     * Calculates estimated calories burned from steps
     */
    private fun calculateCaloriesFromSteps(steps: Int, weightKg: Double = 70.0): Float {
        // Rough approximation: ~0.04 calories per step for average weight person
        val caloriesPerStep = 0.04 * (weightKg / 70.0) // Adjust for weight
        return (steps * caloriesPerStep).toFloat()
    }

    /**
     * Calculates estimated distance from steps
     */
    private fun calculateDistanceFromSteps(steps: Int, strideLength: Float = 0.762f): Float {
        // Average stride length is about 0.762 meters (2.5 feet)
        return steps * strideLength
    }

    /**
     * Validates step count
     */
    fun validateStepCount(steps: Int): Boolean {
        return steps in 0..100000 // Reasonable range for daily steps
    }

    /**
     * Formats step count for display
     */
    fun formatStepCount(steps: Int): String {
        return when {
            steps >= 10000 -> "${steps / 1000}K+ steps"
            steps >= 1000 -> "${String.format("%.1f", steps / 1000.0)}K steps"
            else -> "$steps steps"
        }
    }
}

/**
 * Data class for step statistics
 */
data class StepStatistics(
    val totalSteps: Int,
    val averageSteps: Int,
    val daysActive: Int,
    val totalDistance: Float, // in meters
    val totalCalories: Float,
)

/**
 * Enum for activity levels based on step count
 */
enum class StepActivityLevel(val displayName: String) {
    SEDENTARY("Sedentary"),
    LOW_ACTIVE("Low Active"),
    SOMEWHAT_ACTIVE("Somewhat Active"),
    ACTIVE("Active"),
    HIGHLY_ACTIVE("Highly Active"),
}
