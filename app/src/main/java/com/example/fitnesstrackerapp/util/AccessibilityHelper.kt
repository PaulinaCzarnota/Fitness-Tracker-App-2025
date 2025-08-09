package com.example.fitnesstrackerapp.util

import android.annotation.SuppressLint
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import kotlin.math.pow

/**
 * Helper class for implementing and managing accessibility features.
 *
 * Ensures the app is accessible to all users by providing:
 * - Content descriptions for UI elements
 * - TalkBack support
 * - Proper navigation order
 * - Color contrast compliance
 * - Touch target size requirements
 */
object AccessibilityHelper {
    // Minimum touch target size (48dp x 48dp)
    const val MIN_TOUCH_TARGET_SIZE_DP = 48

    // Minimum color contrast ratio (4.5:1 for normal text, 3:1 for large text)
    const val MIN_CONTRAST_RATIO_NORMAL = 4.5f
    const val MIN_CONTRAST_RATIO_LARGE = 3.0f

    // Custom semantics properties for Compose
    val WorkoutTypeKey = SemanticsPropertyKey<String>("WorkoutType")
    val GoalProgressKey = SemanticsPropertyKey<Float>("GoalProgress")
    val StepCountKey = SemanticsPropertyKey<Int>("StepCount")

    /**
     * Extension function to add workout type semantics to a component.
     */
    fun SemanticsPropertyReceiver.workoutType(type: String) {
        this[WorkoutTypeKey] = type
    }

    /**
     * Extension function to add goal progress semantics to a component.
     */
    fun SemanticsPropertyReceiver.goalProgress(progress: Float) {
        this[GoalProgressKey] = progress
    }

    /**
     * Extension function to add step count semantics to a component.
     */
    fun SemanticsPropertyReceiver.stepCount(count: Int) {
        this[StepCountKey] = count
    }

    /**
     * Generates content descriptions for workout items.
     */
    @SuppressLint("DefaultLocale")
    fun getWorkoutDescription(
        type: String,
        duration: Int,
        calories: Int,
        distance: Float? = null,
    ): String {
        val base = "$type workout, duration $duration minutes, burned $calories calories"
        return if (distance != null) {
            "$base, distance ${String.format("%.1f", distance)} kilometers"
        } else {
            base
        }
    }

    /**
     * Generates content descriptions for goal progress.
     */
    fun getGoalProgressDescription(
        title: String,
        current: Float,
        target: Float,
        unit: String,
    ): String {
        val percentage = (current / target * 100).toInt()
        return "$title: $current out of $target $unit, $percentage% complete"
    }

    /**
     * Generates content descriptions for step counting.
     */
    fun getStepCountDescription(
        steps: Int,
        goal: Int,
    ): String {
        val percentage = (steps.toFloat() / goal * 100).toInt()
        return "$steps steps taken out of $goal goal, $percentage% complete"
    }

    /**
     * Sets proper content descriptions for navigation actions.
     */
    fun getNavigationDescription(
        destination: String,
        hasActiveWorkout: Boolean = false,
        unreadNotifications: Int = 0,
    ): String {
        val base = "Navigate to $destination"
        val additionalInfo = mutableListOf<String>()

        if (hasActiveWorkout) {
            additionalInfo.add("active workout in progress")
        }
        if (unreadNotifications > 0) {
            additionalInfo.add("$unreadNotifications unread notifications")
        }

        return if (additionalInfo.isEmpty()) {
            base
        } else {
            "$base, ${additionalInfo.joinToString(", ")}"
        }
    }

    /**
     * Validates color contrast ratio between two colors.
     */
    fun isColorContrastValid(
        foregroundColor: Int,
        backgroundColor: Int,
        isLargeText: Boolean = false,
    ): Boolean {
        val ratio = calculateContrastRatio(foregroundColor, backgroundColor)
        val minRatio = if (isLargeText) MIN_CONTRAST_RATIO_LARGE else MIN_CONTRAST_RATIO_NORMAL
        return ratio >= minRatio
    }

    /**
     * Calculates the contrast ratio between two colors.
     */
    private fun calculateContrastRatio(color1: Int, color2: Int): Float {
        val luminance1 = calculateRelativeLuminance(color1)
        val luminance2 = calculateRelativeLuminance(color2)
        val lighter = maxOf(luminance1, luminance2)
        val darker = minOf(luminance1, luminance2)
        return (lighter + 0.05f) / (darker + 0.05f)
    }

    /**
     * Calculates the relative luminance of a color.
     */
    private fun calculateRelativeLuminance(color: Int): Float {
        val red = ((color shr 16) and 0xff) / 255f
        val green = ((color shr 8) and 0xff) / 255f
        val blue = (color and 0xff) / 255f

        val r = if (red <= 0.03928f) red / 12.92f else ((red + 0.055f) / 1.055f).pow(2.4f)
        val g = if (green <= 0.03928f) green / 12.92f else ((green + 0.055f) / 1.055f).pow(2.4f)
        val b = if (blue <= 0.03928f) blue / 12.92f else ((blue + 0.055f) / 1.055f).pow(2.4f)

        return 0.2126f * r + 0.7152f * g + 0.0722f * b
    }
}
