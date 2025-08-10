package com.example.fitnesstrackerapp.ui.main

/**
 * Main screen container for the authenticated user interface in the Fitness Tracker application.
 *
 * This Compose screen provides the core navigation structure with:
 * - Bottom navigation bar with five main sections (Home, Workout, Goals, Nutrition, Profile)
 * - Material 3 NavigationBar implementation for modern UI design
 * - State preservation during navigation transitions
 * - Proper back stack management and navigation handling
 * - Integration with authentication system for user session management
 *
 * The screen uses Jetpack Navigation Compose for declarative navigation
 * and implements proper navigation patterns for a smooth user experience.
 */

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fitnesstrackerapp.screens.GoalScreen
import com.example.fitnesstrackerapp.screens.HomeScreen
import com.example.fitnesstrackerapp.ui.nutrition.NutritionScreen
import com.example.fitnesstrackerapp.ui.profile.ProfileScreen
import com.example.fitnesstrackerapp.ui.workout.WorkoutScreen

/**
 * Navigation destinations for the main app flow.
 */
sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Workout : Screen("workout", "Workout", Icons.Default.FitnessCenter)
    object Goals : Screen("goals", "Goals", Icons.Default.Flag)
    object Nutrition : Screen("nutrition", "Nutrition", Icons.Default.Restaurant)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
}

/**
 * Main screen composable that provides the core navigation structure.
 *
 * @param modifier Modifier for styling the screen.
 * @param authViewModel The ViewModel for handling authentication logic.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    authViewModel: com.example.fitnesstrackerapp.ui.auth.AuthViewModel,
) {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Home,
        Screen.Workout,
        Screen.Goals,
        Screen.Nutrition,
        Screen.Profile,
    )

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                            )
                        },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Screen.Home.route) {
                HomeScreen(navController = navController, authViewModel = authViewModel)
            }
            composable(Screen.Workout.route) {
                val activity = LocalActivity.current as? androidx.activity.ComponentActivity
                if (activity != null) {
                    WorkoutScreen(authViewModel = authViewModel, activity = activity)
                } else {
                    // Fallback UI or error handling
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Unable to load workout screen",
                        )
                    }
                }
            }
            composable(Screen.Goals.route) {
                val activity = LocalActivity.current as? androidx.activity.ComponentActivity
                if (activity != null) {
                    GoalScreen(activity = activity)
                } else {
                    // Fallback UI or error handling
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Unable to load goals screen",
                        )
                    }
                }
            }
            composable(Screen.Nutrition.route) {
                NutritionScreen(authViewModel = authViewModel)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                )
            }
        }
    }
}
