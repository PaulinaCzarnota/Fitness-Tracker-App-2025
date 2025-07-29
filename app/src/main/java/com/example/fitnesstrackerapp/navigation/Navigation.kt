package com.example.fitnesstrackerapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fitnesstrackerapp.screens.DietScreen
import com.example.fitnesstrackerapp.screens.GoalScreen
import com.example.fitnesstrackerapp.screens.HomeScreen
import com.example.fitnesstrackerapp.screens.ProgressScreen
import com.example.fitnesstrackerapp.screens.StepTrackerScreen
import com.example.fitnesstrackerapp.screens.WorkoutScreen
import com.example.fitnesstrackerapp.ui.components.LoginScreen
import com.example.fitnesstrackerapp.ui.components.RegisterScreen

/**
 * Screen
 *
 * A sealed class representing all the navigation routes in the app.
 * This provides compile-time safety and avoids string typos in navigation.
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
 * Top-level composable that creates a [NavHostController] and
 * starts the navigation graph using [AppNavigation].
 */
@Composable
fun Navigation() {
    val navController = rememberNavController()
    AppNavigation(navController = navController)
}

/**
 * AppNavigation
 *
 * Composable that defines the app's full navigation graph using
 * Jetpack Navigation Compose. Associates routes with screen composables.
 *
 * @param navController Controller used to manage back stack and navigation actions.
 * @param modifier Optional [Modifier] for wrapping the NavHost.
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
        // Authentication
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController = navController)
        }

        // Main app screens
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Workout.route) {
            WorkoutScreen(navController = navController)
        }
        composable(Screen.Diet.route) {
            DietScreen(navController = navController)
        }
        composable(Screen.Goal.route) {
            GoalScreen(navController = navController)
        }
        composable(Screen.Progress.route) {
            ProgressScreen(navController = navController)
        }
        composable(Screen.StepTracker.route) {
            StepTrackerScreen(navController = navController)
        }
    }
}
