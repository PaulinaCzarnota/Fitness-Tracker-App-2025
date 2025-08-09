package com.example.fitnesstrackerapp.usecase

import com.example.fitnesstrackerapp.data.entity.Goal
import com.example.fitnesstrackerapp.data.entity.GoalType
import com.example.fitnesstrackerapp.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Use case for managing fitness goals.
 *
 * Handles the business logic of creating, updating, tracking, and achieving goals
 * while keeping ViewModels focused on UI state management.
 */
class ManageGoalsUseCase(
    private val goalRepository: GoalRepository,
) {
    /**
     * Creates a new fitness goal
     */
    suspend fun createGoal(
        userId: Long,
        title: String,
        description: String?,
        goalType: GoalType,
        targetValue: Double,
        unit: String,
        targetDate: Date,
        reminderEnabled: Boolean = true,
    ): Result<Goal> {
        return try {
            val goal = Goal(
                userId = userId,
                title = title,
                description = description,
                goalType = goalType,
                targetValue = targetValue,
                unit = unit,
                targetDate = targetDate,
                reminderEnabled = reminderEnabled,
                createdAt = Date(),
                updatedAt = Date(),
            )
            goalRepository.insert(goal)
            Result.success(goal)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates an existing goal
     */
    suspend fun updateGoal(goal: Goal): Result<Unit> {
        return try {
            val updatedGoal = goal.copy(updatedAt = Date())
            goalRepository.update(updatedGoal)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Updates goal progress
     */
    suspend fun updateGoalProgress(
        goalId: Long,
        currentValue: Double,
        autoComplete: Boolean = true,
    ): Result<GoalProgressUpdate> {
        return try {
            goalRepository.updateProgress(goalId, currentValue, autoComplete)

            val goal = goalRepository.getById(goalId)
            val isAchieved = goal != null && currentValue >= goal.targetValue

            if (isAchieved && autoComplete && goal != null) {
                goalRepository.markGoalAsCompleted(goalId, true)
                return Result.success(GoalProgressUpdate(goal, true))
            }

            Result.success(GoalProgressUpdate(goal, isAchieved))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Marks a goal as achieved
     */
    suspend fun achieveGoal(goalId: Long): Result<Goal> {
        return try {
            goalRepository.markGoalAsAchieved(goalId, Date().time)
            val goal = goalRepository.getById(goalId)
            if (goal != null) {
                Result.success(goal)
            } else {
                Result.failure(IllegalArgumentException("Goal not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletes a goal
     */
    suspend fun deleteGoal(goalId: Long): Result<Unit> {
        return try {
            val goal = goalRepository.getById(goalId)
            if (goal != null) {
                goalRepository.delete(goal)
                Result.success(Unit)
            } else {
                Result.failure(IllegalArgumentException("Goal not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gets all goals for a user
     */
    fun getGoalsForUser(userId: String): Flow<List<Goal>> {
        return goalRepository.getAllByUser(userId)
    }

    /**
     * Gets active goals for a user
     */
    fun getActiveGoalsForUser(userId: String): Flow<List<Goal>> {
        // This would need to be implemented in the repository
        return goalRepository.getAllByUser(userId)
    }

    /**
     * Gets achieved goals for a user
     */
    fun getAchievedGoalsForUser(userId: String): Flow<List<Goal>> {
        // This would need to be implemented in the repository
        return goalRepository.getAllByUser(userId)
    }

    /**
     * Toggles goal reminder
     */
    suspend fun toggleGoalReminder(goalId: Long, enabled: Boolean): Result<Unit> {
        return try {
            val goal = goalRepository.getById(goalId)
            if (goal != null) {
                val updatedGoal = goal.copy(
                    reminderEnabled = enabled,
                    updatedAt = Date(),
                )
                goalRepository.update(updatedGoal)
                Result.success(Unit)
            } else {
                Result.failure(IllegalArgumentException("Goal not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Calculates goal progress percentage
     */
    fun calculateGoalProgress(goal: Goal): Float {
        return if (goal.targetValue > 0) {
            ((goal.currentValue / goal.targetValue) * 100.0).toFloat().coerceAtMost(100f)
        } else {
            0f
        }
    }

    /**
     * Checks if goal is overdue
     */
    fun isGoalOverdue(goal: Goal): Boolean {
        val now = Date()
        return !goal.isAchieved && goal.targetDate.before(now)
    }

    /**
     * Gets days remaining to target date
     */
    fun getDaysRemaining(goal: Goal): Int {
        val now = Date()
        val timeDiff = goal.targetDate.time - now.time
        val daysDiff = timeDiff / (1000 * 60 * 60 * 24)
        return daysDiff.toInt().coerceAtLeast(0)
    }

    /**
     * Validates goal input
     */
    fun validateGoalInput(
        title: String,
        targetValue: Double,
        targetDate: Date,
    ): GoalValidationResult {
        return when {
            title.isBlank() -> GoalValidationResult(false, "Goal title cannot be empty")
            targetValue <= 0 -> GoalValidationResult(false, "Target value must be greater than 0")
            targetDate.before(Date()) -> GoalValidationResult(false, "Target date cannot be in the past")
            targetValue > 1000000 -> GoalValidationResult(false, "Target value seems too large")
            else -> GoalValidationResult(true, "Valid goal")
        }
    }

    /**
     * Gets goal recommendations based on goal type
     */
    fun getGoalRecommendations(goalType: GoalType): List<GoalRecommendation> {
        return when (goalType) {
            GoalType.WEIGHT_LOSS -> listOf(
                GoalRecommendation("Lose 1-2 lbs per week", 1.0, "lbs/week"),
                GoalRecommendation("Lose 5 lbs in 1 month", 5.0, "lbs"),
                GoalRecommendation("Lose 10 lbs in 2 months", 10.0, "lbs"),
            )
            GoalType.WEIGHT_GAIN -> listOf(
                GoalRecommendation("Gain 0.5-1 lb per week", 0.5, "lbs/week"),
                GoalRecommendation("Gain 2 lbs in 1 month", 2.0, "lbs"),
                GoalRecommendation("Gain 5 lbs in 3 months", 5.0, "lbs"),
            )
            GoalType.STEP_COUNT -> listOf(
                GoalRecommendation("Walk 10,000 steps daily", 10000.0, "steps"),
                GoalRecommendation("Walk 12,500 steps daily", 12500.0, "steps"),
                GoalRecommendation("Walk 15,000 steps daily", 15000.0, "steps"),
            )
            GoalType.DISTANCE_RUNNING -> listOf(
                GoalRecommendation("Run 5K", 5.0, "km"),
                GoalRecommendation("Run 10K", 10.0, "km"),
                GoalRecommendation("Run half marathon", 21.1, "km"),
            )
            GoalType.CALORIE_BURN -> listOf(
                GoalRecommendation("Burn 300 calories daily", 300.0, "calories"),
                GoalRecommendation("Burn 500 calories daily", 500.0, "calories"),
                GoalRecommendation("Burn 750 calories daily", 750.0, "calories"),
            )
            GoalType.WORKOUT_FREQUENCY -> listOf(
                GoalRecommendation("Workout 3 times per week", 3.0, "times/week"),
                GoalRecommendation("Workout 4 times per week", 4.0, "times/week"),
                GoalRecommendation("Workout 5 times per week", 5.0, "times/week"),
            )
            else -> listOf(
                GoalRecommendation("Custom goal", 1.0, "units"),
            )
        }
    }

    /**
     * Gets motivational message based on progress
     */
    fun getMotivationalMessage(goal: Goal): String {
        val progress = calculateGoalProgress(goal)
        val daysRemaining = getDaysRemaining(goal)

        return when {
            goal.isAchieved -> "🎉 Congratulations! You've achieved your goal!"
            progress >= 90f -> "You're so close! Just ${goal.targetValue - goal.currentValue} ${goal.unit} to go!"
            progress >= 75f -> "Great progress! You're in the final stretch!"
            progress >= 50f -> "Halfway there! Keep up the excellent work!"
            progress >= 25f -> "Good start! You're building momentum!"
            daysRemaining <= 7 && progress < 80f -> "Time is running out! Consider adjusting your approach or extending the deadline."
            else -> "Every step counts! Stay consistent and you'll get there!"
        }
    }

    /**
     * Calculates estimated completion date based on current progress
     */
    fun getEstimatedCompletionDate(goal: Goal): Date? {
        if (goal.isAchieved || goal.currentValue <= 0) return null

        val now = Date()
        val daysSinceCreated = (now.time - goal.createdAt.time) / (1000 * 60 * 60 * 24)
        if (daysSinceCreated <= 0) return null

        val dailyProgress = goal.currentValue / daysSinceCreated.toDouble()
        if (dailyProgress <= 0) return null

        val remainingValue = goal.targetValue - goal.currentValue
        val daysToComplete = remainingValue / dailyProgress

        return Date(now.time + (daysToComplete * 24 * 60 * 60 * 1000).toLong())
    }
}

/**
 * Data class for goal progress updates
 */
data class GoalProgressUpdate(
    val goal: Goal?,
    val isAchieved: Boolean,
)

/**
 * Data class for goal validation results
 */
data class GoalValidationResult(
    val isValid: Boolean,
    val message: String,
)

/**
 * Data class for goal recommendations
 */
data class GoalRecommendation(
    val title: String,
    val targetValue: Double,
    val unit: String,
)
