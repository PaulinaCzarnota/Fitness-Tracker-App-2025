package com.example.fitnesstrackerapp.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.fitnesstrackerapp.navigation.Screen

/**
 * Data class representing each item in the bottom navigation bar.
 */
data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

/**
 * Bottom navigation bar composable showing navigation items and handling clicks.
 */
@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem(Screen.Home.route, Icons.Default.Home, "Home"),
        BottomNavItem(Screen.Workout.route, Icons.Default.FitnessCenter, "Workout"),
        BottomNavItem(Screen.Diet.route, Icons.Default.Restaurant, "Diet"),
        BottomNavItem(Screen.Goal.route, Icons.Default.Star, "Goals"),
        BottomNavItem(Screen.Progress.route, Icons.Default.ShowChart, "Progress"),
        BottomNavItem(Screen.StepTracker.route, Icons.Default.DirectionsWalk, "Steps")
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            // Avoid building up multiple copies of the same destination
                            launchSingleTop = true
                            // Pop up to the start destination to avoid building a large back stack
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}
