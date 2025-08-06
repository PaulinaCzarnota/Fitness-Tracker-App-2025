/**
 * Settings Screen
 *
 * Responsibilities:
 * - Provides UI for app settings configuration
 * - Handles user preference updates
 * - Displays current settings values
 */
package com.example.fitnesstrackerapp.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fitnesstrackerapp.settings.MeasurementUnit
import com.example.fitnesstrackerapp.settings.SettingsManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsManager: SettingsManager,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val measurementUnit by settingsManager.measurementUnit.collectAsState(initial = MeasurementUnit.METRIC)
    val dailyCalorieTarget by settingsManager.dailyCalorieTarget.collectAsState(initial = SettingsManager.DEFAULT_CALORIE_TARGET)
    val workoutReminders by settingsManager.workoutReminders.collectAsState(initial = true)
    val notificationTime by settingsManager.notificationTime.collectAsState(initial = "09:00")

    var showTimePickerDialog by remember { mutableStateOf(false) }
    var showCalorieDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        item {
            SettingsSection(title = "Units") {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Measurement System")
                        Switch(
                            checked = measurementUnit == MeasurementUnit.METRIC,
                            onCheckedChange = { isMetric ->
                                scope.launch {
                                    settingsManager.updateMeasurementUnit(
                                        if (isMetric) MeasurementUnit.METRIC
                                        else MeasurementUnit.IMPERIAL
                                    )
                                }
                            }
                        )
                    }
                    Text(
                        text = if (measurementUnit == MeasurementUnit.METRIC)
                            "Using kilometers and kilograms"
                        else "Using miles and pounds",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            SettingsSection(title = "Goals") {
                Column {
                    OutlinedButton(
                        onClick = { showCalorieDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Daily Calorie Target: $dailyCalorieTarget kcal")
                    }
                }
            }
        }

        item {
            SettingsSection(title = "Notifications") {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Workout Reminders")
                        Switch(
                            checked = workoutReminders,
                            onCheckedChange = { enabled ->
                                scope.launch {
                                    settingsManager.updateWorkoutReminders(enabled)
                                }
                            }
                        )
                    }
                    if (workoutReminders) {
                        OutlinedButton(
                            onClick = { showTimePickerDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text("Reminder Time: $notificationTime")
                        }
                    }
                }
            }
        }
    }

    // Time Picker Dialog
    if (showTimePickerDialog) {
        TimePickerDialog(
            initialTime = notificationTime,
            onTimeSelected = { newTime ->
                scope.launch {
                    settingsManager.updateNotificationTime(newTime)
                }
                showTimePickerDialog = false
            },
            onDismiss = { showTimePickerDialog = false }
        )
    }

    // Calorie Target Dialog
    if (showCalorieDialog) {
        CalorieTargetDialog(
            currentTarget = dailyCalorieTarget,
            onTargetSelected = { newTarget ->
                scope.launch {
                    settingsManager.updateDailyCalorieTarget(newTarget)
                }
                showCalorieDialog = false
            },
            onDismiss = { showCalorieDialog = false }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initialTime: String,
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val initialHour = initialTime.split(":")[0].toInt()
    val initialMinute = initialTime.split(":")[1].toInt()

    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Reminder Time") },
        text = {
            TimePicker(state = timePickerState)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val hour = timePickerState.hour.toString().padStart(2, '0')
                    val minute = timePickerState.minute.toString().padStart(2, '0')
                    onTimeSelected("$hour:$minute")
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun CalorieTargetDialog(
    currentTarget: Int,
    onTargetSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var targetText by remember { mutableStateOf(currentTarget.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Daily Calorie Target") },
        text = {
            OutlinedTextField(
                value = targetText,
                onValueChange = { targetText = it.filter { char -> char.isDigit() } },
                label = { Text("Daily Calories") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    targetText.toIntOrNull()?.let { target ->
                        if (target in 1000..10000) {
                            onTargetSelected(target)
                        }
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
