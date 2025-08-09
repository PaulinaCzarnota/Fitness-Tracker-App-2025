/**
 * Adaptive Navigation Component for Fitness Tracker App
 *
 * Features:
 * - Responsive navigation that adapts to screen size
 * - Bottom navigation for compact screens
 * - Navigation rail for medium screens
 * - Navigation drawer for expanded screens
 * - Material 3 design with smooth animations
 * - Accessibility support with proper semantics
 */

package com.example.fitnesstrackerapp.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.fitnesstrackerapp.ui.theme.FitnessShapes

/**
 * Navigation destinations for the fitness app
 */
sealed class FitnessDestination(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon,
    val description: String,
) {
    object Home : FitnessDestination(
        route = "home",
        title = "Home",
        icon = Icons.Default.Home,
        selectedIcon = Icons.Filled.Home,
        description = "View your fitness dashboard and daily progress",
    )

    object Workouts : FitnessDestination(
        route = "workouts",
        title = "Workouts",
        icon = Icons.Default.FitnessCenter,
        selectedIcon = Icons.Filled.FitnessCenter,
        description = "Track and manage your workout sessions",
    )

    object Progress : FitnessDestination(
        route = "progress",
        title = "Progress",
        icon = Icons.Default.Timeline,
        selectedIcon = Icons.Filled.Timeline,
        description = "View your fitness progress and statistics",
    )

    object Goals : FitnessDestination(
        route = "goals",
        title = "Goals",
        icon = Icons.Default.Star,
        selectedIcon = Icons.Filled.Star,
        description = "Set and track your fitness goals",
    )

    object Nutrition : FitnessDestination(
        route = "nutrition",
        title = "Nutrition",
        icon = Icons.Default.LocalDining,
        selectedIcon = Icons.Filled.LocalDining,
        description = "Log your meals and track nutrition",
    )

    object Steps : FitnessDestination(
        route = "steps",
        title = "Steps",
        icon = Icons.AutoMirrored.Filled.DirectionsRun,
        selectedIcon = Icons.AutoMirrored.Filled.DirectionsRun,
        description = "Track your daily steps and distance",
    )

    object Profile : FitnessDestination(
        route = "profile",
        title = "Profile",
        icon = Icons.Default.Person,
        selectedIcon = Icons.Filled.Person,
        description = "View and manage your profile settings",
    )
}

/**
 * Adaptive navigation component that chooses the appropriate navigation type
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptiveNavigation(
    navController: NavController,
    windowSize: WindowSizeClass,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    val destinations = remember {
        listOf(
            FitnessDestination.Home,
            FitnessDestination.Workouts,
            FitnessDestination.Progress,
            FitnessDestination.Goals,
            FitnessDestination.Nutrition,
        )
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    when (windowSize.widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            // Bottom navigation for phones
            CompactNavigation(
                navController = navController,
                destinations = destinations,
                currentDestination = currentDestination,
                modifier = modifier,
                content = content,
            )
        }

        WindowWidthSizeClass.Medium -> {
            // Navigation rail for tablets in portrait
            MediumNavigation(
                navController = navController,
                destinations = destinations,
                currentDestination = currentDestination,
                modifier = modifier,
                content = content,
            )
        }

        WindowWidthSizeClass.Expanded -> {
            // Navigation drawer for tablets in landscape and desktops
            ExpandedNavigation(
                navController = navController,
                destinations = destinations,
                currentDestination = currentDestination,
                modifier = modifier,
                content = content,
            )
        }
    }
}

/**
 * Bottom navigation for compact screens (phones)
 */
@Composable
private fun CompactNavigation(
    navController: NavController,
    destinations: List<FitnessDestination>,
    currentDestination: androidx.navigation.NavDestination?,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        bottomBar = {
            AnimatedVisibility(
                visible = true,
                enter = slideInHorizontally(
                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                ) + fadeIn(),
                exit = slideOutHorizontally() + fadeOut(),
            ) {
                NavigationBar(
                    modifier = Modifier.clip(FitnessShapes.NavigationItem),
                    tonalElevation = 8.dp,
                ) {
                    destinations.forEach { destination ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (currentDestination?.hierarchy?.any {
                                            it.route == destination.route
                                        } == true
                                    ) {
                                        destination.selectedIcon
                                    } else {
                                        destination.icon
                                    },
                                    contentDescription = destination.title,
                                )
                            },
                            label = {
                                Text(
                                    text = destination.title,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            },
                            selected = currentDestination?.hierarchy?.any {
                                it.route == destination.route
                            } == true,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            modifier = Modifier.semantics {
                                contentDescription = destination.description
                            },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        content(innerPadding)
    }
}

/**
 * Navigation rail for medium screens (tablets in portrait)
 */
@Composable
private fun MediumNavigation(
    navController: NavController,
    destinations: List<FitnessDestination>,
    currentDestination: androidx.navigation.NavDestination?,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    Row(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = true,
            enter = slideInHorizontally(
                animationSpec = spring(stiffness = Spring.StiffnessLow),
            ) + fadeIn(),
            exit = slideOutHorizontally() + fadeOut(),
        ) {
            NavigationRail(
                modifier = Modifier
                    .clip(FitnessShapes.NavigationRail)
                    .padding(start = 8.dp, top = 8.dp, bottom = 8.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                header = {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = "Fitness Tracker",
                        modifier = Modifier
                            .padding(16.dp)
                            .size(32.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                },
            ) {
                destinations.forEach { destination ->
                    NavigationRailItem(
                        icon = {
                            Icon(
                                imageVector = if (currentDestination?.hierarchy?.any {
                                        it.route == destination.route
                                    } == true
                                ) {
                                    destination.selectedIcon
                                } else {
                                    destination.icon
                                },
                                contentDescription = destination.title,
                            )
                        },
                        label = {
                            Text(
                                text = destination.title,
                                style = MaterialTheme.typography.labelMedium,
                            )
                        },
                        selected = currentDestination?.hierarchy?.any {
                            it.route == destination.route
                        } == true,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .semantics {
                                contentDescription = destination.description
                            },
                    )
                }
            }
        }

        content(PaddingValues())
    }
}

/**
 * Navigation drawer for expanded screens (tablets in landscape, desktops)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpandedNavigation(
    navController: NavController,
    destinations: List<FitnessDestination>,
    currentDestination: androidx.navigation.NavDestination?,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)

    PermanentNavigationDrawer(
        drawerContent = {
            PermanentDrawerSheet(
                modifier = Modifier
                    .width(280.dp)
                    .clip(FitnessShapes.NavigationRail),
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerContentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                // Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = "Fitness Tracker",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(FitnessShapes.Badge),
                        tint = MaterialTheme.colorScheme.primary,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Fitness Tracker",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                )

                // Navigation items
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 8.dp),
                ) {
                    destinations.forEach { destination ->
                        NavigationDrawerItem(
                            icon = {
                                Icon(
                                    imageVector = if (currentDestination?.hierarchy?.any {
                                            it.route == destination.route
                                        } == true
                                    ) {
                                        destination.selectedIcon
                                    } else {
                                        destination.icon
                                    },
                                    contentDescription = destination.title,
                                )
                            },
                            label = {
                                Text(
                                    text = destination.title,
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            },
                            selected = currentDestination?.hierarchy?.any {
                                it.route == destination.route
                            } == true,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                                .semantics {
                                    contentDescription = destination.description
                                },
                            shape = FitnessShapes.NavigationItem,
                        )
                    }
                }
            }
        },
        modifier = modifier,
    ) {
        content(PaddingValues())
    }
}

/**
 * Get window size class for responsive design
 */
@Composable
fun getWindowSizeClass(): WindowSizeClass {
    val configuration = LocalConfiguration.current
    return WindowSizeClass.calculateFromSize(
        size = androidx.compose.ui.geometry.Size(
            width = configuration.screenWidthDp.toFloat(),
            height = configuration.screenHeightDp.toFloat(),
        ),
    )
}
