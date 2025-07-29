package com.example.fitnesstrackerapp.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.fitnesstrackerapp.R
import com.example.fitnesstrackerapp.notifications.NotificationUtils
import com.example.fitnesstrackerapp.ui.components.BottomNavigationBar
import com.example.fitnesstrackerapp.viewmodel.UserViewModel

/**
 * HomeScreen
 *
 * Displays a welcome message and overview of features.
 * Uses ViewModel to retrieve logged-in user data.
 * Triggers a daily notification using NotificationUtils.
 *
 * @param navController Used to enable navigation and support BottomNavigationBar.
 */
@Composable
fun HomeScreen(navController: NavHostController) {
    val context = LocalContext.current

    // Initialize the ViewModel using Hilt injection
    val userViewModel: UserViewModel = hiltViewModel()

    // Observe user state using StateFlow + collectAsStateWithLifecycle
    val currentUser by userViewModel.currentUser.collectAsStateWithLifecycle(initialValue = null)

    // Schedule a daily notification when the screen is shown
    LaunchedEffect(Unit) {
        NotificationUtils.scheduleDailyReminder(context)
    }

    // App layout using Scaffold
    Scaffold(
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Display fitness icon (ensure R.drawable.ic_fitness exists)
            Image(
                painter = painterResource(id = R.drawable.ic_fitness),
                contentDescription = "Fitness Icon",
                modifier = Modifier.size(100.dp)
            )

            // Display user's email if available, else fallback welcome
            Text(
                text = currentUser?.email?.let { "Welcome, $it!" } ?: "Welcome to FitTrack!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            // Description text
            Text(
                text = "Track workouts, log meals, count steps, set goals,\nand monitor your progress—all in one place.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            // UI hint
            Text(
                text = "Use the bottom navigation bar to explore.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
