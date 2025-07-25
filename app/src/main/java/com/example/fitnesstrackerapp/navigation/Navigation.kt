package com.example.fitnesstrackerapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fitnesstrackerapp.screens.*

/**
 * Sealed class defining all navigation routes in the app.
 * Each object represents a distinct screen.
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
 * Navigation
 *
 * Root navigation host initializer. Called in MainActivity.
 * It remembers a NavController and sets up the app's screen navigation.
 */
@Composable
fun Navigation() {
    val navController = rememberNavController()
    AppNavigation(navController = navController)
}

/**
 * AppNavigation
 *
 * Maps navigation routes to their corresponding composable screens.
 * Ensures smooth transitions between major app sections.
 *
 * @param navController Controller that manages back stack and navigation state.
 * @param modifier Optional modifier for layout customization.
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
        // Each route is mapped to its corresponding screen Composable
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
