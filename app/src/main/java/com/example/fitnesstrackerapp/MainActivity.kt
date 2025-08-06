/**
 * Main Activity for the Fitness Tracker Application
 *
 * Responsibilities:
 * - Entry point for the application
 * - Handles permission requests for location, activity recognition, and notifications
 * - Sets up navigation and UI structure
 * - Initializes background tracking services
 * - Manages splash screen and app initialization
 */

package com.example.fitnesstrackerapp

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnesstrackerapp.ui.auth.AuthViewModel
import com.example.fitnesstrackerapp.ui.auth.LoginScreen
import com.example.fitnesstrackerapp.ui.main.MainScreen
import com.example.fitnesstrackerapp.ui.theme.FitnessTrackerTheme

/**
 * Main Activity class that serves as the entry point for the application.
 * Handles permission requests, app initialization, and navigation setup.
 */
class MainActivity : ComponentActivity() {

    // Required permissions for the fitness tracking functionality
    private val requiredPermissions = mutableListOf<String>().apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            add(Manifest.permission.ACTIVITY_RECOGNITION)
            add(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Permission launcher for requesting multiple permissions
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            // Initialize background services after permissions are granted
            initializeBackgroundServices()
        }
    }

    /**
     * Initialize the activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before calling super.onCreate()
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Request required permissions
        requestPermissions()

        // Set up the UI content
        setContent {
            FitnessTrackerTheme {
                FitnessTrackerApp()
            }
        }
    }

    /**
     * Main composable function that handles navigation between auth and main screens
     */
    @Composable
    private fun FitnessTrackerApp() {
        val authViewModel: AuthViewModel = viewModel()
        val isAuthenticated by authViewModel.isAuthenticated.collectAsState()

        if (isAuthenticated) {
            MainScreen()
        } else {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    // Navigation will be handled by the ViewModel state change
                }
            )
        }
    }

    /**
     * Requests the necessary permissions for app functionality.
     */
    private fun requestPermissions() {
        if (requiredPermissions.isNotEmpty()) {
            permissionLauncher.launch(requiredPermissions.toTypedArray())
        } else {
            initializeBackgroundServices()
        }
    }

    /**
     * Initializes background services for step tracking and notifications
     */
    private fun initializeBackgroundServices() {
        // Initialize step tracking and other background services
        // This will be implemented when the services are created
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up resources if needed
    }
}
