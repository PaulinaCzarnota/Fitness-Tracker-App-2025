package com.example.fitnesstrackerapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * This file defines the overall app theme using Material Design 3.
 * Supports:
 * - Light and dark modes
 * - Dynamic Material You colors on Android 12+
 * - Custom color palettes (see Color.kt)
 * - Custom typography (see Typography.kt)
 */

// ----------------------------
// DARK THEME COLOR SCHEME
// ----------------------------
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,        // Primary color for dark theme
    secondary = PurpleGrey80,  // Secondary/accent color for dark theme
    tertiary = Pink80,         // Tertiary/accent color for dark theme

    // Optional overrides (if you need advanced color tuning)
    // background = Color(0xFF121212),
    // surface = Color(0xFF1E1E1E),
    // onPrimary = Color.Black,
    // onSecondary = Color.Black,
    // onTertiary = Color.Black
)

// ----------------------------
// LIGHT THEME COLOR SCHEME
// ----------------------------
private val LightColorScheme = lightColorScheme(
    primary = Purple40,        // Primary color for light theme
    secondary = PurpleGrey40,  // Secondary/accent color for light theme
    tertiary = Pink40,         // Tertiary/accent color for light theme

    // Optional overrides (uncomment to customize deeply)
    // background = Color.White,
    // surface = Color(0xFFFFFBFE),
    // onPrimary = Color.White,
    // onSecondary = Color.Black,
    // onTertiary = Color.White
)

/**
 * Wraps your app’s UI in a consistent Material 3 theme.
 *
 * @param darkTheme Enables dark mode. Defaults to system setting.
 * @param dynamicColor Enables Material You dynamic colors (only on Android 12+).
 * @param content The UI content wrapped inside the themed MaterialTheme.
 */
@Composable
fun FitnessTrackerAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    // Resolve the appropriate color scheme
    val colorScheme = when {
        // Use dynamic color if supported (Android 12+)
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        // Otherwise, use custom dark or light theme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Apply the Material 3 theme to the app
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Imported from Typography.kt
        content = content         // Composable content wrapped in theme
    )
}
