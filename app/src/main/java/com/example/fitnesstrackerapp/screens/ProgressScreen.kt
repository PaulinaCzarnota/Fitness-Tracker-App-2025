/**
 * Progress tracking screen for the Fitness Tracker App.
 *
 * This comprehensive progress visualization interface provides:
 * - Real-time fitness statistics and achievements
 * - Interactive charts for workout and step data visualization
 * - Weekly, monthly, and yearly progress summaries
 * - Goal completion tracking with progress indicators
 * - Historical workout and activity data analysis
 * - Motivational insights and performance trends
 *
 * The screen follows Material Design 3 principles and implements responsive
 * design patterns for optimal user experience across different screen sizes.
 */

package com.example.fitnesstrackerapp.screens

import androidx.activity.ComponentActivity
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.fitnesstrackerapp.ViewModelFactoryProvider

/**
 * Data classes for progress tracking
 */
data class ProgressStatistic(
    val label: String,
    val value: String,
    val change: String,
    val icon: ImageVector,
    val isPositive: Boolean = true,
)

data class WeeklyData(
    val day: String,
    val steps: Int,
    val workouts: Int,
)

/**
 * Top-level composable for the enhanced progress tracking screen.
 *
 * Features comprehensive fitness analytics with:
 * - Weekly and monthly progress summaries
 * - Interactive data visualizations
 * - Achievement tracking and milestones
 * - Trend analysis and performance insights
 * - Goal progress monitoring
 *
 * @param navController NavController for navigation between screens
 * @param modifier Modifier for styling the composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    // Initialize ViewModel via factory
    val activity = LocalContext.current as ComponentActivity
    remember {
        ViewModelFactoryProvider.getProgressViewModel(activity)
    }

    // Sample data - in real app, this would come from ViewModel
    val weeklyStats = remember {
        listOf(
            ProgressStatistic("Steps", "52,341", "+12%", Icons.AutoMirrored.Filled.DirectionsRun),
            ProgressStatistic("Workouts", "8", "+2", Icons.Default.FitnessCenter),
            ProgressStatistic("Calories", "2,840", "+15%", Icons.Default.LocalFireDepartment),
            ProgressStatistic("Distance", "38.2 km", "+8%", Icons.Default.Route),
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Progress Tracking",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                WelcomeSection()
            }

            item {
                WeeklyStatsSection(statistics = weeklyStats)
            }

            item {
                WeeklyProgressChart()
            }

            item {
                GoalProgressSection()
            }

            item {
                AchievementsSection()
            }

            item {
                RecentActivitySection()
            }
        }
    }
}

/**
 * Welcome section with personalized greeting and motivation
 */
@Composable
private fun WelcomeSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
        ) {
            Text(
                text = "Your Fitness Journey",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Keep up the great work! Here's how you've been doing this week.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
            )
        }
    }
}

/**
 * Weekly statistics section showing key metrics
 */
@Composable
private fun WeeklyStatsSection(
    statistics: List<ProgressStatistic>,
) {
    Column {
        Text(
            text = "This Week's Stats",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(statistics) { stat ->
                StatisticCard(statistic = stat)
            }
        }
    }
}

/**
 * Individual statistic card
 */
@Composable
private fun StatisticCard(
    statistic: ProgressStatistic,
) {
    Card(
        modifier = Modifier.width(140.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = statistic.icon,
                contentDescription = statistic.label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = statistic.value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = statistic.label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = statistic.change,
                style = MaterialTheme.typography.labelSmall,
                color = if (statistic.isPositive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
            )
        }
    }
}

/**
 * Weekly progress chart section
 */
@Composable
private fun WeeklyProgressChart() {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
        ) {
            Text(
                text = "Weekly Activity",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Simple bar chart representation
            val weeklyData = listOf(
                WeeklyData("Mon", 8500, 1),
                WeeklyData("Tue", 12000, 2),
                WeeklyData("Wed", 6500, 0),
                WeeklyData("Thu", 15000, 1),
                WeeklyData("Fri", 11200, 2),
                WeeklyData("Sat", 9800, 1),
                WeeklyData("Sun", 7500, 0),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                weeklyData.forEach { data ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // Simple bar representation
                        val height = (data.steps / 200).dp.coerceAtMost(80.dp)
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(height.coerceAtLeast(4.dp))
                                .clip(RoundedCornerShape(4.dp)),
                        ) {
                            Card(
                                modifier = Modifier.fillMaxSize(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                ),
                            ) {}
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = data.day,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                ChartLegend("Steps", MaterialTheme.colorScheme.primary)
                ChartLegend("Workouts", MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

/**
 * Chart legend component
 */
@Composable
private fun ChartLegend(
    label: String,
    color: androidx.compose.ui.graphics.Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(2.dp)),
        ) {
            Card(
                modifier = Modifier.fillMaxSize(),
                colors = CardDefaults.cardColors(containerColor = color),
            ) {}
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

/**
 * Goal progress section
 */
@Composable
private fun GoalProgressSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
        ) {
            Text(
                text = "Goal Progress",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sample goals with progress
            GoalProgressItem("Daily Steps", 8500, 10000)
            Spacer(modifier = Modifier.height(12.dp))
            GoalProgressItem("Weekly Workouts", 5, 7)
            Spacer(modifier = Modifier.height(12.dp))
            GoalProgressItem("Monthly Distance", 85, 100)
        }
    }
}

/**
 * Individual goal progress item
 */
@Composable
private fun GoalProgressItem(
    goalName: String,
    current: Int,
    target: Int,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = goalName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "$current / $target",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { (current.toFloat() / target).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

/**
 * Achievements section
 */
@Composable
private fun AchievementsSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
        ) {
            Text(
                text = "Recent Achievements",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            val achievements = listOf(
                "🏆 Completed 5-day workout streak!",
                "🎯 Reached weekly step goal!",
                "🔥 Burned 500+ calories in one workout!",
                "📈 Personal best: 15,000 steps in a day!",
            )

            achievements.forEach { achievement ->
                AchievementItem(achievement)
                if (achievement != achievements.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

/**
 * Individual achievement item
 */
@Composable
private fun AchievementItem(
    achievement: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = "Achievement",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = achievement,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

/**
 * Recent activity section
 */
@Composable
private fun RecentActivitySection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
        ) {
            Text(
                text = "Recent Activity",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            val activities = listOf(
                "Running - 30 min, 350 kcal" to "2 hours ago",
                "Steps - 12,500 steps" to "Today",
                "Strength Training - 45 min" to "Yesterday",
                "Cycling - 1 hour, 400 kcal" to "2 days ago",
            )

            activities.forEach { (activity, time) ->
                ActivityItem(activity, time)
                if (activity != activities.last().first) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

/**
 * Individual activity item
 */
@Composable
private fun ActivityItem(
    activity: String,
    time: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = activity,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = time,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "View details",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
