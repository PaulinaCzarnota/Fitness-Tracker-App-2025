package com.example.fitnesstrackerapp.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.fitnesstrackerapp.navigation.Screen

/**
 * Data class representing a bottom navigation item.
 *
 * @param route The navigation route associated with this tab.
 * @param label The text label displayed in the bottom nav.
 */
data class BottomNavItem(
    val route: String,
    val label: String
)

/**
 * BottomNavigationBar
 *
 * A composable function that displays a Material 3 bottom navigation bar.
 * It allows switching between major app screens like Home, Workout, Diet, etc.
 * Highlights the currently selected tab and uses text-only items for simplicity.
 *
 * @param navController NavController used for navigating between screens.
 */
@Composable
fun BottomNavigationBar(navController: NavController) {
    // Define all screens as navigation items
    val items = listOf(
        BottomNavItem(Screen.Home.route, "Home"),
        BottomNavItem(Screen.Workout.route, "Workout"),
        BottomNavItem(Screen.Diet.route, "Diet"),
        BottomNavItem(Screen.Goal.route, "Goals"),
        BottomNavItem(Screen.Progress.route, "Progress"),
        BottomNavItem(Screen.StepTracker.route, "Steps")
    )

    // Get the current route from the navigation back stack
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Material3 bottom navigation bar
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = {}, // Optional: could add icons later
                label = { Text(text = item.label) },
                selected = currentRoute == item.route,
                alwaysShowLabel = true,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Remove previous instances up to the start destination
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Restore scroll position or input state
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}
