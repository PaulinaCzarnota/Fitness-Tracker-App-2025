package com.example.fitnesstrackerapp

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.fitnesstrackerapp.navigation.Navigation
import com.example.fitnesstrackerapp.notifications.NotificationUtils
import com.example.fitnesstrackerapp.ui.theme.FitnessTrackerAppTheme

/**
 * MainActivity
 *
 * The entry point of the Fitness Tracker App.
 * Sets up the Compose UI, requests runtime permissions (if needed),
 * and schedules notifications via [NotificationUtils].
 */
class MainActivity : ComponentActivity() {

    /**
     * Called when the activity is first created.
     * Initializes permission requests and sets the app content using Jetpack Compose.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationsPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // Schedule a repeating daily notification to remind the user
        NotificationUtils.scheduleDailyReminder(applicationContext)

        // Set the UI content of the app using a custom Material 3 theme
        setContent {
            FitnessTrackerAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppRoot()
                }
            }
        }
    }

    /**
     * A permission launcher for notification access on Android 13+.
     * Displays a warning toast if permission is denied.
     */
    private val requestNotificationsPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(
                this,
                "Notification permission denied. Daily reminders may not work.",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}

/**
 * AppRoot
 *
 * Top-level Composable that hosts the app’s full navigation graph.
 * This function is passed to [setContent] in [MainActivity].
 */
@Composable
fun AppRoot() {
    Navigation()
}
