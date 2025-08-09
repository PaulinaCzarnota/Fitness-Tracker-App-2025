/**
 * Comprehensive Test Suite for Goal Extensions
 *
 * This test file provides thorough coverage of the enhanced goal completion logic,
 * including edge cases for overachievement, negative values, and goal-type specific
 * completion criteria. Tests cover distance, calorie, and step goal types with
 * various scenarios to ensure robust functionality.
 *
 * Test Categories:
 * - Basic completion logic for all goal types
 * - Distance goal completion with GPS tolerance
 * - Calorie goal completion for different scenarios
 * - Step goal completion with device variation tolerance
 * - Edge case handling (negative values, zero targets, overachievement)
 * - Progress calculation accuracy
 * - Completion status consistency
 */

package com.example.fitnesstrackerapp.data.entity

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Date
import java.util.Calendar
import kotlin.math.abs

class GoalExtensionsTest {

    private lateinit var testDate: Date
    private val userId = 1L

    @Before
    fun setUp() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, 30) // 30 days from now
        testDate = calendar.time
    }

    // MARK: - Distance Goal Tests

    @Test
    fun `isCompleted should return true for distance goal with exact target`() {
        val goal = createDistanceGoal(targetValue = 10.0, currentValue = 10.0)
        assertTrue("Distance goal with exact target should be completed", goal.isCompleted())
    }

    @Test
    fun `isCompleted should return true for distance goal within tolerance`() {
        val goal = createDistanceGoal(targetValue = 10.0, currentValue = 9.81) // 1.9% under target
        assertTrue("Distance goal within 2% tolerance should be completed", goal.isCompleted())
    }

    @Test
    fun `isCompleted should return false for distance goal outside tolerance`() {
        val goal = createDistanceGoal(targetValue = 10.0, currentValue = 9.79) // 2.1% under target
        assertFalse("Distance goal outside 2% tolerance should not be completed", goal.isCompleted())
    }

    @Test
    fun `isCompleted should handle distance goal overachievement`() {
        val goal = createDistanceGoal(targetValue = 10.0, currentValue = 15.0) // 150% of target
        assertTrue("Distance goal with overachievement should be completed", goal.isCompleted())
        assertTrue("Overachieved distance goal should be detected", goal.isOverachieved())
    }

    @Test
    fun `isCompleted should handle distance goal with step count type`() {
        val goal = createGoal(
            goalType = GoalType.STEP_COUNT,
            targetValue = 10000.0,
            currentValue = 9901.0 // 0.99% under target
        )
        assertTrue("Step count goal within 1% tolerance should be completed", goal.isCompleted())
    }

    // MARK: - Calorie Goal Tests

    @Test
    fun `isCompleted should return true for calorie burn goal meeting target`() {
        val goal = createCalorieGoal(targetValue = 500.0, currentValue = 500.0)
        assertTrue("Calorie burn goal meeting target should be completed", goal.isCompleted())
    }

    @Test
    fun `isCompleted should return true for calorie burn goal exceeding target`() {
        val goal = createCalorieGoal(targetValue = 500.0, currentValue = 600.0)
        assertTrue("Calorie burn goal exceeding target should be completed", goal.isCompleted())
    }

    @Test
    fun `isCompleted should return false for calorie burn goal under target`() {
        val goal = createCalorieGoal(targetValue = 500.0, currentValue = 450.0)
        assertFalse("Calorie burn goal under target should not be completed", goal.isCompleted())
    }

    @Test
    fun `isCompleted should handle weight loss calorie goals correctly`() {
        val goal = createGoal(
            goalType = GoalType.WEIGHT_LOSS,
            targetValue = 5.0, // 5kg to lose
            currentValue = 5.0  // 5kg lost
        )
        assertTrue("Weight loss goal meeting target should be completed", goal.isCompleted())
    }

    @Test
    fun `isCompleted should handle weight gain calorie goals correctly`() {
        val goal = createGoal(
            goalType = GoalType.WEIGHT_GAIN,
            targetValue = 3.0, // 3kg to gain
            currentValue = 3.5  // 3.5kg gained (overachievement)
        )
        assertTrue("Weight gain goal exceeding target should be completed", goal.isCompleted())
        assertTrue("Weight gain overachievement should be detected", goal.isOverachieved())
    }

    // MARK: - Step Goal Tests

    @Test
    fun `isCompleted should return true for step goal with exact target`() {
        val goal = createStepGoal(targetValue = 10000.0, currentValue = 10000.0)
        assertTrue("Step goal with exact target should be completed", goal.isCompleted())
    }

    @Test
    fun `isCompleted should return true for step goal within tolerance`() {
        val goal = createStepGoal(targetValue = 10000.0, currentValue = 9901.0) // 0.99% under
        assertTrue("Step goal within 1% tolerance should be completed", goal.isCompleted())
    }

    @Test
    fun `isCompleted should return false for step goal outside tolerance`() {
        val goal = createStepGoal(targetValue = 10000.0, currentValue = 9899.0) // 1.01% under
        assertFalse("Step goal outside 1% tolerance should not be completed", goal.isCompleted())
    }

    @Test
    fun `isCompleted should handle step goal massive overachievement`() {
        val goal = createStepGoal(targetValue = 8000.0, currentValue = 20000.0) // 250% of target
        assertTrue("Step goal with massive overachievement should be completed", goal.isCompleted())
        assertTrue("Massive step overachievement should be detected", goal.isOverachieved())
        
        val overachievementPercent = goal.getOverachievementPercentage()
        assertTrue("Overachievement percentage should be calculated correctly", 
                  abs(overachievementPercent - 150.0) < 0.1) // 150% over target
    }

    // MARK: - Edge Case Tests

    @Test
    fun `isCompleted should handle negative current values`() {
        val goal = createGoal(
            goalType = GoalType.WEIGHT_LOSS,
            targetValue = 5.0,
            currentValue = -2.0 // Invalid negative value
        )
        assertFalse("Goal with negative current value should not be completed", goal.isCompleted())
        
        val status = goal.getCompletionStatus()
        assertTrue("Edge case handling should be detected", status.hasEdgeCaseHandling)
        assertEquals("Progress should be 0 for negative current value", 0.0, status.progressPercentage, 0.01)
    }

    @Test
    fun `isCompleted should handle zero target values`() {
        val goal = createGoal(
            goalType = GoalType.CALORIE_BURN,
            targetValue = 0.0, // Invalid zero target
            currentValue = 100.0
        )
        assertFalse("Goal with zero target value should not be completed", goal.isCompleted())
        
        val status = goal.getCompletionStatus()
        assertTrue("Edge case handling should be detected", status.hasEdgeCaseHandling)
        assertEquals("Invalid target value reason should be provided", 
                    "Invalid target value (≤ 0)", status.completionReason)
    }

    @Test
    fun `isCompleted should handle negative target values`() {
        val goal = createGoal(
            goalType = GoalType.STEP_COUNT,
            targetValue = -5000.0, // Invalid negative target
            currentValue = 8000.0
        )
        assertFalse("Goal with negative target value should not be completed", goal.isCompleted())
    }

    @Test
    fun `isCompleted should prioritize explicit completion status`() {
        val goal = createGoal(
            goalType = GoalType.DISTANCE_RUNNING,
            targetValue = 10.0,
            currentValue = 5.0, // Only 50% complete
            status = GoalStatus.COMPLETED // But explicitly marked as completed
        )
        assertTrue("Explicitly completed goal should return true regardless of progress", 
                  goal.isCompleted())
        
        val status = goal.getCompletionStatus()
        assertEquals("Explicit completion reason should be provided", 
                    "Explicitly marked as completed", status.completionReason)
    }

    // MARK: - Progress Calculation Tests

    @Test
    fun `getEnhancedProgressPercentage should handle normal progress`() {
        val goal = createGoal(
            goalType = GoalType.WEIGHT_LOSS,
            targetValue = 10.0,
            currentValue = 7.5
        )
        val progress = goal.getEnhancedProgressPercentage()
        assertEquals("Progress should be 75%", 75.0, progress, 0.01)
    }

    @Test
    fun `getEnhancedProgressPercentage should cap overachievement at 200 percent`() {
        val goal = createGoal(
            goalType = GoalType.STEP_COUNT,
            targetValue = 5000.0,
            currentValue = 15000.0 // 300% of target
        )
        val progress = goal.getEnhancedProgressPercentage()
        assertEquals("Progress should be capped at 200%", 200.0, progress, 0.01)
    }

    @Test
    fun `getEnhancedProgressPercentage should handle micro progress`() {
        val goal = createGoal(
            goalType = GoalType.CALORIE_BURN,
            targetValue = 10000.0,
            currentValue = 1.0 // Very small progress
        )
        val progress = goal.getEnhancedProgressPercentage()
        assertEquals("Micro progress should be shown as 0.1%", 0.1, progress, 0.01)
    }

    @Test
    fun `getEnhancedProgressPercentage should handle zero target gracefully`() {
        val goal = createGoal(
            goalType = GoalType.FITNESS,
            targetValue = 0.0, // Invalid
            currentValue = 50.0
        )
        val progress = goal.getEnhancedProgressPercentage()
        assertEquals("Zero target should result in 0% progress", 0.0, progress, 0.01)
    }

    // MARK: - Overachievement Tests

    @Test
    fun `isOverachieved should detect default 110 percent threshold`() {
        val goal = createGoal(
            goalType = GoalType.WORKOUT_FREQUENCY,
            targetValue = 4.0, // 4 workouts per week
            currentValue = 4.5 // 112.5% of target
        )
        assertTrue("Goal with 112.5% achievement should be detected as overachieved", 
                  goal.isOverachieved())
    }

    @Test
    fun `isOverachieved should not trigger below threshold`() {
        val goal = createGoal(
            goalType = GoalType.ENDURANCE,
            targetValue = 30.0, // 30 minutes
            currentValue = 32.0 // 106.7% of target
        )
        assertFalse("Goal with 106.7% achievement should not be overachieved", 
                   goal.isOverachieved())
    }

    @Test
    fun `isOverachieved should work with custom threshold`() {
        val goal = createGoal(
            goalType = GoalType.FLEXIBILITY,
            targetValue = 5.0,
            currentValue = 6.0 // 120% of target
        )
        assertTrue("Goal should be overachieved with 115% threshold", 
                  goal.isOverachieved(1.15))
        assertFalse("Goal should not be overachieved with 125% threshold", 
                   goal.isOverachieved(1.25))
    }

    @Test
    fun `getOverachievementPercentage should calculate correctly`() {
        val goal = createGoal(
            goalType = GoalType.STRENGTH_TRAINING,
            targetValue = 100.0,
            currentValue = 130.0 // 130% of target = 30% overachievement
        )
        val overachievementPercent = goal.getOverachievementPercentage()
        assertEquals("Overachievement should be 30%", 30.0, overachievementPercent, 0.01)
    }

    @Test
    fun `getOverachievementPercentage should return zero for incomplete goals`() {
        val goal = createGoal(
            goalType = GoalType.HYDRATION,
            targetValue = 8.0, // 8 liters
            currentValue = 6.0  // 75% complete
        )
        val overachievementPercent = goal.getOverachievementPercentage()
        assertEquals("Incomplete goal should have 0% overachievement", 0.0, overachievementPercent, 0.01)
    }

    // MARK: - Standard Goal Type Tests

    @Test
    fun `isCompleted should work for standard goal types without special logic`() {
        val goal = createGoal(
            goalType = GoalType.WORKOUT_FREQUENCY,
            targetValue = 5.0, // 5 workouts per week
            currentValue = 5.0  // Exactly 5 workouts
        )
        assertTrue("Standard goal meeting target should be completed", goal.isCompleted())
    }

    @Test
    fun `isCompleted should work for other goal type`() {
        val goal = createGoal(
            goalType = GoalType.OTHER,
            targetValue = 20.0,
            currentValue = 25.0
        )
        assertTrue("Other goal type exceeding target should be completed", goal.isCompleted())
    }

    // MARK: - Completion Status Tests

    @Test
    fun `getCompletionStatus should provide comprehensive information`() {
        val goal = createGoal(
            goalType = GoalType.DISTANCE_RUNNING,
            targetValue = 5.0,
            currentValue = 4.95 // Within tolerance
        )
        
        val status = goal.getCompletionStatus()
        assertTrue("Goal should be completed", status.isCompleted)
        assertTrue("Goal should have enhanced progress >90%", status.progressPercentage > 90.0)
        assertFalse("Goal should not be overachieved", status.isOverachieved)
        assertEquals("Should have distance completion reason", 
                    "Distance goal completed (with 2% tolerance)", status.completionReason)
        assertTrue("Should have edge case handling for distance type", status.hasEdgeCaseHandling)
    }

    @Test
    fun `isCompletionStatusConsistent should detect inconsistencies`() {
        // Create goal that should be completed by calculation but has wrong status
        val inconsistentGoal = createGoal(
            goalType = GoalType.CALORIE_BURN,
            targetValue = 300.0,
            currentValue = 350.0, // Clearly completed
            status = GoalStatus.ACTIVE // But status is still active
        )
        
        assertTrue("Goal should be calculated as completed", inconsistentGoal.isCompleted())
        assertFalse("Goal should have inconsistent status", 
                   inconsistentGoal.isCompletionStatusConsistent())
    }

    // MARK: - Helper Methods

    private fun createDistanceGoal(targetValue: Double, currentValue: Double): Goal {
        return createGoal(GoalType.DISTANCE_RUNNING, targetValue, currentValue)
    }

    private fun createCalorieGoal(targetValue: Double, currentValue: Double): Goal {
        return createGoal(GoalType.CALORIE_BURN, targetValue, currentValue)
    }

    private fun createStepGoal(targetValue: Double, currentValue: Double): Goal {
        return createGoal(GoalType.STEP_COUNT, targetValue, currentValue)
    }

    private fun createGoal(
        goalType: GoalType,
        targetValue: Double,
        currentValue: Double,
        status: GoalStatus = GoalStatus.ACTIVE
    ): Goal {
        return Goal(
            userId = userId,
            title = "Test ${goalType.name} Goal",
            description = "Test goal for ${goalType.name}",
            goalType = goalType,
            targetValue = targetValue,
            currentValue = currentValue,
            unit = getUnitForGoalType(goalType),
            targetDate = testDate,
            status = status
        )
    }

    private fun getUnitForGoalType(goalType: GoalType): String {
        return when (goalType) {
            GoalType.WEIGHT_LOSS, GoalType.WEIGHT_GAIN, GoalType.MUSCLE_BUILDING -> "kg"
            GoalType.DISTANCE_RUNNING -> "km"
            GoalType.WORKOUT_FREQUENCY -> "times"
            GoalType.CALORIE_BURN -> "kcal"
            GoalType.STEP_COUNT -> "steps"
            GoalType.DURATION_EXERCISE, GoalType.ENDURANCE -> "minutes"
            GoalType.STRENGTH_TRAINING -> "kg"
            GoalType.FLEXIBILITY -> "sessions"
            GoalType.BODY_FAT -> "%"
            GoalType.HYDRATION -> "liters"
            GoalType.SLEEP -> "hours"
            GoalType.FITNESS -> "score"
            GoalType.OTHER -> "units"
        }
    }
}
