package com.example.fitnesstrackerapp.screens

/**
 * Home screen for the Fitness Tracker App.
 *
 * Displays a welcome message, overview of features, quick stats, and provides navigation to all core app areas.
 * Retrieves logged-in user data, triggers daily notifications, and uses Material Design 3 with modular composables.
 * Features an enhanced dashboard with fitness metrics and quick action cards.
 */

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Data class representing a quick action card on the home screen.
 *
 * @property title The title of the action.
 * @property description Brief description of what the action does.
 * @property icon The icon to display for this action.
 * @property onClick Callback when the card is clicked.
 */
data class QuickAction(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
)

/**
 * Data class representing a fitness stat to display on the home screen.
 *
 * @property label The label for the stat (e.g., "Today's Steps").
 * @property value The current value of the stat.
 * @property unit The unit of measurement (e.g., "steps", "kcal").
 * @property icon The icon to display with this stat.
 */
data class FitnessStat(
    val label: String,
    val value: String,
    val unit: String,
    val icon: ImageVector,
)

/**
 * Top-level composable for the home screen.
 *
 * Displays a welcome message, user information, fitness stats, quick actions, and navigation to all core app features.
 * Schedules daily notifications and uses a ViewModel for user state management.
 *
 * @param navController NavController for navigation between screens.
 */
@SuppressLint("ScheduleExactAlarm")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: androidx.navigation.NavController,
    authViewModel: com.example.fitnesstrackerapp.ui.auth.AuthViewModel,
    windowSize: WindowSizeClass? = null,
) {
    LocalContext.current

    // Step Dashboard ViewModel for real-time step tracking
    val stepDashboardViewModel: com.example.fitnesstrackerapp.ui.viewmodel.StepDashboardViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel()

    // Observe user state using StateFlow
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    // Observe real-time step data
    val stepData by stepDashboardViewModel.stepData.collectAsStateWithLifecycle()
    val isServiceConnected by stepDashboardViewModel.isServiceConnected.collectAsStateWithLifecycle()
    val errorMessage by stepDashboardViewModel.errorMessage.collectAsStateWithLifecycle()

    // Determine displayed userName based on state
    val userName = authState.user?.email?.substringBefore("@") ?: "User"

    // Handle errors
    errorMessage?.let { error ->
        LaunchedEffect(error) {
            Log.d("HomeScreen", "Step service error: $error")
            // Could show a snackbar here if needed
            stepDashboardViewModel.clearError()
        }
    }

    // Note: Daily notifications are handled by WorkManagerScheduler in the background
    // The notification scheduling is automatically initialized in FitnessApplication
    // No additional setup needed here as WorkManager handles the scheduling

    // Quick actions for navigation
    val quickActions = listOf(
        QuickAction(
            title = "Log Workout",
            description = "Record your exercise",
            icon = Icons.Default.FitnessCenter,
            onClick = { /* Navigate to workout - handled by bottom nav */ },
        ),
        QuickAction(
            title = "Track Nutrition",
            description = "Log your meals",
            icon = Icons.Default.LocalDining,
            onClick = { /* Navigate to nutrition - handled by bottom nav */ },
        ),
        QuickAction(
            title = "Set Goals",
            description = "Define your targets",
            icon = Icons.Default.TrackChanges,
            onClick = { navController.navigate("goals") },
        ),
        QuickAction(
            title = "Logout",
            description = "Sign out of app",
            icon = Icons.AutoMirrored.Filled.ExitToApp,
            onClick = { authViewModel.logout() },
        ),
    )

    // Enhanced fitness stats with real-time data from step tracking service
    val fitnessStats = listOf(
        FitnessStat(
            label = "Today's Steps",
            value = String.format("%,d", stepData.steps),
            unit = if (isServiceConnected) "${stepData.progress.toInt()}% of goal" else "tracking...",
            icon = Icons.AutoMirrored.Filled.DirectionsRun,
        ),
        FitnessStat(
            label = "Calories Burned",
            value = stepDashboardViewModel.getFormattedCalories().substringBefore(" "),
            unit = "kcal from steps",
            icon = Icons.Default.LocalFireDepartment,
        ),
        FitnessStat(
            label = "Distance",
            value = stepDashboardViewModel.getFormattedDistance().substringBefore(" "),
            unit = stepDashboardViewModel.getFormattedDistance().substringAfter(" "),
            icon = Icons.AutoMirrored.Filled.DirectionsRun,
        ),
        FitnessStat(
            label = "Activity Level",
            value = stepDashboardViewModel.getActivityLevel(),
            unit = if (stepData.isTracking) "live tracking" else "inactive",
            icon = Icons.Default.FitnessCenter,
        ),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Good ${getGreeting()}!",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = "Welcome back, $userName",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
    ) { innerPadding ->
        when (windowSize?.widthSizeClass) {
            WindowWidthSizeClass.Compact -> {
                // Compact layout: Single column with scrolling
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(innerPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    FitnessStatsSection(stats = fitnessStats, windowSize = windowSize)
                    QuickActionsSection(actions = quickActions, windowSize = windowSize)
                }
            }
            WindowWidthSizeClass.Medium -> {
                // Medium layout: Grid layout for better space utilization
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    item {
                        FitnessStatsSection(stats = fitnessStats, windowSize = windowSize)
                    }
                    item {
                        QuickActionsSection(actions = quickActions, windowSize = windowSize)
                    }
                }
            }
            WindowWidthSizeClass.Expanded -> {
                // Expanded layout: Side-by-side layout for large screens
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 32.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        FitnessStatsSection(stats = fitnessStats, windowSize = windowSize)
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        QuickActionsSection(actions = quickActions, windowSize = windowSize)
                    }
                }
            }
            else -> {
                // Default layout: Single column
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(innerPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    FitnessStatsSection(stats = fitnessStats, windowSize = windowSize)
                    QuickActionsSection(actions = quickActions, windowSize = windowSize)
                }
            }
        }
    }
}

/**
 * Gets appropriate greeting based on time of day.
 */
private fun getGreeting(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 0..11 -> "Morning"
        in 12..17 -> "Afternoon"
        else -> "Evening"
    }
}

/**
 * Composable for displaying fitness statistics.
 */
@Composable
private fun FitnessStatsSection(
    stats: List<FitnessStat>,
    windowSize: WindowSizeClass? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "Today's Summary",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        when (windowSize?.widthSizeClass) {
            WindowWidthSizeClass.Expanded -> {
                // Grid layout for expanded screens
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(stats) { stat ->
                        StatCard(stat = stat, windowSize = windowSize)
                    }
                }
            }
            WindowWidthSizeClass.Medium -> {
                // 2-column grid for medium screens
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(stats) { stat ->
                        StatCard(stat = stat, windowSize = windowSize)
                    }
                }
            }
            else -> {
                // Horizontal scroll for compact screens
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(stats) { stat ->
                        StatCard(stat = stat, windowSize = windowSize)
                    }
                }
            }
        }
    }
}

/**
 * Composable for displaying quick action buttons.
 */
@Composable
private fun QuickActionsSection(
    actions: List<QuickAction>,
    windowSize: WindowSizeClass? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )

        when (windowSize?.widthSizeClass) {
            WindowWidthSizeClass.Expanded -> {
                // Grid layout for expanded screens
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(actions) { action ->
                        ActionCard(action = action, windowSize = windowSize)
                    }
                }
            }
            WindowWidthSizeClass.Medium -> {
                // 2-column grid for medium screens
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(actions) { action ->
                        ActionCard(action = action, windowSize = windowSize)
                    }
                }
            }
            else -> {
                // Horizontal scroll for compact screens
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(actions) { action ->
                        ActionCard(action = action, windowSize = windowSize)
                    }
                }
            }
        }
    }
}

/**
 * Individual stat card component.
 */
@Composable
private fun StatCard(
    stat: FitnessStat,
    windowSize: WindowSizeClass? = null,
    modifier: Modifier = Modifier,
) {
    val cardWidth = when (windowSize?.widthSizeClass) {
        WindowWidthSizeClass.Expanded -> 160.dp
        WindowWidthSizeClass.Medium -> 140.dp
        else -> 120.dp
    }

    val iconSize = when (windowSize?.widthSizeClass) {
        WindowWidthSizeClass.Expanded -> 32.dp
        WindowWidthSizeClass.Medium -> 28.dp
        else -> 24.dp
    }

    val padding = when (windowSize?.widthSizeClass) {
        WindowWidthSizeClass.Expanded -> 16.dp
        WindowWidthSizeClass.Medium -> 14.dp
        else -> 12.dp
    }

    Card(
        modifier = if (windowSize?.widthSizeClass == WindowWidthSizeClass.Expanded ||
            windowSize?.widthSizeClass == WindowWidthSizeClass.Medium
        ) {
            modifier.fillMaxSize()
        } else {
            modifier.width(cardWidth)
        },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = stat.icon,
                contentDescription = stat.label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(iconSize),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stat.value,
                style = when (windowSize?.widthSizeClass) {
                    WindowWidthSizeClass.Expanded -> MaterialTheme.typography.titleLarge
                    else -> MaterialTheme.typography.titleMedium
                },
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stat.unit,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stat.label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Individual action card component.
 */
@Composable
private fun ActionCard(
    action: QuickAction,
    windowSize: WindowSizeClass? = null,
    modifier: Modifier = Modifier,
) {
    val cardWidth = when (windowSize?.widthSizeClass) {
        WindowWidthSizeClass.Expanded -> 180.dp
        WindowWidthSizeClass.Medium -> 160.dp
        else -> 140.dp
    }

    val iconSize = when (windowSize?.widthSizeClass) {
        WindowWidthSizeClass.Expanded -> 40.dp
        WindowWidthSizeClass.Medium -> 36.dp
        else -> 32.dp
    }

    val padding = when (windowSize?.widthSizeClass) {
        WindowWidthSizeClass.Expanded -> 20.dp
        WindowWidthSizeClass.Medium -> 18.dp
        else -> 16.dp
    }

    Card(
        onClick = action.onClick,
        modifier = if (windowSize?.widthSizeClass == WindowWidthSizeClass.Expanded ||
            windowSize?.widthSizeClass == WindowWidthSizeClass.Medium
        ) {
            modifier.fillMaxSize()
        } else {
            modifier.width(cardWidth)
        },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = action.title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(iconSize),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = action.title,
                style = when (windowSize?.widthSizeClass) {
                    WindowWidthSizeClass.Expanded -> MaterialTheme.typography.titleMedium
                    else -> MaterialTheme.typography.titleSmall
                },
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = action.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
