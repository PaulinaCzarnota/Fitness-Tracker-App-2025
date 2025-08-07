/**
 * Main Screen Composable for Fitness Tracker Application
 *
 * Responsibilities:
 * - Provides the main navigation structure with bottom navigation
 * - Manages navigation between core app features
 * - Handles authentication state and user session
 * - Implements Material 3 design with proper navigation
 */

package com.example.fitnesstrackerapp.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fitnesstrackerapp.ui.goal.GoalsScreen
import com.example.fitnesstrackerapp.ui.nutrition.NutritionScreen
import com.example.fitnesstrackerapp.ui.profile.ProfileScreen
import com.example.fitnesstrackerapp.ui.workout.WorkoutScreen
import com.example.fitnesstrackerapp.screens.HomeScreen

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
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Home,
        Screen.Workout,
        Screen.Goals,
        Screen.Nutrition,
        Screen.Profile
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
                                contentDescription = screen.title
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
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(navController = navController)
            }
            composable(Screen.Workout.route) {
                WorkoutScreen(navController = navController)
            }
            composable(Screen.Goals.route) {
                GoalsScreen(navController = navController)
            }
            composable(Screen.Nutrition.route) {
                NutritionScreen(navController = navController)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(navController = navController)
            }
        }
    }
}
