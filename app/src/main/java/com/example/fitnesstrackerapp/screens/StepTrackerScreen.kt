package com.example.fitnesstrackerapp.screens

/**
 * Step tracking screen for the Fitness Tracker App.
 *
 * This comprehensive step tracking interface provides:
 * - Real-time step count display with sensor integration
 * - Daily goal progress tracking with visual progress indicators
 * - Distance and calorie estimation based on step count
 * - Achievement badges and motivational messages
 * - Historical step data and weekly progress summaries
 * - Step count reset functionality with confirmation dialogs
 *
 * The screen uses Material Design 3 principles for modern UI/UX and integrates
 * with the device's step detector sensor for accurate real-time tracking.
 */

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.fitnesstrackerapp.ViewModelFactoryProvider
import com.example.fitnesstrackerapp.ui.viewmodel.StepCounterViewModel

/**
 * Constants for step tracking
 */
private object StepTrackingConstants {
    const val STEP_UPDATE_ACTION = "com.example.fitnesstrackerapp.STEP_UPDATE"
    const val EXTRA_STEPS = "extra_steps"
    const val DEFAULT_DAILY_GOAL = 10000
    const val AVERAGE_STEP_LENGTH_METERS = 0.762 // Average step length in meters
    const val CALORIES_PER_STEP = 0.04 // Approximate calories burned per step
}

/**
 * Top-level composable for the enhanced step tracker screen.
 *
 * Features a comprehensive dashboard with:
 * - Circular progress indicator for daily goal
 * - Real-time step count with motivational messages
 * - Distance and calorie calculations
 * - Achievement milestones
 * - Historical data overview
 *
 * @param navController NavController for navigation between screens
 * @param modifier Modifier for styling the composable
 */
@SuppressLint("UnspecifiedRegisterReceiverFlag")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepTrackerScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    // Use LocalActivity for safe activity context acquisition
    val activity = LocalActivity.current
    val stepCounterViewModel: StepCounterViewModel = remember {
        ViewModelFactoryProvider.getStepCounterViewModel(activity as ComponentActivity, 1L) // Default user ID
    }

    // Collect step count and other state
    val stepCount by stepCounterViewModel.stepCount.collectAsStateWithLifecycle()
    var showResetDialog by remember { mutableStateOf(false) }

    // Calculate derived values
    val dailyGoal = StepTrackingConstants.DEFAULT_DAILY_GOAL
    val progressPercentage = (stepCount.toFloat() / dailyGoal).coerceIn(0f, 1f)
    val distanceKm = (stepCount * StepTrackingConstants.AVERAGE_STEP_LENGTH_METERS) / 1000
    val caloriesBurned = (stepCount * StepTrackingConstants.CALORIES_PER_STEP).toInt()

    // Lifecycle-aware BroadcastReceiver for step updates
    DisposableEffect(Unit) {
        val stepReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == StepTrackingConstants.STEP_UPDATE_ACTION) {
                    val steps = intent.getIntExtra(StepTrackingConstants.EXTRA_STEPS, 0)
                    stepCounterViewModel.updateStepCount(steps)
                }
            }
        }

        val filter = IntentFilter(StepTrackingConstants.STEP_UPDATE_ACTION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(stepReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(stepReceiver, filter)
        }

        onDispose {
            try {
                context.unregisterReceiver(stepReceiver)
            } catch (e: IllegalArgumentException) {
                // Receiver was not registered, ignore
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Step Tracker",
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
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            // Main step counter with circular progress
            StepCounterCard(
                stepCount = stepCount,
                progressPercentage = progressPercentage,
            )

            // Statistics row
            StepStatisticsRow(
                distanceKm = distanceKm,
                caloriesBurned = caloriesBurned,
                progressPercentage = progressPercentage,
            )

            // Action buttons
            ActionButtonsRow(
                onResetSteps = { showResetDialog = true },
                onStartTracking = {
                    // Start step tracking functionality
                    stepCounterViewModel.updateStepCount(0) // Initialize tracking
                },
            )

            // Achievement section
            AchievementSection(stepCount = stepCount)
        }
    }

    // Reset confirmation dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Step Count") },
            text = { Text("Are you sure you want to reset your step count? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        stepCounterViewModel.resetStepCount()
                        showResetDialog = false
                    },
                ) {
                    Text("Reset", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

/**
 * Main step counter card with circular progress indicator
 */
@Composable
private fun StepCounterCard(
    stepCount: Int,
    progressPercentage: Float,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(200.dp),
            ) {
                // Circular progress background
                Canvas(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    val strokeWidth = 16.dp.toPx()
                    drawCircle(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        radius = size.minDimension / 2 - strokeWidth / 2,
                        style = Stroke(width = strokeWidth),
                    )

                    // Progress arc
                    drawArc(
                        color = Color(0xFF4CAF50),
                        startAngle = -90f,
                        sweepAngle = 360f * progressPercentage,
                        useCenter = false,
                        style = Stroke(width = strokeWidth),
                    )
                }

                // Step count text
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stepCount.toString(),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Steps",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Goal: ${StepTrackingConstants.DEFAULT_DAILY_GOAL}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress text
            val progressPercent = (progressPercentage * 100).toInt()
            Text(
                text = "$progressPercent% of daily goal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )

            // Motivational message
            val motivationMessage = when {
                progressPercentage >= 1.0f -> "🎉 Goal achieved! Amazing work!"
                progressPercentage >= 0.8f -> "🔥 Almost there! Keep going!"
                progressPercentage >= 0.5f -> "💪 Great progress! You're halfway there!"
                progressPercentage >= 0.25f -> "🚀 Good start! Keep moving!"
                else -> "👟 Time to start walking!"
            }

            Text(
                text = motivationMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

/**
 * Statistics row showing distance and calories
 */
@SuppressLint("DefaultLocale")
@Composable
private fun StepStatisticsRow(
    distanceKm: Double,
    caloriesBurned: Int,
    progressPercentage: Float,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Distance card
        StatisticCard(
            modifier = Modifier.weight(1f),
            icon = Icons.AutoMirrored.Filled.DirectionsRun,
            title = "Distance",
            value = String.format("%.2f km", distanceKm),
            color = MaterialTheme.colorScheme.secondary,
        )

        // Calories card
        StatisticCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.LocalFireDepartment,
            title = "Calories",
            value = "$caloriesBurned kcal",
            color = MaterialTheme.colorScheme.tertiary,
        )
    }
}

/**
 * Individual statistic card component
 */
@Composable
private fun StatisticCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    value: String,
    color: Color,
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(32.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Action buttons row for reset and start tracking
 */
@Composable
private fun ActionButtonsRow(
    onResetSteps: () -> Unit,
    onStartTracking: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Reset button
        OutlinedButton(
            onClick = onResetSteps,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error,
            ),
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reset")
        }

        // Start tracking button
        Button(
            onClick = onStartTracking,
            modifier = Modifier.weight(1f),
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Start Tracking")
        }
    }
}

/**
 * Achievement section showing milestone badges
 */
@Composable
private fun AchievementSection(
    stepCount: Int,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "Achievements",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Achievement badges
            val achievements = listOf(
                Achievement("First Steps", "Take your first 100 steps", 100, Icons.Default.EmojiEvents),
                Achievement("Getting Started", "Walk 1,000 steps", 1000, Icons.Default.Star),
                Achievement("On Fire", "Reach 5,000 steps", 5000, Icons.Default.Whatshot),
                Achievement("Step Master", "Complete 10,000 steps", 10000, Icons.Default.WorkspacePremium),
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(achievements) { achievement ->
                    AchievementBadge(
                        achievement = achievement,
                        isUnlocked = stepCount >= achievement.requirement,
                    )
                }
            }
        }
    }
}

/**
 * Data class for achievements
 */
data class Achievement(
    val name: String,
    val description: String,
    val requirement: Int,
    val icon: ImageVector,
)

/**
 * Achievement badge component
 */
@Composable
private fun AchievementBadge(
    achievement: Achievement,
    isUnlocked: Boolean,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp),
    ) {
        Surface(
            shape = CircleShape,
            color = if (isUnlocked) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            modifier = Modifier.size(56.dp),
        ) {
            Icon(
                imageVector = achievement.icon,
                contentDescription = achievement.name,
                tint = if (isUnlocked) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                },
                modifier = Modifier
                    .size(32.dp)
                    .padding(12.dp),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = achievement.name,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            color = if (isUnlocked) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            },
        )
    }
}
