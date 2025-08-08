/**
 * Navigation controller and routing configuration for the Fitness Tracker application.
 *
 * This file manages the complete navigation flow between:
 * - Authentication screens (login, signup)
 * - Main application screens (home, workouts, goals, progress, steps, nutrition)
 * - Profile and settings screens
 *
 * Uses Jetpack Compose Navigation for modern declarative navigation with proper
 * state management and deep linking support.
 */

package com.example.fitnesstrackerapp.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fitnesstrackerapp.screens.GoalScreen
import com.example.fitnesstrackerapp.screens.HomeScreen
import com.example.fitnesstrackerapp.screens.SignUpScreen
import com.example.fitnesstrackerapp.screens.StepTrackerScreen
import com.example.fitnesstrackerapp.ui.auth.AuthViewModel
import com.example.fitnesstrackerapp.ui.auth.LoginScreen

/**
 * Sealed class representing navigation destinations in the app.
 */
sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Workouts : Screen("workouts", "Workouts", Icons.Default.FitnessCenter)
    object Nutrition : Screen("nutrition", "Nutrition", Icons.Default.LocalDining)
    object Goals : Screen("goals", "Goals", Icons.Filled.Star)
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
    }
}

/**
 * Main application navigation graph with bottom navigation.
 *
 * @param navController Navigation controller for main app flow
 * @param authViewModel Authentication ViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainNavigationGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomNavItems = listOf(
        Screen.Home,
        Screen.Workouts,
        Screen.Nutrition,
        Screen.Goals,
        Screen.Steps,
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                            )
                        },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any {
                            it.route == screen.route
                        } == true,
                        onClick = {
                            bottomNavController.navigate(screen.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            }
        },
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
                )
            }

            composable(Screen.Workouts.route) {
                com.example.fitnesstrackerapp.ui.workout.WorkoutScreen(
                    modifier = Modifier.fillMaxSize(),
                    authViewModel = authViewModel,
                    activity = androidx.compose.ui.platform.LocalContext.current as androidx.activity.ComponentActivity,
                )
            }

            composable(Screen.Nutrition.route) {
                com.example.fitnesstrackerapp.ui.nutrition.NutritionScreen(
                    modifier = Modifier.fillMaxSize(),
                    authViewModel = authViewModel,
                )
            }

            composable(Screen.Goals.route) {
                GoalScreen(
                    modifier = Modifier.fillMaxSize(),
                    activity = androidx.compose.ui.platform.LocalContext.current as androidx.activity.ComponentActivity,
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
                    onLogout = {
                        authViewModel.logout()
                    },
                )
            }
        }
    }
}

/**
 * Profile screen composable for user settings and logout.
 *
 * @param navController Navigation controller
 * @param authViewModel Authentication ViewModel
 * @param onLogout Callback for logout action
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    onLogout: () -> Unit,
) {
    val authUiState by authViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
            )
        },
    ) { innerPadding ->
        // Profile screen implementation would go here
        // For now, this is a placeholder
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Welcome, ${authUiState.user?.email ?: "User"}!",
                style = MaterialTheme.typography.headlineMedium,
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text("Logout")
            }
        }
    }
}
