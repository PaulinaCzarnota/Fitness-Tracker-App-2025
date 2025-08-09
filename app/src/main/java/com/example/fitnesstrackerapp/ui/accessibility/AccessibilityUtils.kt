/**
 * Accessibility Enhancement Utilities for Fitness Tracker App
 *
 * Features:
 * - Comprehensive content descriptions
 * - Screen reader optimizations
 * - Color contrast validation
 * - Touch target size verification
 * - Voice-over friendly components
 * - Accessibility action implementations
 */

package com.example.fitnesstrackerapp.ui.accessibility

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.semantics.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.*

/**
 * Accessibility constants following WCAG guidelines
 */
object AccessibilityConstants {
    // Minimum touch target size (48dp as per Material Design)
    val MIN_TOUCH_TARGET_SIZE = 48.dp

    // Color contrast ratios
    const val MIN_CONTRAST_RATIO_NORMAL = 4.5
    const val MIN_CONTRAST_RATIO_LARGE = 3.0

    // Animation preferences
    const val REDUCED_MOTION_DURATION = 150
    const val NORMAL_MOTION_DURATION = 300
}

/**
 * Accessibility modifier extensions
 */
fun Modifier.fitnessContentDescription(
    value: String,
    stepProgress: Int? = null,
    totalSteps: Int? = null,
    isAchievement: Boolean = false,
): Modifier = this.semantics {
    contentDescription = when {
        stepProgress != null && totalSteps != null ->
            "$value. Progress: $stepProgress out of $totalSteps steps. ${(stepProgress.toFloat() / totalSteps * 100).toInt()}% complete."
        isAchievement -> "$value. Achievement unlocked!"
        else -> value
    }
}

fun Modifier.workoutAccessibility(
    workoutName: String,
    duration: String,
    calories: Int,
    isActive: Boolean = false,
): Modifier = this.semantics {
    contentDescription = if (isActive) {
        "Current workout: $workoutName. Duration: $duration. Calories burned: $calories. Workout in progress."
    } else {
        "Workout: $workoutName. Duration: $duration. Calories burned: $calories. Completed workout."
    }

    if (isActive) {
        stateDescription = "Active"
    }
}

fun Modifier.progressAccessibility(
    current: Float,
    target: Float,
    unit: String,
    progressType: String = "Progress",
): Modifier = this.semantics {
    val percentage = ((current / target) * 100).roundToInt()
    contentDescription = "$progressType: $current out of $target $unit. $percentage% complete."

    // Add role for progress indicators
    role = Role.ProgressIndicator

    // Add state description
    stateDescription = when {
        percentage >= 100 -> "Goal achieved"
        percentage >= 80 -> "Almost there"
        percentage >= 50 -> "Half way"
        percentage >= 25 -> "Good start"
        else -> "Just started"
    }
}

fun Modifier.nutritionAccessibility(
    foodName: String,
    calories: Int,
    protein: Float? = null,
    carbs: Float? = null,
    fat: Float? = null,
): Modifier = this.semantics {
    contentDescription = buildString {
        append("$foodName, $calories calories")
        if (protein != null) append(", ${protein}g protein")
        if (carbs != null) append(", ${carbs}g carbohydrates")
        if (fat != null) append(", ${fat}g fat")
    }
}

fun Modifier.achievementAccessibility(
    title: String,
    description: String,
    isUnlocked: Boolean,
    progress: Int? = null,
    target: Int? = null,
): Modifier = this.semantics {
    contentDescription = if (isUnlocked) {
        "Achievement unlocked: $title. $description"
    } else {
        buildString {
            append("Locked achievement: $title. $description")
            if (progress != null && target != null) {
                append(". Progress: $progress out of $target.")
            }
        }
    }

    stateDescription = if (isUnlocked) "Unlocked" else "Locked"
    role = Role.Image
}

/**
 * Ensures minimum touch target size for accessibility
 */
fun Modifier.accessibleTouchTarget(
    minSize: Dp = AccessibilityConstants.MIN_TOUCH_TARGET_SIZE,
): Modifier = this.size(minSize)

/**
 * Color contrast validation utilities
 */
object ColorAccessibility {

    /**
     * Calculates the contrast ratio between two colors
     */
    fun contrastRatio(color1: Color, color2: Color): Double {
        val luminance1 = color1.luminance() + 0.05
        val luminance2 = color2.luminance() + 0.05

        return max(luminance1, luminance2) / min(luminance1, luminance2)
    }

    /**
     * Checks if color combination meets WCAG AA standards
     */
    fun isAccessibleContrast(
        foreground: Color,
        background: Color,
        isLargeText: Boolean = false,
    ): Boolean {
        val ratio = contrastRatio(foreground, background)
        val minRatio = if (isLargeText) {
            AccessibilityConstants.MIN_CONTRAST_RATIO_LARGE
        } else {
            AccessibilityConstants.MIN_CONTRAST_RATIO_NORMAL
        }

        return ratio >= minRatio
    }

    /**
     * Suggests an accessible color if current combination doesn't meet standards
     */
    fun suggestAccessibleColor(
        foreground: Color,
        background: Color,
        isLargeText: Boolean = false,
    ): Color? {
        if (isAccessibleContrast(foreground, background, isLargeText)) {
            return null // Already accessible
        }

        val minRatio = if (isLargeText) {
            AccessibilityConstants.MIN_CONTRAST_RATIO_LARGE
        } else {
            AccessibilityConstants.MIN_CONTRAST_RATIO_NORMAL
        }

        // Try making the foreground darker
        var adjustedColor = foreground
        for (factor in 0.9 downTo 0.1 step 0.1) {
            adjustedColor = Color(
                red = foreground.red * factor.toFloat(),
                green = foreground.green * factor.toFloat(),
                blue = foreground.blue * factor.toFloat(),
                alpha = foreground.alpha,
            )

            if (contrastRatio(adjustedColor, background) >= minRatio) {
                return adjustedColor
            }
        }

        // If darkening doesn't work, try lightening
        for (factor in 1.1..2.0 step 0.1) {
            adjustedColor = Color(
                red = min(1f, foreground.red * factor.toFloat()),
                green = min(1f, foreground.green * factor.toFloat()),
                blue = min(1f, foreground.blue * factor.toFloat()),
                alpha = foreground.alpha,
            )

            if (contrastRatio(adjustedColor, background) >= minRatio) {
                return adjustedColor
            }
        }

        // Return high contrast fallback
        return if (background.luminance() > 0.5) Color.Black else Color.White
    }
}

/**
 * Accessibility-aware animation utilities
 */
object AnimationAccessibility {

    /**
     * Returns appropriate animation duration based on accessibility preferences
     */
    @Composable
    fun getAnimationDuration(normalDuration: Int = AccessibilityConstants.NORMAL_MOTION_DURATION): Int {
        // In a real app, this would check system accessibility settings
        // For now, we'll return normal duration
        return normalDuration
    }

    /**
     * Checks if animations should be reduced for accessibility
     */
    @Composable
    fun shouldReduceMotion(): Boolean {
        // In a real app, this would check system accessibility settings
        // For now, we'll return false
        return false
    }
}

/**
 * Fitness-specific accessibility descriptions
 */
object FitnessAccessibilityText {

    fun stepCountDescription(steps: Int, goal: Int): String {
        val percentage = ((steps.toFloat() / goal) * 100).roundToInt()
        return when {
            steps >= goal -> "Daily step goal achieved! $steps out of $goal steps completed."
            percentage >= 80 -> "Almost there! $steps out of $goal steps. $percentage% of daily goal."
            percentage >= 50 -> "Halfway to your goal! $steps out of $goal steps. $percentage% complete."
            percentage >= 25 -> "Good progress! $steps out of $goal steps. $percentage% of daily goal."
            else -> "Getting started! $steps out of $goal steps. $percentage% of daily goal."
        }
    }

    fun workoutStatusDescription(
        isActive: Boolean,
        workoutName: String,
        duration: String,
        intensity: String? = null,
    ): String = when {
        isActive ->
            "Currently doing $workoutName workout. Duration: $duration." +
                if (intensity != null) " Intensity: $intensity." else ""
        else ->
            "Completed $workoutName workout. Duration: $duration." +
                if (intensity != null) " Intensity: $intensity." else ""
    }

    fun goalProgressDescription(
        goalName: String,
        current: Float,
        target: Float,
        unit: String,
        daysLeft: Int? = null,
    ): String {
        val percentage = ((current / target) * 100).roundToInt()
        val baseDescription = "Goal: $goalName. Progress: $current out of $target $unit. $percentage% complete."

        return if (daysLeft != null) {
            "$baseDescription $daysLeft days remaining."
        } else {
            baseDescription
        }
    }

    fun nutritionSummaryDescription(
        calories: Int,
        protein: Float,
        carbs: Float,
        fat: Float,
        calorieGoal: Int? = null,
    ): String = buildString {
        append("Daily nutrition: $calories calories")
        if (calorieGoal != null) {
            val remaining = calorieGoal - calories
            append(" out of $calorieGoal goal. $remaining calories remaining.")
        } else {
            append(".")
        }
        append(" Macronutrients: ${protein}g protein, ${carbs}g carbohydrates, ${fat}g fat.")
    }
}

/**
 * Accessibility action helpers
 */
object AccessibilityActions {

    /**
     * Creates custom accessibility actions for workout controls
     */
    fun workoutActions(
        onStart: (() -> Unit)? = null,
        onPause: (() -> Unit)? = null,
        onStop: (() -> Unit)? = null,
        onSkip: (() -> Unit)? = null,
    ): List<CustomAccessibilityAction> = buildList {
        onStart?.let {
            add(CustomAccessibilityAction("Start workout", it))
        }
        onPause?.let {
            add(CustomAccessibilityAction("Pause workout", it))
        }
        onStop?.let {
            add(CustomAccessibilityAction("Stop workout", it))
        }
        onSkip?.let {
            add(CustomAccessibilityAction("Skip exercise", it))
        }
    }

    /**
     * Creates custom accessibility actions for goal management
     */
    fun goalActions(
        onEdit: (() -> Unit)? = null,
        onDelete: (() -> Unit)? = null,
        onUpdateProgress: (() -> Unit)? = null,
    ): List<CustomAccessibilityAction> = buildList {
        onEdit?.let {
            add(CustomAccessibilityAction("Edit goal", it))
        }
        onUpdateProgress?.let {
            add(CustomAccessibilityAction("Update progress", it))
        }
        onDelete?.let {
            add(CustomAccessibilityAction("Delete goal", it))
        }
    }
}

/**
 * Haptic feedback helpers for accessibility
 */
object AccessibilityFeedback {

    /**
     * Provides appropriate haptic feedback for different actions
     */
    enum class FeedbackType {
        SUCCESS, // Goal achieved, workout completed
        WARNING, // Low battery, missed goal
        ERROR, // Failed action, invalid input
        SELECTION, // Item selected, button pressed
        PROGRESS, // Progress milestone reached
    }

    /**
     * In a real implementation, this would trigger appropriate haptic feedback
     */
    fun provideFeedback(type: FeedbackType) {
        // Implementation would use platform-specific haptic feedback APIs
        // For now, this is a placeholder
    }
}
