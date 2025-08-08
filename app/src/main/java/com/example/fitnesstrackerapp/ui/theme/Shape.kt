/**
 * Shape Definitions
 *
 * Responsibilities:
 * - Defines corner shapes for UI components
 * - Implements Material 3 shape scale
 * - Ensures consistent component appearance
 */

package com.example.fitnesstrackerapp.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)
