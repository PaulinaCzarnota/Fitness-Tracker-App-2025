/**
 * Enhanced Material 3 Shapes for the Fitness Tracker App
 *
 * Provides a comprehensive set of shapes optimized for fitness/health apps:
 * - Rounded corners for modern, friendly appearance
 * - Consistent radius values following Material Design guidelines
 * - Special shapes for cards, buttons, and interactive elements
 */

package com.example.fitnesstrackerapp.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    // Extra small components (chips, small buttons)
    extraSmall = RoundedCornerShape(4.dp),

    // Small components (small cards, filled buttons)
    small = RoundedCornerShape(8.dp),

    // Medium components (cards, outlined buttons)
    medium = RoundedCornerShape(12.dp),

    // Large components (navigation drawer, large cards)
    large = RoundedCornerShape(16.dp),

    // Extra large components (dialogs, bottom sheets)
    extraLarge = RoundedCornerShape(28.dp),
)

/**
 * Additional custom shapes for specific fitness app components
 */
object FitnessShapes {
    // Card shapes for different elevations
    val CardSmall = RoundedCornerShape(8.dp)
    val CardMedium = RoundedCornerShape(12.dp)
    val CardLarge = RoundedCornerShape(16.dp)

    // Button shapes
    val ButtonSmall = RoundedCornerShape(8.dp)
    val ButtonMedium = RoundedCornerShape(12.dp)
    val ButtonLarge = RoundedCornerShape(16.dp)
    val ButtonPill = RoundedCornerShape(50) // Fully rounded

    // Progress and chart shapes
    val ProgressBar = RoundedCornerShape(8.dp)
    val ChartContainer = RoundedCornerShape(16.dp)

    // Dialog and modal shapes
    val Dialog = RoundedCornerShape(24.dp)
    val BottomSheet = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)

    // Navigation shapes
    val NavigationItem = RoundedCornerShape(12.dp)
    val NavigationRail = RoundedCornerShape(16.dp)

    // Input field shapes
    val TextField = RoundedCornerShape(8.dp)
    val SearchBar = RoundedCornerShape(28.dp)

    // Achievement and badge shapes
    val Badge = RoundedCornerShape(50) // Fully rounded
    val Achievement = RoundedCornerShape(16.dp)
}
