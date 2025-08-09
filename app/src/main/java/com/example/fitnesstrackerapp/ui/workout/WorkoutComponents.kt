/**
 * Workout Feature UI Components
 *
 * Material 3 implementation of workout-related UI components that provide:
 * - Responsive workout cards and lists
 * - Interactive exercise tracking components
 * - Progress indicators and charts
 * - Dark/light theme support
 * - Motion transitions and animations
 * - Accessibility semantics
 */

package com.example.fitnesstrackerapp.ui.workout

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fitnesstrackerapp.data.entity.Workout
import com.example.fitnesstrackerapp.data.entity.WorkoutType
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * Workout summary card with Material 3 design
 */
@Composable
fun WorkoutSummaryCard(
    workout: Workout,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Workout: ${workout.name}" },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = workout.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                
                WorkoutTypeChip(workout.type)
            }
            
            // Duration and calories row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WorkoutMetric(
                    icon = Icons.Default.Timer,
                    label = "Duration",
                    value = "${workout.durationMinutes}m"
                )
                
                WorkoutMetric(
                    icon = Icons.Default.FitnessCenter,
                    label = "Calories",
                    value = "${workout.caloriesBurned ?: 0}"
                )
            }
            
            // Progress indicator for workout completion
            workout.completionPercentage?.let { percentage ->
                WorkoutProgressIndicator(
                    progress = percentage / 100f,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Workout type chip component
 */
@Composable
fun WorkoutTypeChip(
    workoutType: WorkoutType,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (workoutType) {
        WorkoutType.CARDIO -> MaterialTheme.colorScheme.primaryContainer
        WorkoutType.STRENGTH -> MaterialTheme.colorScheme.secondaryContainer
        WorkoutType.FLEXIBILITY -> MaterialTheme.colorScheme.tertiaryContainer
        WorkoutType.SPORTS -> MaterialTheme.colorScheme.errorContainer
    }
    
    val textColor = when (workoutType) {
        WorkoutType.CARDIO -> MaterialTheme.colorScheme.onPrimaryContainer
        WorkoutType.STRENGTH -> MaterialTheme.colorScheme.onSecondaryContainer
        WorkoutType.FLEXIBILITY -> MaterialTheme.colorScheme.onTertiaryContainer
        WorkoutType.SPORTS -> MaterialTheme.colorScheme.onErrorContainer
    }
    
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .semantics { contentDescription = "Workout type: ${workoutType.name}" },
        color = backgroundColor
    ) {
        Text(
            text = workoutType.name.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

/**
 * Workout metric display component
 */
@Composable
private fun WorkoutMetric(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Animated workout progress indicator
 */
@Composable
fun WorkoutProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "Progress Animation"
    )
    
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Progress",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        LinearProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )
    }
}

/**
 * Workout session timer with circular progress
 */
@Composable
fun WorkoutTimer(
    elapsedSeconds: Int,
    isRunning: Boolean,
    onStartStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Circular timer display
        Box(
            modifier = Modifier
                .size(200.dp)
                .semantics { contentDescription = "Workout timer: ${formatTime(elapsedSeconds)}" },
            contentAlignment = Alignment.Center
        ) {
            // Background circle
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color.Gray.copy(alpha = 0.2f),
                    radius = size.minDimension / 2 - 16.dp.toPx(),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8.dp.toPx())
                )
                
                // Progress arc (optional - could show workout progress)
                val progress = (elapsedSeconds % 3600) / 3600f // Shows progress per hour
                drawArc(
                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 8.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )
            }
            
            // Time display
            Text(
                text = formatTime(elapsedSeconds),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        // Start/Stop button
        FloatingActionButton(
            onClick = onStartStop,
            containerColor = if (isRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = if (isRunning) "Stop workout" else "Start workout"
            )
        }
    }
}

/**
 * Workout statistics chart
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkoutStatsChart(
    workouts: List<Workout>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Weekly Stats",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            
            // Simple bar chart using Canvas
            WorkoutBarChart(
                data = workouts.map { it.durationMinutes.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
            
            // Workout type distribution
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                workouts.groupBy { it.type }.forEach { (type, workoutsOfType) ->
                    WorkoutTypeChip(workoutType = type)
                    Text(
                        text = "${workoutsOfType.size}",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Simple bar chart using Canvas
 */
@Composable
private fun WorkoutBarChart(
    data: List<Float>,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    
    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas
        
        val maxValue = data.maxOrNull() ?: 1f
        val barWidth = size.width / data.size * 0.7f
        val spacing = size.width / data.size * 0.3f
        
        data.forEachIndexed { index, value ->
            val barHeight = (value / maxValue) * size.height * 0.8f
            val x = index * (barWidth + spacing) + spacing / 2
            val y = size.height - barHeight
            
            // Draw bar
            drawRect(
                color = primary,
                topLeft = Offset(x, y),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
            )
            
            // Draw value label
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    value.toInt().toString(),
                    x + barWidth / 2,
                    y - 10,
                    android.graphics.Paint().apply {
                        color = onSurface.value.toInt()
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = 24f
                    }
                )
            }
        }
    }
}

/**
 * Formats seconds into HH:MM:SS format
 */
private fun formatTime(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format("%02d:%02d", minutes, secs)
    }
}

/**
 * Workout list with animated items
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AnimatedWorkoutList(
    workouts: List<Workout>,
    onWorkoutClick: (Workout) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(
            items = workouts,
            key = { _, workout -> workout.id }
        ) { index, workout ->
            // Staggered animation for list items
            var visible by remember { mutableStateOf(false) }
            
            LaunchedEffect(Unit) {
                delay(index * 50L) // Stagger animation
                visible = true
            }
            
            AnimatedVisibility(
                visible = visible,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = spring(stiffness = Spring.StiffnessLow)
                ) + fadeIn(),
                exit = slideOutVertically() + fadeOut(),
                modifier = Modifier.animateItemPlacement()
            ) {
                WorkoutSummaryCard(
                    workout = workout,
                    onClick = { onWorkoutClick(workout) }
                )
            }
        }
    }
}
