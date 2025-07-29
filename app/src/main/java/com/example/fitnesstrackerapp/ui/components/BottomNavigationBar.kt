package com.example.fitnesstrackerapp.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.fitnesstrackerapp.R
import com.example.fitnesstrackerapp.navigation.Screen

/**
 * BottomNavItem
 *
 * Represents a single navigation tab shown in the bottom navigation bar.
 *
 * @property route The unique route string used for navigation.
 * @property labelResId The string resource ID for the label displayed to the user.
 * // You may add: val icon: ImageVector for future icon support.
 */
data class BottomNavItem(
    val route: String,
    val labelResId: Int
)

/**
 * BottomNavigationBar
 *
 * Displays a Material 3 navigation bar at the bottom of the screen.
 * Provides quick access to core screens using [NavigationBarItem].
 *
 * @param navController NavHostController instance for managing navigation state.
 */
@Composable
fun BottomNavigationBar(navController: NavHostController) {
    // Define navigation tabs to show in the bottom nav
    val navItems = listOf(
        BottomNavItem(Screen.Home.route, R.string.home),
        BottomNavItem(Screen.Workout.route, R.string.workout),
        BottomNavItem(Screen.Diet.route, R.string.diet),
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
                    // Placeholder for future icons
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
