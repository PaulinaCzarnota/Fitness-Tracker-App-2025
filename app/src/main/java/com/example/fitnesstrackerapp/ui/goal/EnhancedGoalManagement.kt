package com.example.fitnesstrackerapp.ui.goal

/**
 * Enhanced Goal Management System with WorkManager Integration
 *
 * This comprehensive goal management system provides:
 * - Create/edit/delete fitness goals with progress tracking
 * - Smart goal suggestions based on user activity
 * - WorkManager integration for scheduled reminders
 * - Progress visualization and achievement notifications
 * - Goal categories: steps, workouts, weight loss, muscle gain, etc.
 */

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Loyalty
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.fitnesstrackerapp.data.entity.Goal
import com.example.fitnesstrackerapp.data.entity.GoalType
import com.example.fitnesstrackerapp.data.entity.isCompleted
import com.example.fitnesstrackerapp.ui.progress.AnimatedCircularProgress
import com.example.fitnesstrackerapp.worker.GoalReminderWorker
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Goal categories with icons and descriptions
 */
enum class GoalCategory(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val defaultUnit: String,
    val suggestedTargets: List<Float>,
) {
    STEPS(
        Icons.Default.DirectionsWalk,
        "Daily Steps",
        "Track your daily walking goal",
        "steps",
        listOf(5000f, 8000f, 10000f, 12000f, 15000f),
    ),
    WORKOUTS(
        Icons.Default.FitnessCenter,
        "Weekly Workouts",
        "Set a weekly exercise frequency",
        "workouts",
        listOf(2f, 3f, 4f, 5f, 6f),
    ),
    WEIGHT_LOSS(
        Icons.Default.TrendingDown,
        "Weight Loss",
        "Target weight reduction goal",
        "kg",
        listOf(2f, 5f, 10f, 15f, 20f),
    ),
    MUSCLE_GAIN(
        Icons.Default.TrendingUp,
        "Muscle Gain",
        "Build lean muscle mass",
        "kg",
        listOf(2f, 5f, 8f, 10f, 15f),
    ),
    ENDURANCE(
        Icons.Default.Speed,
        "Endurance",
        "Improve cardio performance",
        "minutes",
        listOf(20f, 30f, 45f, 60f, 90f),
    ),
    STRENGTH(
        Icons.Default.Loyalty,
        "Strength",
        "Increase lifting capacity",
        "kg",
        listOf(50f, 75f, 100f, 125f, 150f),
    ),
    FLEXIBILITY(
        Icons.Default.SelfImprovement,
        "Flexibility",
        "Improve range of motion",
        "sessions",
        listOf(2f, 3f, 4f, 5f, 7f),
    ),
    CALORIES_BURNED(
        Icons.Default.LocalFireDepartment,
        "Calories Burned",
        "Daily calorie burn target",
        "kcal",
        listOf(300f, 500f, 700f, 1000f, 1200f),
    ),
}

/**
 * Enhanced goal management screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedGoalManagementScreen(
    goalViewModel: GoalViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val uiState by goalViewModel.uiState.collectAsStateWithLifecycle()

    var showCreateGoalDialog by remember { mutableStateOf(false) }
    var showGoalCategoryDialog by remember { mutableStateOf(false) }
    var selectedGoalForEdit by remember { mutableStateOf<Goal?>(null) }
    var selectedCategory by remember { mutableStateOf<GoalCategory?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Goal Management",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )

            Button(
                onClick = { showGoalCategoryDialog = true },
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("New Goal")
            }
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Active goals
            GoalsSection(
                title = "Active Goals",
                goals = uiState.goals.filter { !it.isCompleted() },
                onGoalClick = { selectedGoalForEdit = it },
                onToggleReminder = { goal, enabled ->
                    goalViewModel.toggleGoalReminder(goal.id, enabled)
                    if (enabled) {
                        scheduleGoalReminder(context, goal)
                    } else {
                        cancelGoalReminder(context, goal.id)
                    }
                },
                onMarkComplete = { goalViewModel.achieveGoal(it.id) },
                onUpdateProgress = { goal, progress ->
                    goalViewModel.updateGoalProgress(goal.id, progress)
                },
            )

            // Completed goals
            if (uiState.goals.any { it.isCompleted() }) {
                GoalsSection(
                    title = "Completed Goals",
                    goals = uiState.goals.filter { it.isCompleted() },
                    onGoalClick = { selectedGoalForEdit = it },
                    onToggleReminder = { _, _ -> },
                    onMarkComplete = { },
                    onUpdateProgress = { _, _ -> },
                )
            }

            // Quick stats
            GoalStatsCard(goals = uiState.goals)
        }
    }

    // Dialogs
    if (showGoalCategoryDialog) {
        GoalCategoryDialog(
            onDismiss = { showGoalCategoryDialog = false },
            onCategorySelected = { category ->
                selectedCategory = category
                showGoalCategoryDialog = false
                showCreateGoalDialog = true
            },
        )
    }

    if (showCreateGoalDialog && selectedCategory != null) {
        CreateGoalDialog(
            category = selectedCategory!!,
            onDismiss = {
                showCreateGoalDialog = false
                selectedCategory = null
            },
            onCreateGoal = { title, description, targetValue, targetDate, reminderEnabled ->
                goalViewModel.createGoal(
                    title = title,
                    description = description,
                    goalType = when (selectedCategory!!) {
                        GoalCategory.STEPS -> GoalType.STEP_COUNT
                        GoalCategory.WORKOUTS -> GoalType.WORKOUT_FREQUENCY
                        GoalCategory.WEIGHT_LOSS -> GoalType.WEIGHT_LOSS
                        GoalCategory.MUSCLE_GAIN -> GoalType.MUSCLE_BUILDING
                        GoalCategory.ENDURANCE -> GoalType.ENDURANCE
                        GoalCategory.STRENGTH -> GoalType.STRENGTH_TRAINING
                        GoalCategory.FLEXIBILITY -> GoalType.FLEXIBILITY
                        GoalCategory.CALORIES_BURNED -> GoalType.CALORIE_BURN
                    },
                    targetValue = targetValue,
                    unit = selectedCategory!!.defaultUnit,
                    targetDate = targetDate,
                    reminderEnabled = reminderEnabled,
                )
                showCreateGoalDialog = false
                selectedCategory = null
            },
        )
    }

    selectedGoalForEdit?.let { goal ->
        GoalDetailsDialog(
            goal = goal,
            onDismiss = { selectedGoalForEdit = null },
            onUpdateGoal = { updatedGoal ->
                goalViewModel.updateGoal(updatedGoal)
                selectedGoalForEdit = null
            },
            onDeleteGoal = {
                goalViewModel.deleteGoal(goal.id)
                selectedGoalForEdit = null
            },
        )
    }

    // Handle error and success messages
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show snackbar
            goalViewModel.clearMessages()
        }
    }

    uiState.successMessage?.let { message ->
        LaunchedEffect(message) {
            // Show snackbar
            goalViewModel.clearMessages()
        }
    }
}

/**
 * Goals section showing active or completed goals
 */
@Composable
fun GoalsSection(
    title: String,
    goals: List<Goal>,
    onGoalClick: (Goal) -> Unit,
    onToggleReminder: (Goal, Boolean) -> Unit,
    onMarkComplete: (Goal) -> Unit,
    onUpdateProgress: (Goal, Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )

        if (goals.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (title.contains("Active")) "No active goals yet" else "No completed goals",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.height(300.dp),
            ) {
                items(goals) { goal ->
                    GoalCard(
                        goal = goal,
                        onClick = { onGoalClick(goal) },
                        onToggleReminder = { enabled -> onToggleReminder(goal, enabled) },
                        onMarkComplete = { onMarkComplete(goal) },
                        onUpdateProgress = { progress -> onUpdateProgress(goal, progress) },
                    )
                }
            }
        }
    }
}

/**
 * Individual goal card with progress and actions
 */
@Composable
fun GoalCard(
    goal: Goal,
    onClick: () -> Unit,
    onToggleReminder: (Boolean) -> Unit,
    onMarkComplete: () -> Unit,
    onUpdateProgress: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val progress = (goal.currentValue / goal.targetValue).toFloat().coerceIn(0f, 1f)
    val isOverdue = !goal.isCompleted() && goal.targetDate.before(Date())

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                goal.isCompleted() -> MaterialTheme.colorScheme.tertiaryContainer
                isOverdue -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Goal header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )

                    if (goal.description?.isNotBlank() == true) {
                        Text(
                            text = goal.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    // Target date
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    Text(
                        text = "Target: ${dateFormat.format(goal.targetDate)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOverdue) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }

                // Status indicator
                when {
                    goal.isCompleted() -> {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    isOverdue -> {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Overdue",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }

            // Progress section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${goal.currentValue.toInt()} / ${goal.targetValue.toInt()} ${goal.unit}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        color = when {
                            goal.isCompleted() -> MaterialTheme.colorScheme.primary
                            isOverdue -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.secondary
                        },
                    )

                    Text(
                        text = "${(progress * 100).toInt()}% complete",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Circular progress for visual appeal
                Box(
                    modifier = Modifier.padding(start = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    AnimatedCircularProgress(
                        progress = progress,
                        title = "",
                        subtitle = "",
                        modifier = Modifier.size(60.dp),
                    )
                }
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Reminder toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        if (goal.reminderEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Switch(
                        checked = goal.reminderEnabled,
                        onCheckedChange = onToggleReminder,
                        modifier = Modifier.height(32.dp),
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Quick progress buttons (only for active goals)
                if (!goal.isCompleted() && !isOverdue) {
                    val quickIncrements = listOf(0.1f, 0.25f, 0.5f)
                    quickIncrements.forEach { increment ->
                        val incrementValue = goal.targetValue * increment
                        if (goal.currentValue + incrementValue <= goal.targetValue) {
                            FilledTonalButton(
                                onClick = {
                                    onUpdateProgress(goal.currentValue.toFloat() + incrementValue.toFloat())
                                },
                                modifier = Modifier.height(32.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            ) {
                                Text(
                                    "+${incrementValue.toInt()}",
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            }
                        }
                    }
                }

                // Mark complete button
                if (!goal.isCompleted() && progress >= 0.95f) {
                    Button(
                        onClick = onMarkComplete,
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Complete",
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Goal category selection dialog
 */
@Composable
fun GoalCategoryDialog(
    onDismiss: () -> Unit,
    onCategorySelected: (GoalCategory) -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Choose Goal Category",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 400.dp),
                ) {
                    items(GoalCategory.entries.toTypedArray()) { category ->
                        GoalCategoryItem(
                            category = category,
                            onClick = { onCategorySelected(category) },
                        )
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

/**
 * Goal category item
 */
@Composable
fun GoalCategoryItem(
    category: GoalCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    category.icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = category.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Icon(
                Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * Create goal dialog with smart suggestions
 */
@Composable
fun CreateGoalDialog(
    category: GoalCategory,
    onDismiss: () -> Unit,
    onCreateGoal: (String, String, Float, Date, Boolean) -> Unit,
) {
    var title by remember { mutableStateOf(category.title) }
    var description by remember { mutableStateOf("") }
    var targetValue by remember { mutableStateOf("") }
    var selectedSuggestion by remember { mutableStateOf<Float?>(null) }
    var targetDate by remember { mutableStateOf(Calendar.getInstance().apply { add(Calendar.MONTH, 1) }.time) }
    var reminderEnabled by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        category.icon,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "New ${category.title} Goal",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }

                // Goal title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Title") },
                    modifier = Modifier.fillMaxWidth(),
                )

                // Goal description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                )

                // Suggested targets
                Column {
                    Text(
                        text = "Suggested Targets:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(category.suggestedTargets) { suggestion ->
                            FilterChip(
                                onClick = {
                                    selectedSuggestion = suggestion
                                    targetValue = suggestion.toInt().toString()
                                },
                                label = { Text("${suggestion.toInt()} ${category.defaultUnit}") },
                                selected = selectedSuggestion == suggestion,
                            )
                        }
                    }
                }

                // Custom target input
                OutlinedTextField(
                    value = targetValue,
                    onValueChange = {
                        targetValue = it
                        selectedSuggestion = null
                    },
                    label = { Text("Target Value") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    trailingIcon = { Text(category.defaultUnit) },
                    modifier = Modifier.fillMaxWidth(),
                )

                // Target date (simplified)
                Text(
                    text = "Target Date: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(targetDate)}",
                    style = MaterialTheme.typography.bodyMedium,
                )

                // Reminder setting
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Enable Reminders",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { reminderEnabled = it },
                    )
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val target = targetValue.toFloatOrNull() ?: 0f
                            if (target > 0 && title.isNotBlank()) {
                                onCreateGoal(title, description, target, targetDate, reminderEnabled)
                            }
                        },
                        enabled = targetValue.isNotBlank() &&
                            title.isNotBlank() &&
                            (targetValue.toFloatOrNull() ?: 0f) > 0,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Create Goal")
                    }
                }
            }
        }
    }
}

/**
 * Goal details dialog for viewing and editing
 */
@Composable
fun GoalDetailsDialog(
    goal: Goal,
    onDismiss: () -> Unit,
    onUpdateGoal: (Goal) -> Unit,
    onDeleteGoal: () -> Unit,
) {
    var isEditing by remember { mutableStateOf(false) }
    var editedTitle by remember { mutableStateOf(goal.title) }
    var editedDescription by remember { mutableStateOf(goal.description ?: "") }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (isEditing) "Edit Goal" else "Goal Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )

                    Row {
                        IconButton(onClick = { isEditing = !isEditing }) {
                            Icon(
                                if (isEditing) Icons.Default.Cancel else Icons.Default.Edit,
                                contentDescription = if (isEditing) "Cancel" else "Edit",
                            )
                        }
                        IconButton(onClick = { showDeleteConfirmation = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }

                if (isEditing) {
                    // Edit mode
                    OutlinedTextField(
                        value = editedTitle,
                        onValueChange = { editedTitle = it },
                        label = { Text("Goal Title") },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    OutlinedTextField(
                        value = editedDescription,
                        onValueChange = { editedDescription = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                    )
                } else {
                    // View mode
                    Column {
                        Text(
                            text = goal.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        if (goal.description?.isNotBlank() == true) {
                            Text(
                                text = goal.description,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }

                // Goal progress
                Column {
                    Text(
                        text = "Progress",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                    )

                    val progress = (goal.currentValue / goal.targetValue).toFloat().coerceIn(0f, 1f)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${goal.currentValue.toInt()} / ${goal.targetValue.toInt()} ${goal.unit}",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }

                        AnimatedCircularProgress(
                            progress = progress,
                            title = "",
                            subtitle = "",
                            modifier = Modifier
                                .size(60.dp)
                                .padding(start = 16.dp),
                        )
                    }
                }

                // Goal details
                Column {
                    Text(
                        text = "Details",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                    )

                    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

                    InfoRow("Target Date", dateFormat.format(goal.targetDate))
                    InfoRow("Created", dateFormat.format(goal.createdAt))
                    InfoRow("Type", goal.goalType.name.replace("_", " "))
                    InfoRow("Status", if (goal.isCompleted()) "Completed" else "Active")
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (isEditing) {
                        TextButton(
                            onClick = { isEditing = false },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                val updatedGoal = goal.copy(
                                    title = editedTitle,
                                    description = editedDescription,
                                    updatedAt = Date(),
                                )
                                onUpdateGoal(updatedGoal)
                                isEditing = false
                            },
                            enabled = editedTitle.isNotBlank(),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Save")
                        }
                    } else {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Goal") },
            text = { Text("Are you sure you want to delete this goal? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteGoal()
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

/**
 * Information row component
 */
@Composable
fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
        )
    }
}

/**
 * Goal statistics card
 */
@Composable
fun GoalStatsCard(
    goals: List<Goal>,
    modifier: Modifier = Modifier,
) {
    val totalGoals = goals.size
    val completedGoals = goals.count { it.isCompleted() }
    val activeGoals = totalGoals - completedGoals
    val completionRate = if (totalGoals > 0) (completedGoals * 100) / totalGoals else 0

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Goal Statistics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatItem("Total", totalGoals.toString(), Icons.Default.Flag)
                StatItem("Active", activeGoals.toString(), Icons.Default.PlayArrow)
                StatItem("Completed", completedGoals.toString(), Icons.Default.CheckCircle)
                StatItem("Success Rate", "$completionRate%", Icons.Default.TrendingUp)
            }
        }
    }
}

/**
 * Statistic item component
 */
@Composable
fun StatItem(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Schedule goal reminder using WorkManager
 */
private fun scheduleGoalReminder(context: android.content.Context, goal: Goal) {
    val workRequest = PeriodicWorkRequestBuilder<GoalReminderWorker>(1, TimeUnit.DAYS)
        .setInitialDelay(1, TimeUnit.HOURS)
        .setInputData(
            workDataOf(
                "goalId" to goal.id,
                "goalTitle" to goal.title,
                "goalProgress" to (goal.currentValue / goal.targetValue * 100).toInt(),
            ),
        )
        .addTag("goal_reminder_${goal.id}")
        .build()

    WorkManager.getInstance(context)
        .enqueueUniquePeriodicWork(
            "goal_reminder_${goal.id}",
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest,
        )
}

/**
 * Cancel goal reminder
 */
private fun cancelGoalReminder(context: android.content.Context, goalId: Long) {
    WorkManager.getInstance(context)
        .cancelUniqueWork("goal_reminder_$goalId")
}
