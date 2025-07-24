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
 * This is the landing screen of the Fitness Tracker App (FitTrack).
 * It briefly introduces the app and directs users to other features using the bottom navigation bar.
 *
 * @param navController NavController for navigating between app screens.
 */
@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) } // Reusable bottom nav bar
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Avoids overlap with system UI and nav bar
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.Top),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main app welcome title
            Text(
                text = "Welcome to FitTrack!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // Subtitle with app purpose
            Text(
                text = "Track your workouts, goals, and nutrition progress with ease.",
                style = MaterialTheme.typography.bodyLarge
            )

            // Navigation instructions
            Text(
                text = "Use the bottom navigation bar to get started.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
