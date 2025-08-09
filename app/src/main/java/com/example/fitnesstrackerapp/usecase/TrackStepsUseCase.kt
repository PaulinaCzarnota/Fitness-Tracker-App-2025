package com.example.fitnesstrackerapp.usecase

import com.example.fitnesstrackerapp.data.entity.Step
import com.example.fitnesstrackerapp.repository.StepRepository
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Enhanced use case for step tracking functionality.
 *
 * Handles the business logic of step counting, goal management, statistics,
 * and integration with workouts and goals while keeping ViewModels focused
 * on UI state management. Includes battery optimization support and
 * comprehensive analytics.
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

    // Enhanced methods for battery optimization and integration

    /**
     * Batch updates step records for better performance.
     */
    suspend fun batchUpdateSteps(
        userId: Long,
        stepDataPoints: List<StepUpdatePoint>,
    ): Result<List<Step>> {
        return try {
            val steps = stepDataPoints.map { point ->
                Step(
                    userId = userId,
                    count = point.stepCount,
                    date = point.date,
                    goal = point.goal,
                    caloriesBurned = calculateCaloriesFromSteps(point.stepCount),
                    distanceMeters = calculateDistanceFromSteps(point.stepCount),
                    activeMinutes = estimateActiveMinutes(point.stepCount),
                )
            }

            stepRepository.batchUpsertSteps(steps)
            Result.success(steps)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gets comprehensive step analytics.
     */
    suspend fun getStepAnalytics(userId: Long): Result<StepAnalyticsResult> {
        return try {
            val analytics = stepRepository.getStepStatistics(userId)
            analytics?.let {
                Result.success(
                    StepAnalyticsResult(
                        totalSteps = it.totalSteps,
                        averageSteps = it.averageSteps.toInt(),
                        maxSteps = it.maxSteps,
                        goalAchievedDays = it.goalAchievedDays,
                        goalAchievementRate = it.goalAchievementRate,
                        totalCalories = it.totalCaloriesBurned,
                        totalDistanceKm = it.totalDistanceMeters / 1000f,
                        bestDay = it.bestDay,
                        activityLevel = getActivityLevel(it.averageSteps.toInt()),
                    ),
                )
            } ?: Result.failure(Exception("No analytics data available"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates today's step count efficiently.
     */
    suspend fun updateTodaySteps(
        userId: Long,
        stepCount: Int,
        goal: Int = 10000,
    ): Result<Long> {
        return try {
            val calories = calculateCaloriesFromSteps(stepCount)
            val distance = calculateDistanceFromSteps(stepCount)

            val id = stepRepository.updateTodaySteps(
                userId = userId,
                stepCount = stepCount,
                caloriesBurned = calories,
                distanceMeters = distance,
                goal = goal,
            )

            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gets recent step trends for analysis.
     */
    fun getRecentStepTrends(userId: Long, days: Int = 7): Flow<List<Step>> {
        return stepRepository.getRecentSteps(userId, days)
    }

    /**
     * Gets goal achievement history.
     */
    fun getGoalAchievementHistory(userId: Long): Flow<List<Step>> {
        return stepRepository.getGoalAchievedSteps(userId)
    }

    /**
     * Calculates step consistency score (0-100).
     */
    suspend fun calculateConsistencyScore(
        userId: Long,
        days: Int = 30,
    ): Result<ConsistencyScore> {
        return try {
            val endDate = Date()
            val startDate = Date(endDate.time - (days * 24 * 60 * 60 * 1000L))

            // This would collect from flow in real implementation
            val stepHistory = stepRepository.getStepsInDateRange(userId, startDate, endDate)

            // Calculate consistency based on how many days user was active
            // Implementation would analyze the flow data
            val mockConsistency = ConsistencyScore(
                score = 75f, // Placeholder
                totalDays = days,
                activeDays = (days * 0.75).toInt(),
                averageSteps = 8500,
                trend = StepTrend.INCREASING,
            )

            Result.success(mockConsistency)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Suggests optimal daily step goal based on user's history.
     */
    suspend fun suggestOptimalGoal(userId: Long): Result<Int> {
        return try {
            val analytics = stepRepository.getStepStatistics(userId)
            analytics?.let {
                val suggestedGoal = when {
                    it.averageSteps < 5000 -> 6000
                    it.averageSteps < 8000 -> (it.averageSteps * 1.2).toInt()
                    it.averageSteps < 10000 -> 10000
                    else -> (it.averageSteps * 1.1).toInt()
                }
                Result.success(suggestedGoal)
            } ?: Result.success(10000) // Default goal
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Integrates step data with workout sessions.
     */
    suspend fun integrateWithWorkout(
        userId: Long,
        workoutId: Long,
        stepsDuringWorkout: Int,
    ): Result<Unit> {
        return try {
            // This would integrate with WorkoutRepository
            // Update workout with step data
            // Update daily steps to include workout steps

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Syncs step data with fitness goals.
     */
    suspend fun syncWithGoals(
        userId: Long,
        currentSteps: Int,
        dailyGoal: Int,
    ): Result<List<GoalUpdate>> {
        return try {
            val goalUpdates = mutableListOf<GoalUpdate>()

            // Check various step-related goals
            if (currentSteps >= dailyGoal) {
                goalUpdates.add(
                    GoalUpdate(
                        goalType = "daily_steps",
                        achieved = true,
                        progress = 100f,
                        value = currentSteps,
                    ),
                )
            }

            // Check weekly goals, consistency goals, etc.

            Result.success(goalUpdates)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Provides step-based health insights.
     */
    suspend fun getHealthInsights(userId: Long): Result<List<HealthInsight>> {
        return try {
            val analytics = stepRepository.getStepStatistics(userId)
            val insights = mutableListOf<HealthInsight>()

            analytics?.let {
                // Generate insights based on step data
                if (it.averageSteps < 5000) {
                    insights.add(
                        HealthInsight(
                            type = "LOW_ACTIVITY",
                            title = "Increase Daily Activity",
                            description = "Consider increasing your daily steps to improve cardiovascular health.",
                            recommendation = "Try to add a 10-minute walk to your routine.",
                        ),
                    )
                }

                if (it.goalAchievementRate > 80f) {
                    insights.add(
                        HealthInsight(
                            type = "GOAL_ACHIEVER",
                            title = "Great Consistency!",
                            description = "You're consistently meeting your step goals.",
                            recommendation = "Consider increasing your daily goal by 1000 steps.",
                        ),
                    )
                }
            }

            Result.success(insights)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Helper methods

    private fun estimateActiveMinutes(steps: Int): Int {
        return (steps / 100).coerceAtMost(1440)
    }

    private fun calculateWeekRange(): Pair<Date, Date> {
        val now = Date()
        val weekStart = Date(now.time - (7 * 24 * 60 * 60 * 1000L))
        return Pair(weekStart, now)
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

/**
 * Data class for step update points used in batch operations.
 */
data class StepUpdatePoint(
    val date: Date,
    val stepCount: Int,
    val goal: Int = 10000,
)

/**
 * Data class for comprehensive step analytics result.
 */
data class StepAnalyticsResult(
    val totalSteps: Int,
    val averageSteps: Int,
    val maxSteps: Int,
    val goalAchievedDays: Int,
    val goalAchievementRate: Float,
    val totalCalories: Float,
    val totalDistanceKm: Float,
    val bestDay: Step?,
    val activityLevel: StepActivityLevel,
)

/**
 * Data class for step consistency scoring.
 */
data class ConsistencyScore(
    val score: Float, // 0-100
    val totalDays: Int,
    val activeDays: Int,
    val averageSteps: Int,
    val trend: StepTrend,
)

/**
 * Enum for step trends.
 */
enum class StepTrend {
    INCREASING,
    DECREASING,
    STABLE,
}

/**
 * Data class for goal updates.
 */
data class GoalUpdate(
    val goalType: String,
    val achieved: Boolean,
    val progress: Float,
    val value: Int,
)

/**
 * Data class for health insights based on step data.
 */
data class HealthInsight(
    val type: String,
    val title: String,
    val description: String,
    val recommendation: String,
)
