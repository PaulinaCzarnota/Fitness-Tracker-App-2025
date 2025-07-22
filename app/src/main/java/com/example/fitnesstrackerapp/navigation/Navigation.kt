package com.example.fitnesstrackerapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.fitnesstrackerapp.screens.*

/**
 * Sealed class defining app screens and their navigation routes.
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Workout : Screen("workout")
    object Diet : Screen("diet")
    object Goal : Screen("goal")           // singular to match GoalScreen.kt
    object Progress : Screen("progress")
    object StepTracker : Screen("step_tracker")  // added step tracker screen
}

/**
 * Navigation graph connecting routes to composable screens.
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        composable(Screen.Workout.route) {
            WorkoutScreen()
        }
        composable(Screen.Diet.route) {
            DietScreen()
        }
        composable(Screen.Goal.route) {
            GoalScreen()
        }
        composable(Screen.Progress.route) {
            ProgressScreen()
        }
        composable(Screen.StepTracker.route) {
            StepTrackerScreen()
        }
    }
}
