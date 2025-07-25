package com.example.fitnesstrackerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.fitnesstrackerapp.navigation.Navigation
import com.example.fitnesstrackerapp.ui.theme.FitnessTrackerAppTheme

/**
 * MainActivity
 *
 * The main entry point of the Fitness Tracker App.
 * This activity initializes the Jetpack Compose UI and sets up theming and navigation.
 */
class MainActivity : ComponentActivity() {

    /**
     * Called when the activity is first created.
     * Sets up the full Compose UI inside a Material3 Surface.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Compose root content
        setContent {
            // Apply the custom theme defined in ui.theme package
            FitnessTrackerAppTheme {
                // Surface provides a themed background for the UI
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Load the Navigation composable to manage app screens
                    Navigation()
                }
            }
        }
    }
}
