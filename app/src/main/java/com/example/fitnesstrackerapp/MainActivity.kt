package com.example.fitnesstrackerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.fitnesstrackerapp.navigation.Navigation
import com.example.fitnesstrackerapp.ui.theme.FitnessTrackerAppTheme

/**
 * MainActivity
 *
 * The root entry point for the Fitness Tracker App.
 * It applies the custom Material3 theme and hosts the navigation system.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the root Compose UI content
        setContent {
            // Wrap app content with the theme
            FitnessTrackerAppTheme {
                // Surface provides the base container with background color
                Surface(
                    modifier = Modifier.fillMaxSize() // Fills full screen
                ) {
                    // Load and display the app's navigation graph
                    Navigation()
                }
            }
        }
    }
}
