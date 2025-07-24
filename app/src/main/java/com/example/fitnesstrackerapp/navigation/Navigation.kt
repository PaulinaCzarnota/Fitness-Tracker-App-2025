package com.example.fitnesstrackerapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fitnesstrackerapp.screens.*

/**
 * Sealed class defining all screen routes in the app.
 * Using `data object` (Kotlin 2.0+) ensures each screen is a singleton with stable identity.
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Workout : Screen("workout")
    data object Diet : Screen("diet")
    data object Goal : Screen("goal")
    data object Progress : Screen("progress")
    data object StepTracker : Screen("step_tracker")
}

/**
 * Navigation entry point.
 * Call this from MainActivity to start navigation using NavController.
 */
@Composable
fun Navigation() {
    val navController = rememberNavController()
    AppNavigation(navController = navController)
}

/**
 * Composable that defines the navigation graph.
 *
 * @param navController Used to navigate between screens.
 * @param modifier Optional modifier for styling.
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
        // Navigation destinations
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
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
