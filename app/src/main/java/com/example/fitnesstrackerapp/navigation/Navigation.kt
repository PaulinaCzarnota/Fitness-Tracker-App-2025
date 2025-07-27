package com.example.fitnesstrackerapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fitnesstrackerapp.screens.*
import com.example.fitnesstrackerapp.ui.components.LoginScreen
import com.example.fitnesstrackerapp.ui.components.RegisterScreen

/**
 * Screen
 *
 * Sealed class used to define each screen's unique navigation route.
 * This ensures type safety and consistent routing across the app.
 */
sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
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
 * Entry point for app navigation. Initializes a NavHostController
 * and launches the full navigation graph via [AppNavigation].
 */
@Composable
fun Navigation() {
    val navController = rememberNavController()
    AppNavigation(navController = navController)
}

/**
 * AppNavigation
 *
 * Defines the navigation graph of the app using NavHost. Each screen
 * is registered using a unique route defined in [Screen].
 *
 * @param navController NavController passed to handle screen navigation.
 * @param modifier Optional [Modifier] to customize the NavHost container.
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = modifier
    ) {
        // --- Authentication Screens ---
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController = navController)
        }

        // --- Main Screens ---
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
