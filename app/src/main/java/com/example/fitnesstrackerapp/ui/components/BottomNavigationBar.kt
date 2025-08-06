/**
 * BottomNavigationBar
 *
 * Purpose:
 * - Displays a Material 3 navigation bar at the bottom of the screen in the FitnessTrackerApp
 * - Provides quick access to core screens using NavigationBarItem
 */

package com.example.fitnesstrackerapp.ui.components

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.fitnesstrackerapp.R
import com.example.fitnesstrackerapp.navigation.Screen

/**
 * Represents a single navigation tab shown in the bottom navigation bar.
 */
data class BottomNavItem(
    val route: String,
    val labelResId: Int
)

@Composable
fun BottomNavigationBar(navController: NavController) {
    // Define navigation tabs to show in the bottom nav
    val navItems = listOf(
        BottomNavItem(Screen.Home.route, R.string.home),
        BottomNavItem(Screen.Workout.route, R.string.workout),
        BottomNavItem(Screen.Nutrition.route, R.string.nutrition),
        BottomNavItem(Screen.Goal.route, R.string.goals),
        BottomNavItem(Screen.Progress.route, R.string.progress),
        BottomNavItem(Screen.StepTracker.route, R.string.steps)
    )

    // Get the currently active screen route
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Render bottom navigation bar
    NavigationBar {
        navItems.forEach { item ->
            NavigationBarItem(
                icon = {
                    // TODO: Add icons for the navigation items
                    // Icon(imageVector = item.icon, contentDescription = stringResource(item.labelResId))
                },
                label = { Text(text = stringResource(item.labelResId)) },
                selected = currentRoute == item.route,
                alwaysShowLabel = true,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            launchSingleTop = true           // Avoid multiple copies
                            restoreState = true              // Restore previous screen state
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true             // Save popped screen state
                            }
                        }
                    }
                }
            )
        }
    }
}
