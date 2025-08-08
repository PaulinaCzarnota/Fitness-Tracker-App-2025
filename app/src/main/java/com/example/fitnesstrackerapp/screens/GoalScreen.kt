package com.example.fitnesstrackerapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitnesstrackerapp.ViewModelFactoryProvider
import com.example.fitnesstrackerapp.data.entity.Goal
import com.example.fitnesstrackerapp.data.entity.GoalStatus
import com.example.fitnesstrackerapp.data.entity.GoalType
import com.example.fitnesstrackerapp.ui.goal.GoalViewModel
import java.util.Calendar
import java.util.Date

/**
 * Screen for displaying and managing fitness goals.
 *
 * @param modifier Modifier for styling.
 * @param activity The parent activity, used for ViewModel instantiation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalScreen(
    modifier: Modifier = Modifier,
    activity: androidx.activity.ComponentActivity
) {
    // Initialize ViewModel using ServiceLocator-backed factory
    val goalViewModel: GoalViewModel = ViewModelFactoryProvider.getGoalViewModel(activity)
    val uiState by goalViewModel.uiState.collectAsStateWithLifecycle()
    var showAddGoalDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "My Goals",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Add new goal button
        Button(
            onClick = { showAddGoalDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Goal")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add New Goal")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Goals section
        if (uiState.goals.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = "No goals icon",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No goals yet. Add your first goal!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn {
                items(uiState.goals, key = { it.id }) { goal ->
                    GoalItem(
                        goal = goal,
                        onDelete = { goalViewModel.deleteGoal(goal.id) },
                        onToggleComplete = { goalViewModel.achieveGoal(goal.id) }
                    )
                }
            }
        }
    }

    // Add Goal Dialog
    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onAddGoal = { title, description, targetValue, goalType ->
                // Create a proper Goal entity - TODO: Replace with actual user ID from auth
                val goal = Goal(
                    userId = 1L, // TODO: Get actual current user ID from authentication
                    title = title,
                    description = description.takeIf { it.isNotBlank() },
                    goalType = goalType,
                    targetValue = targetValue,
                    unit = getUnitForGoalType(goalType),
                    targetDate = getDefaultTargetDate()
                )
                goalViewModel.addGoal(goal)
                showAddGoalDialog = false
            }
        )
    }
}

/**
 * Individual goal item component.
 *
 * @param goal The goal entity to display.
 * @param onDelete Callback for deleting the goal.
 * @param onToggleComplete Callback for toggling goal completion.
 * @param modifier Modifier for styling.
 */
@Composable
private fun GoalItem(
    goal: Goal,
    onDelete: () -> Unit,
    onToggleComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                goal.description?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                // Goal progress info
                Text(
                    text = "Target: ${goal.targetValue} | Current: ${goal.currentValue}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
                // Goal status
                val statusColor = when (goal.status) {
                    GoalStatus.ACTIVE -> MaterialTheme.colorScheme.primary
                    GoalStatus.COMPLETED -> MaterialTheme.colorScheme.primary
                    GoalStatus.PAUSED -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                Text(
                    text = "Status: ${goal.status.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Column {
                // Toggle complete button
                IconButton(onClick = onToggleComplete) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = if (goal.status == GoalStatus.COMPLETED) "Mark as active" else "Mark as completed",
                        tint = if (goal.status == GoalStatus.COMPLETED) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                // Delete button
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete goal",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * Dialog for adding a new goal.
 *
 * @param onDismiss Callback when dialog is dismissed.
 * @param onAddGoal Callback when a new goal is added.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddGoalDialog(
    onDismiss: () -> Unit,
    onAddGoal: (String, String, Double, GoalType) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var targetValue by remember { mutableStateOf("") }
    var selectedGoalType by remember { mutableStateOf(GoalType.STEP_COUNT) }
    var expandedGoalType by remember { mutableStateOf(false) }
    var showInputError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Goal") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Title") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = showInputError && title.isBlank()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = targetValue,
                    onValueChange = { targetValue = it },
                    label = { Text("Target Value") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = showInputError && (targetValue.isBlank() || targetValue.toDoubleOrNull() == null)
                )
                if (showInputError && (title.isBlank() || targetValue.isBlank() || targetValue.toDoubleOrNull() == null)) {
                    Text(
                        text = "Please enter a valid title and numeric target value.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                // Goal Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedGoalType,
                    onExpandedChange = { expandedGoalType = !expandedGoalType }
                ) {
                    OutlinedTextField(
                        value = selectedGoalType.name,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Goal Type") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGoalType)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedGoalType,
                        onDismissRequest = { expandedGoalType = false }
                    ) {
                        GoalType.entries.forEach { goalType ->
                            DropdownMenuItem(
                                text = { Text(goalType.name) },
                                onClick = {
                                    selectedGoalType = goalType
                                    expandedGoalType = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank() && targetValue.isNotBlank() && targetValue.toDoubleOrNull() != null) {
                        onAddGoal(
                            title,
                            description,
                            targetValue.toDouble(),
                            selectedGoalType
                        )
                    } else {
                        showInputError = true
                    }
                },
                enabled = title.isNotBlank() && targetValue.isNotBlank() && targetValue.toDoubleOrNull() != null
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Helper function to get appropriate unit for goal type.
 *
 * @param goalType The type of goal.
 * @return The unit string for the goal type.
 */
private fun getUnitForGoalType(goalType: GoalType): String {
    return when (goalType) {
        GoalType.WEIGHT_LOSS, GoalType.WEIGHT_GAIN -> "kg"
        GoalType.DISTANCE_RUNNING -> "km"
        GoalType.WORKOUT_FREQUENCY -> "times"
        GoalType.CALORIE_BURN -> "kcal"
        GoalType.STEP_COUNT -> "steps"
        GoalType.DURATION_EXERCISE -> "minutes"
        GoalType.STRENGTH_TRAINING -> "reps"
        GoalType.OTHER -> "units"
    }
}

/**
 * Helper function to get default target date (30 days from now).
 *
 * @return Date 30 days from now.
 */
private fun getDefaultTargetDate(): Date {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_MONTH, 30)
    return calendar.time
}
