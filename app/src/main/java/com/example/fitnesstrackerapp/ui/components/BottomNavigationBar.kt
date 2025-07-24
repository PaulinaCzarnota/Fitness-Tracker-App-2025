package com.example.fitnesstrackerapp.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.fitnesstrackerapp.navigation.Screen

/**
 * Represents a single item in the bottom navigation bar.
 *
 * @property route Navigation route this item navigates to.
 * @property label Label text shown in the navigation bar.
 */
data class BottomNavItem(
    val route: String,
    val label: String
)

/**
 * BottomNavigationBar composable.
 *
 * Provides tabbed navigation without icons, using labels only.
 * Highlights the current tab based on NavController's state.
 *
 * @param navController NavController for navigation between screens.
 */
@Composable
fun BottomNavigationBar(navController: NavController) {
    // List of navigation items (labels only, no icons)
    val items = listOf(
        BottomNavItem(Screen.Home.route, "Home"),
        BottomNavItem(Screen.Workout.route, "Workout"),
        BottomNavItem(Screen.Diet.route, "Diet"),
        BottomNavItem(Screen.Goal.route, "Goals"),
        BottomNavItem(Screen.Progress.route, "Progress"),
        BottomNavItem(Screen.StepTracker.route, "Steps")
    )

    // Get current back stack entry to highlight selected tab
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Render the Material3 NavigationBar
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = {}, // No icon shown
                label = { Text(text = item.label) },
                selected = currentRoute == item.route,
                alwaysShowLabel = true,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            launchSingleTop = true
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}
