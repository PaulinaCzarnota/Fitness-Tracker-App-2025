package com.example.fitnesstrackerapp.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.fitnesstrackerapp.navigation.Screen

/**
 * BottomNavItem
 *
 * Represents a single tab item in the bottom navigation bar.
 *
 * @param route The destination route this tab navigates to.
 * @param label The text label shown under the tab.
 */
data class BottomNavItem(
    val route: String,
    val label: String
)

/**
 * BottomNavigationBar
 *
 * A composable that displays a Material3 bottom navigation bar with multiple tabs.
 * Handles navigation between key screens and highlights the selected screen.
 *
 * @param navController The app's NavController used to navigate between screens.
 */
@Composable
fun BottomNavigationBar(navController: NavController) {
    // Define the tab items shown in the bottom navigation bar
    val items = listOf(
        BottomNavItem(Screen.Home.route, "Home"),
        BottomNavItem(Screen.Workout.route, "Workout"),
        BottomNavItem(Screen.Diet.route, "Diet"),
        BottomNavItem(Screen.Goal.route, "Goals"),
        BottomNavItem(Screen.Progress.route, "Progress"),
        BottomNavItem(Screen.StepTracker.route, "Steps")
    )

    // Track the currently active route using the back stack
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Render the navigation bar with tabs
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                // Optional: You can add icons here if needed
                icon = { /* e.g. Icon(Icons.Default.Home, contentDescription = item.label) */ },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                alwaysShowLabel = true,
                onClick = {
                    // Navigate only if we're not already on the selected screen
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            launchSingleTop = true // Prevent duplicates
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
