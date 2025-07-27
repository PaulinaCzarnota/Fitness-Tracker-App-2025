package com.example.fitnesstrackerapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fitnesstrackerapp.ui.components.BottomNavigationBar

/**
 * HomeScreen
 *
 * This is the main dashboard shown after login. It introduces the app and provides
 * a clean welcome layout with navigation instructions.
 *
 * @param navController NavController passed down for navigation handling.
 */
@Composable
fun HomeScreen(navController: NavController) {
    // Scaffold layout wraps the screen and includes the bottom navigation bar
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        // Main content column with responsive padding
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.Top),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App name / welcome message
            Text(
                text = "Welcome to FitTrack!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // App description / summary of features
            Text(
                text = "Track your workouts, set goals, and monitor your nutrition with ease.",
                style = MaterialTheme.typography.bodyLarge
            )

            // Call to action for new users
            Text(
                text = "Use the bottom navigation bar below to get started.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
