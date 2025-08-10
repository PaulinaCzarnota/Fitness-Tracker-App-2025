package com.example.fitnesstrackerapp.ui.workout

/**
 * Detailed Workout View Screen
 *
 * This screen provides comprehensive details of a single workout including:
 * - Complete workout information with metrics
 * - Visual charts and progress indicators
 * - Notes and custom data fields
 * - Exercise breakdown (for multi-exercise workouts)
 * - Performance analysis and comparisons
 * - Quick actions for editing and sharing
 */

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fitnesstrackerapp.data.entity.Workout
import com.example.fitnesstrackerapp.util.MetTableCalculator
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    workout: Workout,
    userWeight: Double = 70.0,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val dateFormat = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    // Calculate additional metrics
    val estimatedCaloriesFromMET = remember(workout, userWeight) {
        MetTableCalculator.calculateWorkoutCalories(
            workoutType = workout.workoutType,
            durationMinutes = workout.duration,
            weightKg = userWeight,
            avgHeartRate = workout.avgHeartRate,
        )
    }

    val caloriesPerMinute = remember(workout) {
        if (workout.duration > 0) {
            workout.caloriesBurned.toFloat() / workout.duration
        } else {
            0f
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        // Top app bar
        WorkoutDetailTopBar(
            workout = workout,
            onBack = onBack,
            onEdit = onEdit,
            onDelete = onDelete,
        )

        // Main content
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Workout header info
            WorkoutHeaderCard(
                workout = workout,
                dateFormat = dateFormat,
                timeFormat = timeFormat,
            )

            // Main statistics
            WorkoutStatsGrid(
                workout = workout,
                caloriesPerMinute = caloriesPerMinute,
            )

            // Performance metrics
            PerformanceMetricsCard(
                workout = workout,
                estimatedCaloriesFromMET = estimatedCaloriesFromMET,
            )

            // Heart rate and intensity (if available)
            if (workout.avgHeartRate != null || workout.maxHeartRate != null) {
                HeartRateCard(workout = workout)
            }

            // Notes section (if available)
            workout.notes?.let { notes ->
                if (notes.isNotBlank()) {
                    NotesCard(notes = notes)
                }
            }

            // Environmental conditions (if available)
            if (workout.weatherCondition != null || workout.temperature != null) {
                EnvironmentalCard(workout = workout)
            }

            // Workout analysis
            WorkoutAnalysisCard(
                workout = workout,
                userWeight = userWeight,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkoutDetailTopBar(
    workout: Workout,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = workout.title,
                maxLines = 1,
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit workout")
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete workout",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        },
    )
}

@Composable
private fun WorkoutHeaderCard(
    workout: Workout,
    dateFormat: SimpleDateFormat,
    timeFormat: SimpleDateFormat,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Workout type icon
            Icon(
                imageVector = getWorkoutIcon(workout.workoutType),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = workout.workoutType.name.replace("_", " "),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            Text(
                text = dateFormat.format(workout.startTime),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${timeFormat.format(workout.startTime)} - ${
                        workout.endTime?.let { timeFormat.format(it) } ?: "In Progress"
                    }",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun WorkoutStatsGrid(
    workout: Workout,
    caloriesPerMinute: Float,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Workout Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            // Primary stats in a 2x2 grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatItem(
                    label = "Duration",
                    value = workout.getFormattedDuration(),
                    icon = Icons.Default.Schedule,
                    modifier = Modifier.weight(1f),
                )

                StatItem(
                    label = "Calories",
                    value = "${workout.caloriesBurned}",
                    icon = Icons.Default.LocalFireDepartment,
                    iconTint = Color(0xFFFF5722),
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                if (workout.distance > 0) {
                    StatItem(
                        label = "Distance",
                        value = "${workout.distance} km",
                        icon = Icons.Default.Straighten,
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    StatItem(
                        label = "Calories/Min",
                        value = String.format("%.1f", caloriesPerMinute),
                        icon = Icons.Default.Speed,
                        modifier = Modifier.weight(1f),
                    )
                }

                StatItem(
                    label = "Intensity",
                    value = workout.getIntensity(),
                    icon = Icons.Default.TrendingUp,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.primary,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = iconTint,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PerformanceMetricsCard(
    workout: Workout,
    estimatedCaloriesFromMET: Int,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Performance Analysis",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            // Pace and speed (if distance available)
            if (workout.distance > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Average Pace",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = workout.getFormattedPace() ?: "N/A",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End,
                    ) {
                        Text(
                            text = "Average Speed",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = workout.getFormattedSpeed() ?: "N/A",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Calorie comparison
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Logged Calories",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${workout.caloriesBurned} cal",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "MET Estimate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "$estimatedCaloriesFromMET cal",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (Math.abs(workout.caloriesBurned - estimatedCaloriesFromMET) > 50) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                    )
                }
            }

            // Accuracy indicator
            val accuracy = if (estimatedCaloriesFromMET > 0) {
                val diff = Math.abs(workout.caloriesBurned - estimatedCaloriesFromMET)
                val percentage = (diff.toFloat() / estimatedCaloriesFromMET) * 100
                when {
                    percentage <= 10 -> "Excellent"
                    percentage <= 20 -> "Good"
                    percentage <= 30 -> "Fair"
                    else -> "Review Needed"
                }
            } else {
                "Unknown"
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.Analytics,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Accuracy: $accuracy",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun HeartRateCard(workout: Workout) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Heart Rate Data",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                workout.avgHeartRate?.let { avgHr ->
                    StatItem(
                        label = "Average HR",
                        value = "$avgHr bpm",
                        icon = Icons.Default.Favorite,
                        iconTint = Color(0xFFE91E63),
                        modifier = Modifier.weight(1f),
                    )
                }

                workout.maxHeartRate?.let { maxHr ->
                    StatItem(
                        label = "Max HR",
                        value = "$maxHr bpm",
                        icon = Icons.Default.MonitorHeart,
                        iconTint = Color(0xFFE91E63),
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // Heart rate zone estimation (if average HR available)
            workout.avgHeartRate?.let { avgHr ->
                Spacer(modifier = Modifier.height(8.dp))

                val zone = when {
                    avgHr < 100 -> "Zone 1 (Active Recovery)"
                    avgHr < 130 -> "Zone 2 (Aerobic Base)"
                    avgHr < 150 -> "Zone 3 (Aerobic Development)"
                    avgHr < 170 -> "Zone 4 (Lactate Threshold)"
                    else -> "Zone 5 (Neuromuscular Power)"
                }

                Text(
                    text = "Training Zone: $zone",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun NotesCard(notes: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Notes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            Text(
                text = notes,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EnvironmentalCard(workout: Workout) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Environmental Conditions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                workout.weatherCondition?.let { weather ->
                    Column {
                        Text(
                            text = "Weather",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = weather,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }

                workout.temperature?.let { temp ->
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Temperature",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "${temp.toInt()}°C",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutAnalysisCard(
    workout: Workout,
    @Suppress("UNUSED_PARAMETER") userWeight: Double,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Workout Analysis",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            // Efficiency score based on calories per minute
            val efficiency = workout.getCaloriesPerMinute()
            val efficiencyRating = when {
                efficiency >= 15 -> "Excellent"
                efficiency >= 10 -> "Very Good"
                efficiency >= 7 -> "Good"
                efficiency >= 4 -> "Moderate"
                else -> "Light"
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "Efficiency Rating",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = efficiencyRating,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = when (efficiencyRating) {
                            "Excellent" -> Color(0xFF4CAF50)
                            "Very Good" -> Color(0xFF8BC34A)
                            "Good" -> Color(0xFFFFEB3B)
                            "Moderate" -> Color(0xFFFF9800)
                            else -> Color(0xFF9E9E9E)
                        },
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Cal/Min",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = String.format("%.1f", efficiency),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Duration assessment
            val durationAssessment = when (workout.workoutType) {
                com.example.fitnesstrackerapp.data.entity.WorkoutType.RUNNING -> {
                    when {
                        workout.duration < 15 -> "Short run - good for interval training"
                        workout.duration < 30 -> "Moderate run - excellent for fitness"
                        workout.duration < 60 -> "Long run - great for endurance"
                        else -> "Ultra distance - impressive endurance workout"
                    }
                }
                com.example.fitnesstrackerapp.data.entity.WorkoutType.WEIGHTLIFTING -> {
                    when {
                        workout.duration < 30 -> "Quick session - focus on key exercises"
                        workout.duration < 60 -> "Standard session - balanced workout"
                        workout.duration < 90 -> "Extended session - comprehensive training"
                        else -> "Long session - detailed muscle work"
                    }
                }
                com.example.fitnesstrackerapp.data.entity.WorkoutType.CYCLING -> {
                    when {
                        workout.duration < 30 -> "Short ride - good for commuting"
                        workout.duration < 60 -> "Moderate ride - excellent cardio"
                        workout.duration < 120 -> "Long ride - great for endurance"
                        else -> "Epic ride - outstanding commitment"
                    }
                }
                else -> {
                    when {
                        workout.duration < 20 -> "Quick session - efficient use of time"
                        workout.duration < 45 -> "Standard session - good duration"
                        workout.duration < 90 -> "Extended session - thorough workout"
                        else -> "Long session - dedicated training"
                    }
                }
            }

            Text(
                text = durationAssessment,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
