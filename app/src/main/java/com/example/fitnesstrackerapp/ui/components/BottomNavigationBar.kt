/**
 * BottomNavigationBar
 *
 * Purpose:
 * - Displays a Material 3 navigation bar at the bottom of the screen in the FitnessTrackerApp
 * - Provides quick access to core screens using NavigationBarItem
 */

package com.example.fitnesstrackerapp.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

@Composable
fun BottomNavigationBar(
    navController: NavController,
) {
    val items = listOf(
        BottomNavItem("home", "Home", Icons.Default.Home),
        BottomNavItem("workout", "Workout", Icons.AutoMirrored.Filled.DirectionsRun),
        BottomNavItem("goals", "Goals", Icons.Default.Flag),
        BottomNavItem("progress", "Progress", Icons.AutoMirrored.Filled.ShowChart),
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                },
            )
        }
    }
}
