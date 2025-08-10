package com.example.fitnesstrackerapp.ui.progress

/**
 * Enhanced Progress Tracking Components with MPAndroidChart Integration
 *
 * This file provides advanced data visualization components for fitness progress tracking
 * using MPAndroidChart library. Features include:
 * - Interactive line and bar charts
 * - Real-time data visualization
 * - Daily, weekly, and monthly summaries
 * - Goal progress tracking with animations
 * - Comprehensive workout and nutrition analytics
 */

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Data classes for chart data
 */
data class ProgressChartData(
    val date: Date,
    val value: Float,
    val label: String = "",
    val goal: Float? = null,
)

data class WorkoutProgressData(
    val workouts: List<Float>,
    val calories: List<Float>,
    val duration: List<Float>,
    val dates: List<String>,
)

data class NutritionProgressData(
    val calories: List<Float>,
    val protein: List<Float>,
    val carbs: List<Float>,
    val fat: List<Float>,
    val dates: List<String>,
)

/**
 * Main progress tracking screen with enhanced charts
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedProgressScreen(
    workoutData: WorkoutProgressData,
    nutritionData: NutritionProgressData,
    stepData: List<ProgressChartData>,
    goalProgress: Map<String, Float>,
    onPeriodChanged: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var selectedPeriod by remember { mutableStateOf("Weekly") }
    val periods = listOf("Daily", "Weekly", "Monthly")

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header with period selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Progress Analytics",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )

            // Period selector
            Card {
                Row(
                    modifier = Modifier.padding(4.dp),
                ) {
                    periods.forEach { period ->
                        FilterChip(
                            onClick = {
                                selectedPeriod = period
                                onPeriodChanged(period)
                            },
                            label = { Text(period) },
                            selected = selectedPeriod == period,
                            modifier = Modifier.padding(horizontal = 4.dp),
                        )
                    }
                }
            }
        }

        // Goal progress overview
        GoalProgressSection(goalProgress = goalProgress)

        // Workout progress chart
        WorkoutProgressChart(
            workoutData = workoutData,
            title = "Workout Progress",
        )

        // Steps progress chart
        StepsProgressChart(
            stepData = stepData,
            title = "Daily Steps",
        )

        // Nutrition progress chart
        NutritionProgressChart(
            nutritionData = nutritionData,
            title = "Nutrition Tracking",
        )

        // Weekly comparison
        WeeklyComparisonChart(
            workoutData = workoutData,
            title = "Weekly Comparison",
        )
    }
}

/**
 * Goal progress section with circular progress indicators
 */
@Composable
fun GoalProgressSection(
    goalProgress: Map<String, Float>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Goal Progress",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                goalProgress.forEach { (goalName, progress) ->
                    GoalProgressIndicator(
                        goalName = goalName,
                        progress = progress,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

/**
 * Individual goal progress indicator with enhanced animations
 */
@Composable
fun GoalProgressIndicator(
    goalName: String,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    // Main progress animation
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(
            durationMillis = 1500,
            easing = LinearEasing,
        ),
        label = "Goal Progress Animation",
    )

    // Pulsing animation for completed goals
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse Animation")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = if (progress >= 1f) 0.3f else 1f,
        targetValue = if (progress >= 1f) 1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "Pulse Alpha",
    )

    Column(
        modifier = modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(80.dp),
            contentAlignment = Alignment.Center,
        ) {
            // Background circle
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.size(80.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                strokeWidth = 8.dp,
            )
            // Progress circle with pulsing effect for completed goals
            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.size(80.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha),
                strokeWidth = 8.dp,
            )
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = goalName,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Enhanced workout progress chart using Compose
 */
@Composable
fun WorkoutProgressChart(
    workoutData: WorkoutProgressData,
    title: String,
    modifier: Modifier = Modifier,
) {
    MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            if (workoutData.workouts.isNotEmpty() && workoutData.calories.isNotEmpty()) {
                // Simple bar chart representation
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Legend
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(bottom = 8.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Workouts",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(secondaryColor, RoundedCornerShape(2.dp)),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Calories (÷10)",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }

                    // Chart data
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        workoutData.dates.forEachIndexed { index, date ->
                            if (index < workoutData.workouts.size && index < workoutData.calories.size) {
                                val workoutValue = workoutData.workouts[index]
                                val calorieValue = workoutData.calories[index] / 10f
                                val maxValue = maxOf(
                                    workoutData.workouts.maxOrNull() ?: 1f,
                                    (workoutData.calories.maxOrNull() ?: 10f) / 10f,
                                )

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f),
                                ) {
                                    // Workout bar
                                    Box(
                                        modifier = Modifier
                                            .width(20.dp)
                                            .height((workoutValue / maxValue * 150).dp)
                                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)),
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    // Calorie bar
                                    Box(
                                        modifier = Modifier
                                            .width(20.dp)
                                            .height((calorieValue / maxValue * 150).dp)
                                            .background(secondaryColor, RoundedCornerShape(2.dp)),
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = date.take(3),
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No workout data available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/**
 * Steps progress chart with daily goals using Compose
 */
@Composable
fun StepsProgressChart(
    stepData: List<ProgressChartData>,
    title: String,
    modifier: Modifier = Modifier,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val goalColor = MaterialTheme.colorScheme.tertiary

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            if (stepData.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Legend
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(bottom = 8.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Steps",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(goalColor, RoundedCornerShape(2.dp)),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Goal",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }

                    // Chart data
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        stepData.forEach { data ->
                            val maxValue = maxOf(
                                stepData.maxOfOrNull { it.value } ?: 1f,
                                stepData.mapNotNull { it.goal }.maxOrNull() ?: 1f,
                            )

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f),
                            ) {
                                // Goal bar (background)
                                data.goal?.let { goal ->
                                    Box(
                                        modifier = Modifier
                                            .width(30.dp)
                                            .height((goal / maxValue * 150).dp)
                                            .background(goalColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp)),
                                    )
                                }
                                // Steps bar (foreground)
                                Box(
                                    modifier = Modifier
                                        .width(30.dp)
                                        .height((data.value / maxValue * 150).dp)
                                        .background(primaryColor, RoundedCornerShape(4.dp)),
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = SimpleDateFormat("MM/dd", Locale.US).format(data.date),
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No step data available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/**
 * Nutrition progress chart using Compose
 */
@Composable
fun NutritionProgressChart(
    nutritionData: NutritionProgressData,
    title: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            // Show latest day's nutrition breakdown
            val latestIndex = nutritionData.dates.lastIndex
            if (latestIndex >= 0) {
                val protein = nutritionData.protein.getOrNull(latestIndex) ?: 0f
                val carbs = nutritionData.carbs.getOrNull(latestIndex) ?: 0f
                val fat = nutritionData.fat.getOrNull(latestIndex) ?: 0f
                val total = protein + carbs + fat

                if (total > 0) {
                    val proteinPercent = (protein / total * 100).toInt()
                    val carbsPercent = (carbs / total * 100).toInt()
                    val fatPercent = (fat / total * 100).toInt()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        // Protein
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f),
                        ) {
                            Box(
                                modifier = Modifier
                                    .height(100.dp)
                                    .width(40.dp)
                                    .background(
                                        androidx.compose.ui.graphics.Color(0xFF4CAF50),
                                        RoundedCornerShape(4.dp),
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "$proteinPercent%",
                                    color = androidx.compose.ui.graphics.Color.White,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Protein",
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Text(
                                text = "${protein.toInt()}g",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        // Carbs
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f),
                        ) {
                            Box(
                                modifier = Modifier
                                    .height(100.dp)
                                    .width(40.dp)
                                    .background(
                                        androidx.compose.ui.graphics.Color(0xFF2196F3),
                                        RoundedCornerShape(4.dp),
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "$carbsPercent%",
                                    color = androidx.compose.ui.graphics.Color.White,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Carbs",
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Text(
                                text = "${carbs.toInt()}g",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        // Fat
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f),
                        ) {
                            Box(
                                modifier = Modifier
                                    .height(100.dp)
                                    .width(40.dp)
                                    .background(
                                        androidx.compose.ui.graphics.Color(0xFFFF9800),
                                        RoundedCornerShape(4.dp),
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "$fatPercent%",
                                    color = androidx.compose.ui.graphics.Color.White,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Fat",
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Text(
                                text = "${fat.toInt()}g",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No nutrition data for today",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No nutrition data available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/**
 * Weekly comparison chart showing progress trends
 */
@Composable
fun WeeklyComparisonChart(
    workoutData: WorkoutProgressData,
    title: String,
    modifier: Modifier = Modifier,
) {
    LocalContext.current
    MaterialTheme.colorScheme.primary.toArgb()

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            if (workoutData.duration.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // Legend
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(bottom = 8.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Duration (min)",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }

                    // Chart data
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        workoutData.dates.forEachIndexed { index, date ->
                            if (index < workoutData.duration.size) {
                                val duration = workoutData.duration[index]
                                val maxValue = workoutData.duration.maxOrNull() ?: 1f

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(30.dp)
                                            .height((duration / maxValue * 150).dp)
                                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)),
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = date.take(3),
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                    )
                                    Text(
                                        text = "${duration.toInt()}m",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No duration data available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

// =============================================================================
// PREVIEW COMPOSABLES FOR QA TESTING
// =============================================================================

/**
 * Sample data for preview testing
 */
private val sampleWorkoutData = WorkoutProgressData(
    workouts = listOf(3f, 2f, 4f, 1f, 5f, 2f, 3f),
    calories = listOf(250f, 180f, 320f, 120f, 400f, 200f, 280f),
    duration = listOf(45f, 30f, 60f, 25f, 75f, 35f, 50f),
    dates = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
)

private val sampleNutritionData = NutritionProgressData(
    calories = listOf(2000f, 1800f, 2200f, 1900f, 2100f),
    protein = listOf(120f, 110f, 130f, 115f, 125f),
    carbs = listOf(250f, 220f, 280f, 240f, 260f),
    fat = listOf(80f, 70f, 90f, 75f, 85f),
    dates = listOf("Day 1", "Day 2", "Day 3", "Day 4", "Day 5"),
)

private val sampleStepData = listOf(
    ProgressChartData(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -6) }.time, 8500f, goal = 10000f),
    ProgressChartData(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -5) }.time, 12000f, goal = 10000f),
    ProgressChartData(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -4) }.time, 9500f, goal = 10000f),
    ProgressChartData(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -3) }.time, 11200f, goal = 10000f),
    ProgressChartData(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -2) }.time, 7800f, goal = 10000f),
    ProgressChartData(Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.time, 13500f, goal = 10000f),
    ProgressChartData(Date(), 10800f, goal = 10000f),
)

private val sampleGoalProgress = mapOf(
    "Steps" to 0.85f,
    "Calories" to 0.92f,
    "Workouts" to 0.67f,
    "Sleep" to 1.0f,
)

@Preview(name = "Goal Progress Indicator - In Progress")
@Composable
fun PreviewGoalProgressIndicatorInProgress() {
    MaterialTheme {
        Surface {
            GoalProgressIndicator(
                goalName = "Steps",
                progress = 0.75f,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Preview(name = "Goal Progress Indicator - Completed")
@Composable
fun PreviewGoalProgressIndicatorCompleted() {
    MaterialTheme {
        Surface {
            GoalProgressIndicator(
                goalName = "Sleep",
                progress = 1.0f,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Preview(name = "Goal Progress Indicator - Low Progress")
@Composable
fun PreviewGoalProgressIndicatorLow() {
    MaterialTheme {
        Surface {
            GoalProgressIndicator(
                goalName = "Calories",
                progress = 0.25f,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Preview(name = "Goal Progress Section")
@Composable
fun PreviewGoalProgressSection() {
    MaterialTheme {
        Surface {
            GoalProgressSection(
                goalProgress = sampleGoalProgress,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Preview(name = "Workout Progress Chart")
@Composable
fun PreviewWorkoutProgressChart() {
    MaterialTheme {
        Surface {
            WorkoutProgressChart(
                workoutData = sampleWorkoutData,
                title = "Weekly Workout Progress",
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Preview(name = "Steps Progress Chart")
@Composable
fun PreviewStepsProgressChart() {
    MaterialTheme {
        Surface {
            StepsProgressChart(
                stepData = sampleStepData,
                title = "Daily Steps vs Goal",
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Preview(name = "Nutrition Progress Chart")
@Composable
fun PreviewNutritionProgressChart() {
    MaterialTheme {
        Surface {
            NutritionProgressChart(
                nutritionData = sampleNutritionData,
                title = "Today's Nutrition",
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Preview(name = "Weekly Comparison Chart")
@Composable
fun PreviewWeeklyComparisonChart() {
    MaterialTheme {
        Surface {
            WeeklyComparisonChart(
                workoutData = sampleWorkoutData,
                title = "This Week vs Last Week",
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Preview(name = "Enhanced Progress Screen", showSystemUi = true)
@Composable
fun PreviewEnhancedProgressScreen() {
    MaterialTheme {
        Surface {
            EnhancedProgressScreen(
                workoutData = sampleWorkoutData,
                nutritionData = sampleNutritionData,
                stepData = sampleStepData,
                goalProgress = sampleGoalProgress,
                onPeriodChanged = { },
            )
        }
    }
}

@Preview(name = "Enhanced Progress Screen - Light", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_NO)
@Composable
fun PreviewEnhancedProgressScreenLight() {
    MaterialTheme {
        Surface {
            EnhancedProgressScreen(
                workoutData = sampleWorkoutData,
                nutritionData = sampleNutritionData,
                stepData = sampleStepData,
                goalProgress = sampleGoalProgress,
                onPeriodChanged = { },
            )
        }
    }
}

@Preview(name = "Enhanced Progress Screen - Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewEnhancedProgressScreenDark() {
    MaterialTheme {
        Surface {
            EnhancedProgressScreen(
                workoutData = sampleWorkoutData,
                nutritionData = sampleNutritionData,
                stepData = sampleStepData,
                goalProgress = sampleGoalProgress,
                onPeriodChanged = { },
            )
        }
    }
}
