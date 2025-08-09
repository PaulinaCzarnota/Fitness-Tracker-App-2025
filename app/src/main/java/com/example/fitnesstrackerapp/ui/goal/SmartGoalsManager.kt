/**
 * SMART Goals Manager
 *
 * This file implements a comprehensive SMART goals system for fitness tracking:
 * - Specific: Clear goal definition with specific metrics
 * - Measurable: Quantifiable targets and progress tracking
 * - Achievable: Realistic goal setting with recommendations
 * - Relevant: Context-aware goals based on user activity
 * - Time-bound: Deadline-driven with progress notifications
 *
 * Features:
 * - Goal creation wizard with validation
 * - Progress percentage calculation
 * - Completion logic with achievement notifications
 * - Goal recommendations based on user history
 * - Progress tracking with milestone alerts
 */

package com.example.fitnesstrackerapp.ui.goal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.fitnesstrackerapp.data.entity.Goal
import com.example.fitnesstrackerapp.data.entity.GoalStatus
import com.example.fitnesstrackerapp.data.entity.GoalType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Data class for SMART goal creation
 */
data class SmartGoalData(
    val title: String = "",
    val description: String = "",
    val goalType: GoalType = GoalType.WEIGHT_LOSS,
    val targetValue: String = "",
    val unit: String = "kg",
    val targetDate: Date? = null,
    val reminderEnabled: Boolean = true,
    val reminderFrequency: String = "daily",
) {
    /**
     * Validates if the SMART goal data is complete and valid
     */
    fun isValid(): Boolean {
        return title.isNotBlank() &&
            targetValue.toDoubleOrNull() != null &&
            targetValue.toDouble() > 0 &&
            targetDate != null &&
            targetDate.after(Date()) &&
            unit.isNotBlank()
    }

    /**
     * Converts to Goal entity
     */
    fun toGoal(userId: Long): Goal {
        return Goal(
            userId = userId,
            title = title,
            description = description.takeIf { it.isNotBlank() },
            goalType = goalType,
            targetValue = targetValue.toDouble(),
            unit = unit,
            targetDate = targetDate ?: Date(),
            reminderEnabled = reminderEnabled,
            reminderFrequency = reminderFrequency,
        )
    }
}

/**
 * SMART Goals Screen with goal creation and management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartGoalsScreen(
    goals: List<Goal>,
    onCreateGoal: (SmartGoalData) -> Unit,
    onUpdateProgress: (Long, Double) -> Unit,
    onCompleteGoal: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        // Header
        Text(
            text = "SMART Goals",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Goals summary
        GoalsSummaryCard(goals = goals)

        Spacer(modifier = Modifier.height(16.dp))

        // Goals list
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(goals) { goal ->
                SmartGoalCard(
                    goal = goal,
                    onUpdateProgress = onUpdateProgress,
                    onCompleteGoal = onCompleteGoal,
                )
            }
        }

        // Add goal FAB
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            modifier = Modifier.align(Alignment.End),
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Goal")
        }

        // Create goal dialog
        if (showCreateDialog) {
            CreateSmartGoalDialog(
                onDismiss = { showCreateDialog = false },
                onCreateGoal = { goalData ->
                    onCreateGoal(goalData)
                    showCreateDialog = false
                },
            )
        }
    }
}

/**
 * Goals summary card showing overall progress
 */
@Composable
private fun GoalsSummaryCard(
    goals: List<Goal>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "Goals Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(modifier = Modifier.height(12.dp))

            val activeGoals = goals.count { it.status == GoalStatus.ACTIVE }
            val completedGoals = goals.count { it.status == GoalStatus.COMPLETED }
            val overdueGoals = goals.count { it.isOverdue() }
            val averageProgress = if (goals.isNotEmpty()) {
                goals.map { it.getProgressPercentage() }.average().toFloat()
            } else {
                0f
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                SummaryItem(
                    icon = Icons.Default.Flag,
                    label = "Active",
                    value = activeGoals.toString(),
                    color = MaterialTheme.colorScheme.primary,
                )

                SummaryItem(
                    icon = Icons.Default.Check,
                    label = "Completed",
                    value = completedGoals.toString(),
                    color = MaterialTheme.colorScheme.secondary,
                )

                SummaryItem(
                    icon = Icons.Default.Schedule,
                    label = "Overdue",
                    value = overdueGoals.toString(),
                    color = MaterialTheme.colorScheme.error,
                )

                SummaryItem(
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    label = "Avg Progress",
                    value = "${averageProgress.roundToInt()}%",
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Overall progress bar
            Text(
                text = "Overall Progress: ${averageProgress.roundToInt()}%",
                style = MaterialTheme.typography.bodyMedium,
            )
            LinearProgressIndicator(
                progress = { averageProgress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
            )
        }
    }
}

/**
 * Summary item for goals overview
 */
@Composable
private fun SummaryItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Individual SMART goal card with progress tracking
 */
@Composable
private fun SmartGoalCard(
    goal: Goal,
    onUpdateProgress: (Long, Double) -> Unit,
    onCompleteGoal: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showProgressDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            // Goal header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = goal.goalType.name.replace("_", " "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Status indicator
                val statusColor = when {
                    goal.isCompleted() -> MaterialTheme.colorScheme.secondary
                    goal.isOverdue() -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.primary
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = statusColor),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(
                        text = when {
                            goal.isCompleted() -> "Completed"
                            goal.isOverdue() -> "Overdue"
                            else -> "Active"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress section
            val progressPercentage = goal.getProgressPercentage()
            Text(
                text = "Progress: ${progressPercentage.roundToInt()}%",
                style = MaterialTheme.typography.bodyMedium,
            )

            LinearProgressIndicator(
                progress = { progressPercentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
            )

            Text(
                text = goal.getFormattedProgress(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Goal details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = "Target Date",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(goal.targetDate),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Days Remaining",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    val daysRemaining = goal.getDaysRemaining()
                    Text(
                        text = if (daysRemaining >= 0) "$daysRemaining days" else "Overdue",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (daysRemaining < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = { showProgressDialog = true },
                    modifier = Modifier.weight(1f),
                    enabled = !goal.isCompleted(),
                ) {
                    Text("Update Progress")
                }

                if (!goal.isCompleted() && goal.getProgressPercentage() >= 100f) {
                    Button(
                        onClick = { onCompleteGoal(goal.id) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Mark Complete")
                    }
                }
            }
        }
    }

    // Progress update dialog
    if (showProgressDialog) {
        UpdateProgressDialog(
            goal = goal,
            onDismiss = { showProgressDialog = false },
            onUpdateProgress = { newValue ->
                onUpdateProgress(goal.id, newValue)
                showProgressDialog = false
            },
        )
    }
}

/**
 * Dialog for creating a new SMART goal
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateSmartGoalDialog(
    onDismiss: () -> Unit,
    onCreateGoal: (SmartGoalData) -> Unit,
    modifier: Modifier = Modifier,
) {
    var goalData by remember { mutableStateOf(SmartGoalData()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var goalTypeExpanded by remember { mutableStateOf(false) }
    var reminderFrequencyExpanded by remember { mutableStateOf(false) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Create SMART Goal",
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Column {
                // Goal title
                OutlinedTextField(
                    value = goalData.title,
                    onValueChange = { goalData = goalData.copy(title = it) },
                    label = { Text("Goal Title") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., Lose 5kg for summer") },
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Goal description
                OutlinedTextField(
                    value = goalData.description,
                    onValueChange = { goalData = goalData.copy(description = it) },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Goal type dropdown
                ExposedDropdownMenuBox(
                    expanded = goalTypeExpanded,
                    onExpandedChange = { goalTypeExpanded = !goalTypeExpanded },
                ) {
                    OutlinedTextField(
                        value = goalData.goalType.name.replace("_", " "),
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Goal Type") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = goalTypeExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                    )

                    ExposedDropdownMenu(
                        expanded = goalTypeExpanded,
                        onDismissRequest = { goalTypeExpanded = false },
                    ) {
                        GoalType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name.replace("_", " ")) },
                                onClick = {
                                    goalData = goalData.copy(
                                        goalType = type,
                                        unit = getDefaultUnit(type),
                                    )
                                    goalTypeExpanded = false
                                },
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Target value and unit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = goalData.targetValue,
                        onValueChange = { goalData = goalData.copy(targetValue = it) },
                        label = { Text("Target Value") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(2f),
                    )

                    OutlinedTextField(
                        value = goalData.unit,
                        onValueChange = { goalData = goalData.copy(unit = it) },
                        label = { Text("Unit") },
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Target date
                OutlinedTextField(
                    value = goalData.targetDate?.let {
                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it)
                    } ?: "",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Target Date") },
                    trailingIcon = {
                        androidx.compose.material3.IconButton(
                            onClick = { showDatePicker = true },
                        ) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Reminder frequency
                ExposedDropdownMenuBox(
                    expanded = reminderFrequencyExpanded,
                    onExpandedChange = { reminderFrequencyExpanded = !reminderFrequencyExpanded },
                ) {
                    OutlinedTextField(
                        value = goalData.reminderFrequency,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Reminder Frequency") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = reminderFrequencyExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                    )

                    ExposedDropdownMenu(
                        expanded = reminderFrequencyExpanded,
                        onDismissRequest = { reminderFrequencyExpanded = false },
                    ) {
                        listOf("daily", "weekly", "monthly").forEach { frequency ->
                            DropdownMenuItem(
                                text = { Text(frequency.replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    goalData = goalData.copy(reminderFrequency = frequency)
                                    reminderFrequencyExpanded = false
                                },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreateGoal(goalData) },
                enabled = goalData.isValid(),
            ) {
                Text("Create Goal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )

    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialDisplayMode = DisplayMode.Picker,
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            goalData = goalData.copy(targetDate = Date(millis))
                        }
                        showDatePicker = false
                    },
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/**
 * Dialog for updating goal progress
 */
@Composable
private fun UpdateProgressDialog(
    goal: Goal,
    onDismiss: () -> Unit,
    onUpdateProgress: (Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    var currentValueText by remember { mutableStateOf(goal.currentValue.toString()) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Update Progress")
        },
        text = {
            Column {
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = currentValueText,
                    onValueChange = { currentValueText = it },
                    label = { Text("Current Value") },
                    suffix = { Text(goal.unit) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Target: ${goal.targetValue} ${goal.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                val newValue = currentValueText.toDoubleOrNull() ?: goal.currentValue
                val newProgress = if (goal.targetValue > 0) (newValue / goal.targetValue * 100).coerceIn(0.0, 100.0) else 0.0

                Text(
                    text = "New Progress: ${newProgress.roundToInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newValue = currentValueText.toDoubleOrNull()
                    if (newValue != null && newValue >= 0) {
                        onUpdateProgress(newValue)
                    }
                },
                enabled = currentValueText.toDoubleOrNull() != null && currentValueText.toDoubleOrNull()!! >= 0,
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

/**
 * Helper function to get default unit for goal type
 */
private fun getDefaultUnit(goalType: GoalType): String {
    return when (goalType) {
        GoalType.WEIGHT_LOSS, GoalType.WEIGHT_GAIN, GoalType.MUSCLE_BUILDING -> "kg"
        GoalType.DISTANCE_RUNNING -> "km"
        GoalType.WORKOUT_FREQUENCY -> "sessions"
        GoalType.CALORIE_BURN -> "calories"
        GoalType.STEP_COUNT -> "steps"
        GoalType.DURATION_EXERCISE -> "minutes"
        GoalType.STRENGTH_TRAINING -> "kg"
        GoalType.ENDURANCE -> "minutes"
        GoalType.FLEXIBILITY -> "sessions"
        GoalType.BODY_FAT -> "%"
        GoalType.HYDRATION -> "liters"
        GoalType.SLEEP -> "hours"
        GoalType.FITNESS -> "score"
        GoalType.OTHER -> "units"
    }
}

/**
 * Progress calculation utilities
 */
object GoalProgressCalculator {

    /**
     * Calculates progress percentage with completion logic
     */
    fun calculateProgress(goal: Goal): Float {
        return if (goal.targetValue > 0) {
            (goal.currentValue / goal.targetValue * 100).toFloat().coerceIn(0f, 100f)
        } else {
            0f
        }
    }

    /**
     * Determines if goal should be marked as completed
     */
    fun shouldMarkComplete(goal: Goal): Boolean {
        return goal.currentValue >= goal.targetValue && goal.status != GoalStatus.COMPLETED
    }

    /**
     * Calculates required daily progress to meet goal
     */
    fun calculateRequiredDailyProgress(goal: Goal): Double? {
        val daysRemaining = goal.getDaysRemaining()
        val remainingValue = goal.getRemainingValue()

        return if (daysRemaining > 0 && remainingValue > 0) {
            remainingValue / daysRemaining
        } else {
            null
        }
    }

    /**
     * Generates goal recommendations based on user activity
     */
    fun generateRecommendations(userGoals: List<Goal>, userActivity: Map<String, Any>): List<SmartGoalData> {
        val recommendations = mutableListOf<SmartGoalData>()

        // Weight loss recommendation if user has workout activity but no weight goal
        if (userGoals.none { it.goalType in listOf(GoalType.WEIGHT_LOSS, GoalType.WEIGHT_GAIN) }) {
            recommendations.add(
                SmartGoalData(
                    title = "Reach ideal weight",
                    goalType = GoalType.WEIGHT_LOSS,
                    targetValue = "5",
                    unit = "kg",
                    targetDate = Calendar.getInstance().apply { add(Calendar.MONTH, 3) }.time,
                ),
            )
        }

        // Step count goal if user tracks steps but has no step goal
        if (userGoals.none { it.goalType == GoalType.STEP_COUNT }) {
            recommendations.add(
                SmartGoalData(
                    title = "Daily step target",
                    goalType = GoalType.STEP_COUNT,
                    targetValue = "10000",
                    unit = "steps",
                    targetDate = Calendar.getInstance().apply { add(Calendar.MONTH, 1) }.time,
                ),
            )
        }

        return recommendations
    }
}
