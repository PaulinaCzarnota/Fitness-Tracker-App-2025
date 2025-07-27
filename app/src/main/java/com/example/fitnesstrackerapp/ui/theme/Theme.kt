package com.example.fitnesstrackerapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * -----------------------------------
 * Material 3 Theme for the App
 * -----------------------------------
 * This theme applies consistent styling using Material Design 3 and supports:
 * - Light and Dark mode
 * - Optional Material You (dynamic color) on Android 12+
 * - Custom colors from Color.kt
 * - Custom typography from Typography.kt
 */

// ----------------------------
// DARK THEME COLOR SCHEME
// ----------------------------
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,        // Main brand color in dark mode
    secondary = PurpleGrey80,  // Secondary brand/accent color
    tertiary = Pink80          // Tertiary/accent color
)

// ----------------------------
// LIGHT THEME COLOR SCHEME
// ----------------------------
private val LightColorScheme = lightColorScheme(
    primary = Purple40,        // Main brand color in light mode
    secondary = PurpleGrey40,  // Secondary brand/accent color
    tertiary = Pink40          // Tertiary/accent color

    // Optional overrides for advanced customization:
    // background = Color.White,
    // surface = Color(0xFFFFFBFE),
    // onPrimary = Color.White,
    // onSecondary = Color.Black,
    // onTertiary = Color.White,
    // onBackground = Color.Black,
    // onSurface = Color.Black,
)

/**
 * FitnessTrackerAppTheme
 *
 * Central theming composable that wraps your app UI.
 * Applies Material3 color schemes and typography to all content.
 *
 * @param darkTheme Whether to use dark mode (defaults to system setting)
 * @param dynamicColor Whether to enable Material You dynamic color (Android 12+)
 * @param content The app's Composable UI
 */
@Composable
fun FitnessTrackerAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    // Choose appropriate color scheme
    val colorScheme = when {
        // Android 12+ dynamic colors from system wallpaper
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        // Fallback to static palettes
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Apply the theme using MaterialTheme from Material3
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Defined in Typography.kt
        content = content         // Your app's UI
    )
}
