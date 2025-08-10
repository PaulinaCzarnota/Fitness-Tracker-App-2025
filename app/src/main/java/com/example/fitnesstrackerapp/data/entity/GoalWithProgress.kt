package com.example.fitnesstrackerapp.data.entity

/**
 * Goal with Progress Data Class
 *
 * Responsibilities:
 * - Combines Goal entity with calculated progress information
 * - Provides progress tracking and analysis methods
 * - Used for displaying goal progress in UI components
 */

import androidx.room.Embedded

data class GoalWithProgress(
    @Embedded val goal: Goal,
    val progressPercentage: Float = 0f,
    val daysRemaining: Int = 0,
    val isOnTrack: Boolean = false,
) {
    /**
     * Calculate if the goal is achievable based on current progress.
     * This is a simple heuristic and does not guarantee achievability.
     */
    fun isAchievable(): Boolean {
        if (goal.isAchieved) return true
        if (daysRemaining <= 0) return false

        val remainingValue = goal.targetValue - goal.currentValue
        val requiredDailyProgress = remainingValue / daysRemaining.coerceAtLeast(1)

        return requiredDailyProgress > 0
    }

    /**
     * Get motivational message based on progress.
     * Thresholds are arbitrary and can be adjusted for your app's tone.
     */
    fun getMotivationalMessage(): String {
        return when {
            goal.isAchieved -> "🎉 Goal achieved! Great work!"
            progressPercentage >= 90f -> "💪 Almost there! You've got this!"
            progressPercentage >= 75f -> "🔥 Great progress! Keep it up!"
            progressPercentage >= 50f -> "📈 Halfway there! Stay motivated!"
            progressPercentage >= 25f -> "🚀 Making good progress!"
            else -> "✨ Every step counts! Let's go!"
        }
    }

    /**
     * Get status of the goal
     */
    fun getStatus(): GoalStatus {
        return when {
            goal.isAchieved -> GoalStatus.COMPLETED
            daysRemaining <= 0 && !goal.isAchieved -> GoalStatus.OVERDUE
            progressPercentage < 25f && daysRemaining < 7 -> GoalStatus.PAUSED
            else -> GoalStatus.ACTIVE
        }
    }
}
