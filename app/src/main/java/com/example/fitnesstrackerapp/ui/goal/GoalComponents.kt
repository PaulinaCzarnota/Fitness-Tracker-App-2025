package com.example.fitnesstrackerapp.ui.goal

/**
 * Goal Management UI Components
 *
 * Material 3 implementation of goal-related UI components that provide:
 * - Interactive goal creation and editing
 * - Progress tracking and visualization
 * - Achievement badges and milestones
 * - Responsive layouts and animations
 * - Dark/light theme support
 * - Accessibility semantics
 */

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fitnesstrackerapp.data.entity.Goal
import com.example.fitnesstrackerapp.data.entity.GoalStatus
import com.example.fitnesstrackerapp.data.entity.GoalType
import kotlinx.coroutines.delay
import java.time.LocalDate

/**
 * Data class for goal progress information
 */
data class GoalProgress(
    val current: Float,
    val target: Float,
    val unit: String,
    val progressPercentage: Float = if (target > 0) (current / target).coerceAtMost(1f) else 0f,
)

/**
 * Data class for achievement information
 */
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isUnlocked: Boolean = false,
    val unlockedDate: LocalDate? = null,
)

/**
 * Goal summary card with progress visualization
 */
@Composable
fun GoalSummaryCard(
    goal: Goal,
    progress: GoalProgress,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit = {},
    onClick: () -> Unit = {},
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Goal: ${goal.title}, " +
                    "${(progress.progressPercentage * 100).toInt()}% complete"
            },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header row with title and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    goal.description?.takeIf { it.isNotEmpty() }?.let { description ->
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    GoalStatusBadge(goal.status)
                    GoalTypeIcon(goal.goalType)
                }
            }

            // Progress section
            GoalProgressSection(
                progress = progress,
                modifier = Modifier.fillMaxWidth(),
            )

            // Timeline information
            GoalTimelineInfo(
                startDate = goal.startDate,
                targetDate = goal.targetDate,
                status = goal.status,
            )

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit")
                }

                FilledTonalButton(
                    onClick = onClick,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("View Details")
                }
            }
        }
    }
}

/**
 * Goal status badge component
 */
@Composable
fun GoalStatusBadge(
    status: GoalStatus,
    modifier: Modifier = Modifier,
) {
    val (backgroundColor, textColor, text) = when (status) {
        GoalStatus.ACTIVE -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "Active",
        )
        GoalStatus.COMPLETED -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            "Completed",
        )
        GoalStatus.PAUSED -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            "Paused",
        )
        GoalStatus.CANCELLED -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "Cancelled",
        )
        GoalStatus.OVERDUE -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "Overdue",
        )
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .semantics { contentDescription = "Status: $text" },
        color = backgroundColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (status == GoalStatus.COMPLETED) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = textColor,
                )
            }

            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = textColor,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

/**
 * Goal type icon component
 */
@Composable
fun GoalTypeIcon(
    type: GoalType,
    modifier: Modifier = Modifier,
) {
    val (icon, backgroundColor) = when (type) {
        GoalType.WEIGHT_LOSS -> Icons.Default.MonitorWeight to MaterialTheme.colorScheme.primaryContainer
        GoalType.WEIGHT_GAIN -> Icons.Default.MonitorWeight to MaterialTheme.colorScheme.secondaryContainer
        GoalType.DISTANCE_RUNNING -> Icons.Default.DirectionsRun to MaterialTheme.colorScheme.tertiaryContainer
        GoalType.WORKOUT_FREQUENCY -> Icons.Default.FitnessCenter to MaterialTheme.colorScheme.primaryContainer
        GoalType.CALORIE_BURN -> Icons.Default.LocalFireDepartment to MaterialTheme.colorScheme.errorContainer
        GoalType.STEP_COUNT -> Icons.Default.DirectionsWalk to MaterialTheme.colorScheme.secondaryContainer
        GoalType.DURATION_EXERCISE -> Icons.Default.Timer to MaterialTheme.colorScheme.tertiaryContainer
        GoalType.STRENGTH_TRAINING -> Icons.Default.FitnessCenter to MaterialTheme.colorScheme.primaryContainer
        GoalType.MUSCLE_BUILDING -> Icons.Default.FitnessCenter to MaterialTheme.colorScheme.secondaryContainer
        GoalType.ENDURANCE -> Icons.Default.DirectionsRun to MaterialTheme.colorScheme.tertiaryContainer
        GoalType.FLEXIBILITY -> Icons.Default.SelfImprovement to MaterialTheme.colorScheme.primaryContainer
        GoalType.BODY_FAT -> Icons.Default.Analytics to MaterialTheme.colorScheme.errorContainer
        GoalType.HYDRATION -> Icons.Default.WaterDrop to MaterialTheme.colorScheme.primaryContainer
        GoalType.SLEEP -> Icons.Default.Bedtime to MaterialTheme.colorScheme.secondaryContainer
        GoalType.FITNESS -> Icons.Default.FitnessCenter to MaterialTheme.colorScheme.tertiaryContainer
        GoalType.OTHER -> Icons.Default.Star to MaterialTheme.colorScheme.surfaceVariant
    }

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .semantics { contentDescription = "Goal type: ${type.name}" },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Goal progress section with animated indicators
 */
@Composable
fun GoalProgressSection(
    progress: GoalProgress,
    modifier: Modifier = Modifier,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.progressPercentage,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "Goal Progress Animation",
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Progress values row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Progress",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = "${progress.current.toInt()} / ${progress.target.toInt()} ${progress.unit}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        // Progress bar
        LinearProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round,
        )

        // Percentage indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

/**
 * Goal timeline information
 */
@Composable
fun GoalTimelineInfo(
    startDate: java.util.Date,
    targetDate: java.util.Date,
    status: GoalStatus,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Start date
        TimelineItem(
            icon = Icons.Default.Flag,
            label = "Started",
            value = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault()).format(startDate),
            modifier = Modifier.weight(1f),
        )

        // Target date
        TimelineItem(
            icon = Icons.Default.CalendarToday,
            label = "Target",
            value = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault()).format(targetDate),
            modifier = Modifier.weight(1f),
        )

        // Days remaining
        if (status != GoalStatus.COMPLETED) {
            val daysRemaining = ((targetDate.time - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()
            TimelineItem(
                icon = Icons.Default.Timer,
                label = "Remaining",
                value = "${daysRemaining}d",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * Individual timeline item
 */
@Composable
private fun TimelineItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.semantics { contentDescription = "$label: $value" },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * Achievement badge component
 */
@Composable
fun AchievementBadge(
    achievement: Achievement,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    var showAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(achievement.isUnlocked) {
        if (achievement.isUnlocked) {
            delay(200)
            showAnimation = true
        }
    }

    Card(
        onClick = onClick,
        modifier = modifier
            .size(120.dp)
            .semantics {
                contentDescription = if (achievement.isUnlocked) {
                    "Achievement unlocked: ${achievement.title}"
                } else {
                    "Locked achievement: ${achievement.title}"
                }
            },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (achievement.isUnlocked) 4.dp else 1.dp,
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.isUnlocked) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            AnimatedVisibility(
                visible = showAnimation && achievement.isUnlocked,
                enter = scaleIn(spring(stiffness = Spring.StiffnessLow)) + fadeIn(),
                exit = scaleOut() + fadeOut(),
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.Yellow.copy(alpha = 0.3f),
                                    Color.Transparent,
                                ),
                                radius = 48f,
                            ),
                        )
                        .clip(CircleShape),
                )
            }

            Icon(
                imageVector = achievement.icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (achievement.isUnlocked) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                },
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = achievement.title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = if (achievement.isUnlocked) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                },
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
        }
    }
}

/**
 * Achievements grid section
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AchievementsGrid(
    achievements: List<Achievement>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Achievements",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                achievements.forEach { achievement ->
                    AchievementBadge(
                        achievement = achievement,
                        onClick = { /* Handle achievement click */ },
                    )
                }
            }
        }
    }
}

/**
 * Goal creation/editing FAB
 */
@Composable
fun GoalCreationFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.semantics { contentDescription = "Create new goal" },
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Create Goal",
        )
    }
}
