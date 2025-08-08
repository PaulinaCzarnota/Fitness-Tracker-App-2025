package com.example.fitnesstrackerapp.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.fitnesstrackerapp.data.entity.Step
import com.example.fitnesstrackerapp.data.entity.Workout
import com.example.fitnesstrackerapp.ui.viewmodel.WorkoutSummary
import java.text.SimpleDateFormat
import java.util.*

/**
 * A card that displays summary information about workouts.
 *
 * @param title The title to display at the top of the card
 * @param summary The [WorkoutSummary] containing the data to display
 */
@Composable
fun SummaryCard(
    title: String,
    summary: WorkoutSummary,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            HorizontalDivider()

            // Display summary metrics
            SummaryMetric("Total Workouts", summary.totalWorkouts.toString())
            SummaryMetric("Total Calories", "${summary.totalCalories.toInt()} kcal")
            SummaryMetric("Avg. Duration", "${summary.averageDuration.toInt()} min")
            SummaryMetric("Total Distance", "%.1f km".format(summary.totalDistance))
        }
    }
}

/**
 * A reusable component for displaying a labeled metric in a summary card.
 *
 * @param label The label for the metric
 * @param value The value to display
 */
@Composable
private fun SummaryMetric(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

/**
 * A composable that displays a bar chart of weekly workout data.
 *
 * @param chartData A list of 7 values representing the data for each day of the week
 * @param modifier Modifier to be applied to the chart
 */
@Composable
fun WeeklyChart(
    chartData: List<Float>,
    modifier: Modifier = Modifier,
) {
    // Ensure we have exactly 7 days of data
    val data = if (chartData.size == 7) chartData else List(7) { 0f }
    val maxValue = (data.maxOrNull() ?: 0f).coerceAtLeast(1f)

    // Day labels for the x-axis
    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Weekly Activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            // Chart content
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                data.forEachIndexed { index, value ->
                    val heightRatio = if (maxValue > 0) value / maxValue else 0f

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f),
                    ) {
                        // Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height((140 * heightRatio).dp)
                                .background(MaterialTheme.colorScheme.primary),
                        )

                        // Day label
                        Text(
                            text = dayLabels[index],
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        // Optional: Show value on top of bar if there's enough space
                        if (heightRatio > 0.3f) {
                            Text(
                                text = value.toInt().toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 4.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * A list item that displays a workout entry.
 *
 * @param workout The workout to display
 * @param onClick Optional callback when the item is clicked
 */
@Composable
fun WorkoutItem(
    workout: Workout,
    onClick: (() -> Unit)? = null,
) {
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    SimpleDateFormat("h:mm a", Locale.getDefault())

    Card(
        onClick = { onClick?.invoke() },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = workout.workoutType.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = dateFormat.format(workout.startTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                MetricChip("${workout.duration} min", "Duration")
                MetricChip("${workout.caloriesBurned} kcal", "Calories")
                if (workout.distance > 0) {
                    MetricChip(
                        value = "%.1f km".format(workout.distance), // Fixed line
                        label = "Distance",
                    )
                }
            }

            if (!workout.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = workout.notes,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * A reusable chip component for displaying metrics.
 *
 * @param value The value to display (e.g., "30 min")
 * @param label The label for the value (e.g., "Duration")
 */
@Composable
private fun MetricChip(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * A list item that displays a step history entry.
 *
 * @param step The step entry to display
 * @param onClick Optional callback when the item is clicked
 */
@Composable
fun StepHistoryItem(
    step: Step,
    onClick: (() -> Unit)? = null,
) {
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = dateFormat.format(step.date),
            style = MaterialTheme.typography.bodyMedium,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "${step.count} steps",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )

            // Optional: Add an icon or visual indicator
            Icon(
                imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

/**
 * A composable that displays the workout progress for the week.
 *
 * @param workouts The list of workouts to display progress for
 * @param modifier Modifier to be applied to the progress component
 */
@Composable
fun WorkoutProgress(
    workouts: List<Workout>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.padding(16.dp)) {
        // Weekly calories
        val weeklyCalories = workouts
            .filter { workout -> isWorkoutThisWeek(workout.startTime) }
            .sumOf { it.caloriesBurned }

        // Weekly duration
        val weeklyDuration = workouts
            .filter { workout -> isWorkoutThisWeek(workout.startTime) }
            .sumOf { it.duration }

        // Workout count
        val workoutCount = workouts.count()

        ProgressMetric(
            value = weeklyCalories,
            label = "Weekly Calories",
            suffix = "kcal",
        )

        Spacer(modifier = Modifier.height(8.dp))

        ProgressMetric(
            value = weeklyDuration,
            label = "Weekly Duration",
            suffix = "min",
        )

        Spacer(modifier = Modifier.height(8.dp))

        ProgressMetric(
            value = workoutCount,
            label = "Total Workouts",
        )
    }
}

@Composable
private fun ProgressMetric(
    value: Number,
    label: String,
    suffix: String = "",
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "$value$suffix",
                style = MaterialTheme.typography.headlineMedium,
            )
        }
    }
}

private fun isWorkoutThisWeek(date: Date): Boolean {
    val calendar = Calendar.getInstance()
    val today = calendar.time
    calendar.add(Calendar.DAY_OF_YEAR, -7)
    val weekAgo = calendar.time
    return date in weekAgo..today
}
