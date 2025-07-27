package com.example.fitnesstrackerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.fitnesstrackerapp.navigation.Navigation
import com.example.fitnesstrackerapp.notifications.NotificationUtils
import com.example.fitnesstrackerapp.ui.theme.FitnessTrackerAppTheme

/**
 * MainActivity
 *
 * The entry point of the Fitness Tracker App.
 * This activity uses Jetpack Compose for the UI,
 * applies the app's custom Material 3 theme,
 * and triggers a daily reminder notification setup.
 */
class MainActivity : ComponentActivity() {

    /**
     * Called when the activity is created.
     * This sets up the entire UI using Jetpack Compose and configures background behavior.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Schedule daily notification for reminders (e.g., workout or goal check)
        NotificationUtils.scheduleDailyReminder(applicationContext)

        // Launch the Jetpack Compose UI content
        setContent {
            // Apply the custom Material 3 theme defined in the theme package
            FitnessTrackerAppTheme {
                // Surface acts as a full-screen background container
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Start the navigation system from Navigation.kt
                    Navigation()
                }
            }
        }
    }
}
