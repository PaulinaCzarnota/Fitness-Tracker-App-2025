/**
 * Goal Progress Charts
 *
 * Responsibilities:
 * - Visualizes goal progress using charts and graphs
 * - Displays progress trends over time
 * - Provides interactive data visualization
 */
package com.example.fitnesstrackerapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.fitnesstrackerapp.data.entity.FitnessGoal
import com.example.fitnesstrackerapp.data.entity.WorkoutType
import kotlin.math.min

/**
 * Chart showing the progress for a single goal as a ring chart.
 */
@Composable
fun GoalProgressChart(
    goal: FitnessGoal,
    progressPercentage: Float,
    modifier: Modifier = Modifier
) {
    // Get colors outside of Canvas context
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    
    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val circleRadius = min(canvasWidth, canvasHeight) / 2
            val strokeWidth = 20f
            val center = Offset(canvasWidth / 2, canvasHeight / 2)

            // Draw background circle
            drawCircle(
                color = surfaceVariantColor,
                radius = circleRadius,
                center = center,
                style = Stroke(width = strokeWidth)
            )

            // Draw progress arc
            val sweepAngle = 360f * (progressPercentage / 100f)
            drawArc(
                color = when {
                    progressPercentage >= 100f -> primaryColor
                    progressPercentage >= 75f -> secondaryColor
                    progressPercentage >= 50f -> tertiaryColor
                    else -> primaryColor
                },
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(
                    center.x - circleRadius,
                    center.y - circleRadius
                ),
                size = Size(circleRadius * 2, circleRadius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Goal information
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "${progressPercentage.toInt()}%",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "${goal.type.name} Goal",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LinearProgressIndicator(
                progress = { progressPercentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Text(
                text = "Current: ${goal.currentValue}/${goal.targetValue}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Simple stat for one entry in the workout type trend display.
 */
@Composable
private fun WorkoutTrendStat(type: WorkoutType, count: Int, maxCount: Int) {
    val progress = if (maxCount > 0) count.toFloat() / maxCount.toFloat() else 0f
    Column {
        Text(type.name, style = MaterialTheme.typography.bodyMedium)
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )
        Text(
            text = "$count sessions",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun WorkoutTrendChart(
    workoutCounts: Map<WorkoutType, Int>,
    modifier: Modifier = Modifier
) {
    val maxCount = workoutCounts.values.maxOrNull() ?: 0
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Workout Distribution",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        for ((type, count) in workoutCounts) {
            WorkoutTrendStat(type, count, maxCount)
        }
    }
}

/**
 * Label for one bar in the calories burned chart.
 */
@Composable
private fun CaloriesBarLabel(day: String) {
    Text(text = day, style = MaterialTheme.typography.bodySmall)
}

/**
 * Bar chart of calories burned per day.
 */
@Composable
fun CaloriesBurnedChart(
    dailyCalories: List<Pair<String, Int>>,
    modifier: Modifier = Modifier
) {
    val maxCalories = dailyCalories.maxOfOrNull { it.second } ?: 0
    val primaryColor = MaterialTheme.colorScheme.primary
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Calories Burned (Last 7 Days)",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val barWidth = if (dailyCalories.isNotEmpty()) canvasWidth / (dailyCalories.size * 2) else 0f
            for (index in dailyCalories.indices) {
                val (_, calories) = dailyCalories[index]
                val barHeight = if (maxCalories > 0) (calories.toFloat() / maxCalories) * canvasHeight else 0f
                val x = (index * 2 + 1) * barWidth
                drawRect(
                    color = primaryColor,
                    topLeft = Offset(x, canvasHeight - barHeight),
                    size = Size(barWidth, barHeight)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for ((day, _) in dailyCalories) {
                CaloriesBarLabel(day)
            }
        }
    }
}

/**
 * Progress display for a single goal using a progress bar and label.
 */
@Composable
fun GoalProgressDisplay(
    goal: FitnessGoal,
    modifier: Modifier = Modifier
) {
    val progress = (goal.currentValue / goal.targetValue * 100).coerceIn(0.0, 100.0).toFloat()
    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = goal.title,
            style = MaterialTheme.typography.titleMedium
        )
        LinearProgressIndicator(
            progress = { progress / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("${progress.toInt()}% Complete")
            Text("Target: ${goal.targetValue}")
        }
    }
}

/**
 * Composable for displaying the chart and info for a goal.
 */
@Composable
fun ShowGoalProgress(goal: FitnessGoal) {
    val pct = (goal.currentValue / goal.targetValue * 100).coerceIn(0.0, 100.0).toFloat()
    GoalProgressChart(goal = goal, progressPercentage = pct)
}

@Composable
fun DynamicGoalProgressSection(goals: List<FitnessGoal>) {
    Column {
        for (goal in goals) {
            ShowGoalProgress(goal)
        }
    }
}

@Composable
fun GoalChartsScreen(goals: List<FitnessGoal>) {
    Column {
        for (goal in goals) {
            ShowGoalProgress(goal)
        }
    }
}
