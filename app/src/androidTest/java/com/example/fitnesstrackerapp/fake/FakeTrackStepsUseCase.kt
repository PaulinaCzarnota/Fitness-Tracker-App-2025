package com.example.fitnesstrackerapp.fake

import com.example.fitnesstrackerapp.data.entity.Step
import com.example.fitnesstrackerapp.usecase.StepActivityLevel
import com.example.fitnesstrackerapp.usecase.StepStatistics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.Date
import java.util.concurrent.atomic.AtomicLong

/**
 * Fake implementation of step tracking use case for Android instrumented tests.
 * Provides minimal functionality with in-memory storage to prevent compilation errors.
 */
class FakeTrackStepsUseCase {

    private val steps = mutableMapOf<String, Step>() // userId-date key to Step
    private val idCounter = AtomicLong(1)

    /**
     * Records steps for a user.
     */
    suspend fun recordSteps(
        userId: Long,
        stepCount: Int,
        date: Date = Date(),
        goal: Int = 10000,
    ): Result<Step> {
        return try {
            val step = Step(
                id = idCounter.getAndIncrement(),
                userId = userId,
                count = stepCount,
                goal = goal,
                date = date,
                caloriesBurned = calculateCaloriesFromSteps(stepCount),
                distanceMeters = calculateDistanceFromSteps(stepCount),
            )
            steps["${userId}_${date.time}"] = step
            Result.success(step)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gets today's steps for a user.
     */
    fun getTodaysSteps(userId: Long): Flow<Step?> {
        val today = Date()
        val step = steps["${userId}_${today.time}"] ?: steps.values.firstOrNull {
            it.userId == userId && isSameDay(it.date, today)
        }
        return flowOf(step)
    }

    /**
     * Calculates step progress percentage.
     */
    fun calculateProgress(currentSteps: Int, goal: Int): Float {
        return if (goal > 0) {
            ((currentSteps.toFloat() / goal) * 100f).coerceAtMost(100f)
        } else {
            0f
        }
    }

    /**
     * Checks if daily goal is achieved.
     */
    fun isGoalAchieved(currentSteps: Int, goal: Int): Boolean {
        return currentSteps >= goal
    }

    /**
     * Gets remaining steps to goal.
     */
    fun getRemainingSteps(currentSteps: Int, goal: Int): Int {
        return (goal - currentSteps).coerceAtLeast(0)
    }

    /**
     * Gets activity level based on step count.
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
     * Formats step count for display.
     */
    fun formatStepCount(steps: Int): String {
        return when {
            steps >= 10000 -> "${steps / 1000}K+ steps"
            steps >= 1000 -> "${String.format("%.1f", steps / 1000.0)}K steps"
            else -> "$steps steps"
        }
    }

    /**
     * Calculates weekly step statistics.
     */
    suspend fun getWeeklyStatistics(userId: Long): Result<StepStatistics> {
        return try {
            val userSteps = steps.values.filter { it.userId == userId }
            val totalSteps = userSteps.sumOf { it.count }
            val averageSteps = if (userSteps.isNotEmpty()) totalSteps / userSteps.size else 0

            val statistics = StepStatistics(
                totalSteps = totalSteps,
                averageSteps = averageSteps,
                daysActive = userSteps.size,
                totalDistance = calculateDistanceFromSteps(totalSteps),
                totalCalories = calculateCaloriesFromSteps(totalSteps),
            )
            Result.success(statistics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sets today's steps for a user (for testing).
     */
    fun setTodaysSteps(userId: Long, stepCount: Int) {
        val today = Date()
        val step = Step(
            id = idCounter.getAndIncrement(),
            userId = userId,
            count = stepCount,
            goal = 10000,
            date = today,
            caloriesBurned = calculateCaloriesFromSteps(stepCount),
            distanceMeters = calculateDistanceFromSteps(stepCount),
        )
        steps["${userId}_${today.time}"] = step
    }

    /**
     * Calculates estimated calories burned from steps.
     */
    private fun calculateCaloriesFromSteps(steps: Int, weightKg: Double = 70.0): Float {
        val caloriesPerStep = 0.04 * (weightKg / 70.0)
        return (steps * caloriesPerStep).toFloat()
    }

    /**
     * Calculates estimated distance from steps.
     */
    private fun calculateDistanceFromSteps(steps: Int, strideLength: Float = 0.762f): Float {
        return steps * strideLength
    }

    /**
     * Checks if two dates are the same day.
     */
    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = java.util.Calendar.getInstance().apply { time = date1 }
        val cal2 = java.util.Calendar.getInstance().apply { time = date2 }

        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
            cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
    }
}
