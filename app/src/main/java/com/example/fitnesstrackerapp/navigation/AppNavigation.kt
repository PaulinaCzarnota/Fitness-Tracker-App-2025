/**
 * Navigation controller and routing configuration for the Fitness Tracker application.
 *
 * This file manages the complete navigation flow between:
 * - Authentication screens (login, signup)
 * - Main application screens (home, workouts, goals, progress, steps, nutrition)
 * - Profile and settings screens
 *
 * Uses Jetpack Compose Navigation with proper ViewModel scoping via ServiceLocator
 * for state management and dependency injection without external libraries.
 */

package com.example.fitnesstrackerapp.navigation

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fitnesstrackerapp.screens.ForgotPasswordScreen
import com.example.fitnesstrackerapp.screens.GoalScreen
import com.example.fitnesstrackerapp.screens.HomeScreen
import com.example.fitnesstrackerapp.screens.ProgressScreen
import com.example.fitnesstrackerapp.screens.SignUpScreen
import com.example.fitnesstrackerapp.screens.StepTrackerScreen
import com.example.fitnesstrackerapp.ui.auth.AuthViewModel
import com.example.fitnesstrackerapp.ui.auth.LoginScreen
import com.example.fitnesstrackerapp.ui.navigation.AdaptiveNavigation
import com.example.fitnesstrackerapp.ui.navigation.getWindowSizeClass
import com.example.fitnesstrackerapp.ui.nutrition.NutritionScreen
import com.example.fitnesstrackerapp.ui.profile.ProfileScreen
import com.example.fitnesstrackerapp.ui.workout.WorkoutScreen

/**
 * Sealed class representing navigation destinations in the app.
 */
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Workouts : Screen("workouts", "Workouts", Icons.Default.FitnessCenter)
    object Progress : Screen("progress", "Progress", Icons.Default.Timeline)
    object Goals : Screen("goals", "Goals", Icons.Filled.Star)
    object Nutrition : Screen("nutrition", "Nutrition", Icons.Default.LocalDining)
    object Steps : Screen("steps", "Steps", Icons.AutoMirrored.Outlined.DirectionsRun)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person)
}

/**
 * Main navigation composable that handles the entire app navigation flow.
 *
 * @param authViewModel The ViewModel for handling authentication logic
 */
@Composable
fun AppNavigation(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val authUiState by authViewModel.uiState.collectAsState()

    if (authUiState.isAuthenticated) {
        MainNavigationGraph(
            navController = navController,
            authViewModel = authViewModel,
        )
    } else {
        AuthNavigationGraph(
            navController = navController,
            authViewModel = authViewModel,
        )
    }
}

/**
 * Authentication navigation graph for login and signup flows.
 *
 * @param navController Navigation controller for auth flow
 * @param authViewModel Authentication ViewModel
 */
@Composable
private fun AuthNavigationGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
) {
    NavHost(
        navController = navController,
        startDestination = "login",
    ) {
        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    // Navigation is handled by the parent composable watching authUiState
                },
                onNavigateToSignUp = {
                    navController.navigate("signup") {
                        launchSingleTop = true
                    }
                },
                onNavigateToForgotPassword = {
                    navController.navigate("forgot_password") {
                        launchSingleTop = true
                    }
                },
            )
        }

        composable("signup") {
            SignUpScreen(
                navController = navController,
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                viewModel = authViewModel,
            )
        }

        composable("forgot_password") {
            ForgotPasswordScreen(
                navController = navController,
                onNavigateToLogin = {
                    navController.popBackStack("login", inclusive = false)
                },
                viewModel = authViewModel,
            )
        }
    }
}

/**
 * Main application navigation graph with adaptive navigation.
 *
 * @param navController Navigation controller for main app flow
 * @param authViewModel Authentication ViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainNavigationGraph(
    @Suppress("UNUSED_PARAMETER") navController: NavHostController,
    authViewModel: AuthViewModel,
) {
    val bottomNavController = rememberNavController()
    val windowSize = getWindowSizeClass()

    AdaptiveNavigation(
        navController = bottomNavController,
        windowSize = windowSize,
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    navController = bottomNavController,
                    authViewModel = authViewModel,
                    windowSize = windowSize,
                )
            }

            composable(Screen.Workouts.route) {
                WorkoutScreen(
                    modifier = Modifier.fillMaxSize(),
                    authViewModel = authViewModel,
                    activity = LocalContext.current as ComponentActivity,
                )
            }

            composable(Screen.Progress.route) {
                ProgressScreen(
                    modifier = Modifier.fillMaxSize(),
                    authViewModel = authViewModel,
                )
            }

            composable(Screen.Nutrition.route) {
                NutritionScreen(
                    modifier = Modifier.fillMaxSize(),
                    authViewModel = authViewModel,
                )
            }

            composable(Screen.Goals.route) {
                GoalScreen(
                    modifier = Modifier.fillMaxSize(),
                    activity = LocalContext.current as ComponentActivity,
                )
            }

            composable(Screen.Steps.route) {
                StepTrackerScreen(
                    navController = bottomNavController,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    navController = bottomNavController,
                    authViewModel = authViewModel,
                )
            }
        }
    }
}
