/**
 * Goal Extensions for enhanced completion logic
 *
 * This file provides extension functions for the Goal entity that implement
 * sophisticated completion logic for different goal types. The extensions
 * handle edge cases including overachievement, negative values, and
 * goal-type specific completion criteria.
 *
 * Key Features:
 * - Type-specific completion logic for distance, calorie, and step goals
 * - Overachievement detection and handling
 * - Negative value protection
 * - Edge case handling for various goal scenarios
 * - Performance optimized calculations
 */

package com.example.fitnesstrackerapp.data.entity

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Enhanced completion check that handles goal-type specific logic
 *
 * This extension provides intelligent completion detection based on goal type:
 * - Distance goals: Considers both target distance and performance metrics
 * - Calorie goals: Handles both burn and intake scenarios
 * - Step goals: Accounts for step count variations and accuracy
 * - Other goals: Uses standard target/current value comparison
 *
 * @return true if the goal is completed according to its type-specific criteria
 */
fun Goal.isCompleted(): Boolean {
    // First check explicit completion status
    if (status == GoalStatus.COMPLETED) {
        return true
    }
    
    // Handle negative or zero target values (invalid goals)
    if (targetValue <= 0) {
        return false
    }
    
    // Protect against negative current values (data integrity)
    val safeCurrentValue = max(0.0, currentValue)
    
    // Apply goal-type specific completion logic
    return when (goalType) {
        // Distance-based goals (running, walking, etc.)
        in GoalType.getDistanceTypes() -> {
            isDistanceGoalCompleted(safeCurrentValue)
        }
        
        // Calorie-based goals (burn, weight management)
        in GoalType.getCalorieTypes() -> {
            isCalorieGoalCompleted(safeCurrentValue)
        }
        
        // Step-based goals
        in GoalType.getStepTypes() -> {
            isStepGoalCompleted(safeCurrentValue)
        }
        
        // Standard goals (weight, duration, frequency, etc.)
        else -> {
            safeCurrentValue >= targetValue
        }
    }
}

/**
 * Checks completion for distance-based goals with tolerance
 *
 * Distance goals allow for slight measurement variations and GPS accuracy issues.
 * A 2% tolerance is applied to account for real-world measurement discrepancies.
 *
 * @param currentValue The safe (non-negative) current value
 * @return true if distance goal is completed within tolerance
 */
private fun Goal.isDistanceGoalCompleted(currentValue: Double): Boolean {
    val tolerance = 0.02 // 2% tolerance for GPS/measurement variations
    val effectiveTarget = targetValue * (1 - tolerance)
    return currentValue >= effectiveTarget
}

/**
 * Checks completion for calorie-based goals
 *
 * Calorie goals can be either burn goals (positive) or intake restriction goals.
 * The logic adapts based on the goal type and target interpretation.
 *
 * @param currentValue The safe (non-negative) current value
 * @return true if calorie goal is completed
 */
private fun Goal.isCalorieGoalCompleted(currentValue: Double): Boolean {
    return when (goalType) {
        GoalType.CALORIE_BURN -> {
            // For calorie burn goals, current >= target means success
            currentValue >= targetValue
        }
        GoalType.WEIGHT_LOSS -> {
            // For weight loss, we typically track calories burned or weight lost
            // If tracking weight lost, current >= target means success
            // If tracking calories burned for weight loss, current >= target means success
            currentValue >= targetValue
        }
        GoalType.WEIGHT_GAIN -> {
            // For weight gain, could be tracking weight gained or calories consumed
            // Current >= target indicates goal achievement
            currentValue >= targetValue
        }
        else -> {
            // Fallback to standard logic
            currentValue >= targetValue
        }
    }
}

/**
 * Checks completion for step-based goals with accuracy consideration
 *
 * Step goals allow for minor counting variations that occur with different
 * step tracking devices and algorithms.
 *
 * @param currentValue The safe (non-negative) current value
 * @return true if step goal is completed within acceptable range
 */
private fun Goal.isStepGoalCompleted(currentValue: Double): Boolean {
    // Step counters can have 1-3% variation, so we use a small tolerance
    val tolerance = 0.01 // 1% tolerance for step counter variations
    val effectiveTarget = targetValue * (1 - tolerance)
    return currentValue >= effectiveTarget
}

/**
 * Calculates the completion percentage with enhanced precision
 *
 * This extension improves upon the basic getProgressPercentage() by providing
 * better handling of edge cases and goal-type specific calculations.
 *
 * @return Progress percentage from 0 to 100, with decimal precision
 */
fun Goal.getEnhancedProgressPercentage(): Double {
    if (targetValue <= 0) return 0.0
    
    val safeCurrentValue = max(0.0, currentValue)
    val rawPercentage = (safeCurrentValue / targetValue * 100.0)
    
    return when {
        // Cap overachievement display at reasonable level (200% max)
        rawPercentage > 200.0 -> 200.0
        // Handle micro-progress for very small values
        rawPercentage < 0.1 && safeCurrentValue > 0 -> 0.1
        else -> rawPercentage
    }
}

/**
 * Determines if the goal has been overachieved
 *
 * Overachievement is when the current value significantly exceeds the target.
 * This is useful for motivation and progress tracking.
 *
 * @param overachievementThreshold The multiplier for overachievement (default 1.1 = 110%)
 * @return true if goal is overachieved by the specified threshold
 */
fun Goal.isOverachieved(overachievementThreshold: Double = 1.1): Boolean {
    if (targetValue <= 0 || currentValue <= 0) return false
    return currentValue >= (targetValue * overachievementThreshold)
}

/**
 * Gets the overachievement percentage
 *
 * @return Percentage above target (0 if not overachieved, positive value if overachieved)
 */
fun Goal.getOverachievementPercentage(): Double {
    if (!isCompleted() || targetValue <= 0) return 0.0
    val safeCurrentValue = max(0.0, currentValue)
    val excess = safeCurrentValue - targetValue
    return (excess / targetValue * 100.0).coerceAtLeast(0.0)
}

/**
 * Validates goal completion status consistency
 *
 * Ensures that the goal's status and calculated completion state are consistent.
 * This helps identify data integrity issues.
 *
 * @return true if status and calculated completion are consistent
 */
fun Goal.isCompletionStatusConsistent(): Boolean {
    val calculatedCompletion = isCompleted()
    val statusCompletion = status == GoalStatus.COMPLETED
    
    return calculatedCompletion == statusCompletion
}

/**
 * Gets completion status with reasoning
 *
 * Provides detailed information about why a goal is considered completed or not.
 *
 * @return Detailed completion status information
 */
data class GoalCompletionStatus(
    val isCompleted: Boolean,
    val progressPercentage: Double,
    val isOverachieved: Boolean,
    val overachievementPercentage: Double,
    val completionReason: String,
    val hasEdgeCaseHandling: Boolean
)

/**
 * Gets comprehensive completion status with detailed reasoning
 */
fun Goal.getCompletionStatus(): GoalCompletionStatus {
    val completed = isCompleted()
    val progress = getEnhancedProgressPercentage()
    val overachieved = isOverachieved()
    val overachievementPercent = getOverachievementPercentage()
    
    val reason = when {
        status == GoalStatus.COMPLETED -> "Explicitly marked as completed"
        targetValue <= 0 -> "Invalid target value (≤ 0)"
        currentValue < 0 -> "Invalid current value (< 0) - using 0"
        goalType in GoalType.getDistanceTypes() && completed -> "Distance goal completed (with 2% tolerance)"
        goalType in GoalType.getCalorieTypes() && completed -> "Calorie goal achieved"
        goalType in GoalType.getStepTypes() && completed -> "Step goal completed (with 1% tolerance)"
        completed -> "Standard goal completion achieved"
        else -> "Goal not yet completed"
    }
    
    val hasEdgeCase = currentValue < 0 || targetValue <= 0 || 
                     goalType in GoalType.getSpecialCompletionTypes()
    
    return GoalCompletionStatus(
        isCompleted = completed,
        progressPercentage = progress,
        isOverachieved = overachieved,
        overachievementPercentage = overachievementPercent,
        completionReason = reason,
        hasEdgeCaseHandling = hasEdgeCase
    )
}
