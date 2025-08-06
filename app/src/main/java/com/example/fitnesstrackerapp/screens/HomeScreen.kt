/**
 * Home screen for the Fitness Tracker App.
 *
 * Displays a welcome message, overview of features, quick stats, and provides navigation to all core app areas. 
 * Retrieves logged-in user data, triggers daily notifications, and uses Material Design 3 with modular composables.
 * Features an enhanced dashboard with fitness metrics and quick action cards.
 */

package com.example.fitnesstrackerapp.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.Timeline
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.fitnesstrackerapp.R
import com.example.fitnesstrackerapp.navigation.Screen
import com.example.fitnesstrackerapp.ui.components.BottomNavigationBar
import com.example.fitnesstrackerapp.ui.viewmodel.AuthViewModel
import com.example.fitnesstrackerapp.notifications.NotificationScheduler
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val onClick: () -> Unit
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
    val icon: ImageVector
)

/**
 * Top-level composable for the home screen.
 *
 * Displays a welcome message, user information, fitness stats, quick actions, and navigation to all core app features. 
 * Schedules daily notifications and uses a ViewModel for user state management.
 *
 * @param navController NavController for navigation between screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController
) {
    val context = LocalContext.current

    // Initialize the ViewModel using Hilt injection
    val authViewModel: AuthViewModel = koinViewModel()

    // Observe user state using StateFlow + collectAsStateWithLifecycle
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()

    // Schedule a daily notification when the screen is shown
    LaunchedEffect(Unit) {
        NotificationScheduler(context).scheduleDailyReminder()
    }

    // Quick actions for navigation
    val quickActions = listOf(
        QuickAction(
            title = "Log Workout",
            description = "Record your exercise",
            icon = Icons.Default.FitnessCenter,
            onClick = { navController.navigate(Screen.Workout.route) }
        ),
        QuickAction(
            title = "Track Nutrition",
            description = "Log your meals",
            icon = Icons.Default.LocalDining,
            onClick = { navController.navigate(Screen.Nutrition.route) }
        ),
        QuickAction(
            title = "Set Goals",
            description = "Define your targets",
            icon = Icons.Default.TrackChanges,
            onClick = { navController.navigate(Screen.Goal.route) }
        ),
        QuickAction(
            title = "View Progress",
            description = "See your achievements",
            icon = Icons.Default.Timeline,
            onClick = { navController.navigate(Screen.Progress.route) }
        )
    )

    // Sample fitness stats (in a real app, these would come from ViewModels)
    val fitnessStats = listOf(
        FitnessStat(
            label = "Today's Steps",
            value = "0",
            unit = "steps",
            icon = Icons.Default.DirectionsRun
        ),
        FitnessStat(
            label = "Calories Burned",
            value = "0",
            unit = "kcal",
            icon = Icons.Default.FitnessCenter
        ),
        FitnessStat(
            label = "Workouts",
            value = "0",
            unit = "today",
            icon = Icons.Default.Timeline
        )
    )

    // App layout using Scaffold
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Fitness Tracker",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        bottomBar = { 
            BottomNavigationBar(navController = navController) 
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // Welcome Section
            WelcomeSection(
                userName = authState.currentUser?.email?.substringBefore("@") ?: "User"
            )

            // Today's Stats Section
            TodaysStatsSection(stats = fitnessStats)

            // Quick Actions Section
            QuickActionsSection(actions = quickActions)

            // Motivational Quote Section
            MotivationalSection()

            // Add some bottom padding for better scrolling experience
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Welcome section displaying user greeting and current date.
 *
 * @param userName The name of the current user.
 */
@Composable
private fun WelcomeSection(userName: String) {
    val currentDate = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Fitness icon
            Image(
                painter = painterResource(id = R.drawable.ic_fitness),
                contentDescription = "Fitness Icon",
                modifier = Modifier.size(60.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = "Hello, $userName!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = currentDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Ready for your fitness journey?",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

/**
 * Section displaying today's fitness statistics.
 *
 * @param stats List of fitness statistics to display.
 */
@Composable
private fun TodaysStatsSection(stats: List<FitnessStat>) {
    Column {
        Text(
            text = "Today's Overview",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(stats) { stat ->
                StatCard(stat = stat)
            }
        }
    }
}

/**
 * Individual stat card component.
 *
 * @param stat The fitness stat to display.
 */
@Composable
private fun StatCard(stat: FitnessStat) {
    Card(
        modifier = Modifier.width(120.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = stat.icon,
                contentDescription = stat.label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stat.value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stat.unit,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stat.label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Section displaying quick action cards for navigation.
 *
 * @param actions List of quick actions to display.
 */
@Composable
private fun QuickActionsSection(actions: List<QuickAction>) {
    Column {
        Text(
            text = "Quick Actions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(actions) { action ->
                QuickActionCard(action = action)
            }
        }
    }
}

/**
 * Individual quick action card component.
 *
 * @param action The quick action to display.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickActionCard(action: QuickAction) {
    Card(
        onClick = action.onClick,
        modifier = Modifier.width(160.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = action.title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = action.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = action.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Motivational section with encouraging message.
 */
@Composable
private fun MotivationalSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "💪",
                    style = MaterialTheme.typography.displaySmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "\"Every workout brings you closer to your goals!\"",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "Keep pushing forward!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}
