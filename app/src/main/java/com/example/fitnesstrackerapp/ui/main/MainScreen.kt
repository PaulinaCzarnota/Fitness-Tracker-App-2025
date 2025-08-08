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
import com.example.fitnesstrackerapp.screens.GoalScreen
import com.example.fitnesstrackerapp.screens.HomeScreen
import com.example.fitnesstrackerapp.ui.nutrition.NutritionScreen
import com.example.fitnesstrackerapp.ui.profile.ProfileScreen
import com.example.fitnesstrackerapp.ui.workout.WorkoutScreen
import androidx.compose.ui.platform.LocalContext
import androidx.activity.ComponentActivity

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
    authViewModel: com.example.fitnesstrackerapp.ui.auth.AuthViewModel
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
                HomeScreen(navController = navController, authViewModel = authViewModel)
            }
            composable(Screen.Workout.route) {
                val activity = LocalContext.current as ComponentActivity
                WorkoutScreen(authViewModel = authViewModel, activity = activity)
            }
            composable(Screen.Goals.route) {
                val activity = LocalContext.current as ComponentActivity
                GoalScreen(activity = activity)
            }
            composable(Screen.Nutrition.route) {
                NutritionScreen(authViewModel = authViewModel)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    navController = navController,
                    authViewModel = authViewModel
                )
            }
        }
    }
}
