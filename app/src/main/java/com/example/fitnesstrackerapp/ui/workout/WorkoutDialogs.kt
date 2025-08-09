/**
 * Workout Management Dialog Components
 *
 * This file contains all the dialog components needed for workout management:
 * - CreateWorkoutDialog: Create new workouts with MET-based calorie calculation
 * - EditWorkoutDialog: Edit existing workouts with live calorie preview
 * - DeleteWorkoutDialog: Confirmation dialog for workout deletion
 * - Utility components for workout type selection and intensity settings
 */

package com.example.fitnesstrackerapp.ui.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.fitnesstrackerapp.data.entity.Workout
import com.example.fitnesstrackerapp.data.entity.WorkoutType
import com.example.fitnesstrackerapp.util.MetTableCalculator
import com.example.fitnesstrackerapp.util.WorkoutIntensity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateWorkoutDialog(
    userWeight: Double,
    onDismiss: () -> Unit,
    onCreate: (WorkoutData) -> Unit,
) {
    var workoutType by remember { mutableStateOf(WorkoutType.RUNNING) }
    var title by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }
    var intensity by remember { mutableStateOf(WorkoutIntensity.MODERATE) }
    var notes by remember { mutableStateOf("") }
    var avgHeartRate by remember { mutableStateOf("") }
    var maxHeartRate by remember { mutableStateOf("") }
    var showWorkoutTypeDialog by remember { mutableStateOf(false) }
    var showIntensityDialog by remember { mutableStateOf(false) }

    // Generate title based on workout type and duration
    LaunchedEffect(workoutType, duration) {
        if (title.isEmpty() || title.matches(Regex("^[A-Z_]+( - \\d+ min)?\$"))) {
            val durationText = if (duration.isNotBlank()) " - $duration min" else ""
            title = "${workoutType.name.replace("_", " ")}$durationText"
        }
    }

    // Calculate estimated calories
    val estimatedCalories = remember(workoutType, duration, distance, intensity, avgHeartRate) {
        val durationInt = duration.toIntOrNull() ?: 0
        val distanceFloat = distance.toFloatOrNull()
        val heartRate = avgHeartRate.toIntOrNull()

        if (durationInt > 0) {
            MetTableCalculator.calculateWorkoutCalories(
                workoutType = workoutType,
                durationMinutes = durationInt,
                weightKg = userWeight,
                intensity = intensity,
                distance = distanceFloat,
                avgHeartRate = heartRate,
            )
        } else {
            0
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                // Header
                Text(
                    text = "Create Workout",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Workout Type Selection
                OutlinedTextField(
                    value = workoutType.name.replace("_", " "),
                    onValueChange = { },
                    label = { Text("Workout Type") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showWorkoutTypeDialog = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select workout type")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Workout Title") },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Duration and Distance Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = duration,
                        onValueChange = { duration = it },
                        label = { Text("Duration (min)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )

                    OutlinedTextField(
                        value = distance,
                        onValueChange = { distance = it },
                        label = { Text("Distance (km)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Intensity Selection
                OutlinedTextField(
                    value = intensity.name.replace("_", " "),
                    onValueChange = { },
                    label = { Text("Intensity") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showIntensityDialog = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select intensity")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Heart Rate Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = avgHeartRate,
                        onValueChange = { avgHeartRate = it },
                        label = { Text("Avg HR (bpm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )

                    OutlinedTextField(
                        value = maxHeartRate,
                        onValueChange = { maxHeartRate = it },
                        label = { Text("Max HR (bpm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Calorie Estimate
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = Color(0xFFFF5722),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Estimated Calories",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        Text(
                            text = "$estimatedCalories cal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF5722),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (title.isNotBlank() && duration.isNotBlank()) {
                                onCreate(
                                    WorkoutData(
                                        workoutType = workoutType,
                                        title = title,
                                        duration = duration.toIntOrNull() ?: 0,
                                        distance = distance.toFloatOrNull() ?: 0f,
                                        intensity = intensity,
                                        notes = notes,
                                        avgHeartRate = avgHeartRate.toIntOrNull(),
                                        maxHeartRate = maxHeartRate.toIntOrNull(),
                                    ),
                                )
                            }
                        },
                        enabled = title.isNotBlank() && duration.isNotBlank(),
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }

    // Workout type selection dialog
    if (showWorkoutTypeDialog) {
        WorkoutTypeSelectionDialog(
            currentType = workoutType,
            onDismiss = { showWorkoutTypeDialog = false },
            onSelect = { selectedType ->
                workoutType = selectedType
                showWorkoutTypeDialog = false
            },
        )
    }

    // Intensity selection dialog
    if (showIntensityDialog) {
        IntensitySelectionDialog(
            currentIntensity = intensity,
            onDismiss = { showIntensityDialog = false },
            onSelect = { selectedIntensity ->
                intensity = selectedIntensity
                showIntensityDialog = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditWorkoutDialog(
    workout: Workout,
    userWeight: Double,
    onDismiss: () -> Unit,
    onUpdate: (Workout) -> Unit,
) {
    var workoutType by remember { mutableStateOf(workout.workoutType) }
    var title by remember { mutableStateOf(workout.title) }
    var duration by remember { mutableStateOf(workout.duration.toString()) }
    var distance by remember { mutableStateOf(if (workout.distance > 0) workout.distance.toString() else "") }
    var notes by remember { mutableStateOf(workout.notes ?: "") }
    var avgHeartRate by remember { mutableStateOf(workout.avgHeartRate?.toString() ?: "") }
    var maxHeartRate by remember { mutableStateOf(workout.maxHeartRate?.toString() ?: "") }
    var showWorkoutTypeDialog by remember { mutableStateOf(false) }
    var showIntensityDialog by remember { mutableStateOf(false) }
    var intensity by remember { mutableStateOf(WorkoutIntensity.MODERATE) }

    // Calculate current estimated calories
    val estimatedCalories = remember(workoutType, duration, distance, intensity, avgHeartRate) {
        val durationInt = duration.toIntOrNull() ?: 0
        val distanceFloat = distance.toFloatOrNull()
        val heartRate = avgHeartRate.toIntOrNull()

        if (durationInt > 0) {
            MetTableCalculator.calculateWorkoutCalories(
                workoutType = workoutType,
                durationMinutes = durationInt,
                weightKg = userWeight,
                intensity = intensity,
                distance = distanceFloat,
                avgHeartRate = heartRate,
            )
        } else {
            0
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                // Header
                Text(
                    text = "Edit Workout",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Workout Type Selection
                OutlinedTextField(
                    value = workoutType.name.replace("_", " "),
                    onValueChange = { },
                    label = { Text("Workout Type") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showWorkoutTypeDialog = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select workout type")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Workout Title") },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Duration and Distance Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = duration,
                        onValueChange = { duration = it },
                        label = { Text("Duration (min)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )

                    OutlinedTextField(
                        value = distance,
                        onValueChange = { distance = it },
                        label = { Text("Distance (km)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Intensity Selection
                OutlinedTextField(
                    value = intensity.name.replace("_", " "),
                    onValueChange = { },
                    label = { Text("Intensity") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showIntensityDialog = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select intensity")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Heart Rate Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = avgHeartRate,
                        onValueChange = { avgHeartRate = it },
                        label = { Text("Avg HR (bpm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )

                    OutlinedTextField(
                        value = maxHeartRate,
                        onValueChange = { maxHeartRate = it },
                        label = { Text("Max HR (bpm)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Calorie comparison
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    ),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Original Calories:",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                text = "${workout.caloriesBurned} cal",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = Color(0xFFFF5722),
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Updated Estimate:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                            Text(
                                text = "$estimatedCalories cal",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF5722),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (title.isNotBlank() && duration.isNotBlank()) {
                                onUpdate(
                                    workout.copy(
                                        workoutType = workoutType,
                                        title = title,
                                        duration = duration.toIntOrNull() ?: workout.duration,
                                        distance = distance.toFloatOrNull() ?: 0f,
                                        caloriesBurned = estimatedCalories,
                                        notes = notes.takeIf { it.isNotBlank() },
                                        avgHeartRate = avgHeartRate.toIntOrNull(),
                                        maxHeartRate = maxHeartRate.toIntOrNull(),
                                        updatedAt = Date(),
                                    ),
                                )
                            }
                        },
                        enabled = title.isNotBlank() && duration.isNotBlank(),
                    ) {
                        Text("Update")
                    }
                }
            }
        }
    }

    // Dialogs
    if (showWorkoutTypeDialog) {
        WorkoutTypeSelectionDialog(
            currentType = workoutType,
            onDismiss = { showWorkoutTypeDialog = false },
            onSelect = { selectedType ->
                workoutType = selectedType
                showWorkoutTypeDialog = false
            },
        )
    }

    if (showIntensityDialog) {
        IntensitySelectionDialog(
            currentIntensity = intensity,
            onDismiss = { showIntensityDialog = false },
            onSelect = { selectedIntensity ->
                intensity = selectedIntensity
                showIntensityDialog = false
            },
        )
    }
}

@Composable
fun DeleteWorkoutDialog(
    workout: Workout,
    onDismiss: () -> Unit,
    onDelete: (Workout) -> Unit,
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete Workout?",
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column {
                Text("Are you sure you want to delete this workout?")

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = workout.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                        )

                        Text(
                            text = workout.workoutType.name.replace("_", " "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Text(
                            text = dateFormat.format(workout.startTime),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Text(
                            text = "${workout.getFormattedDuration()} • ${workout.caloriesBurned} cal",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "This action cannot be undone.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onDelete(workout) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Text("Delete", color = MaterialTheme.colorScheme.onError)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
fun WorkoutTypeSelectionDialog(
    currentType: WorkoutType,
    onDismiss: () -> Unit,
    onSelect: (WorkoutType) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Workout Type") },
        text = {
            LazyColumn {
                items(WorkoutType.values().size) { index ->
                    val workoutType = WorkoutType.values()[index]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = workoutType == currentType,
                                onClick = { onSelect(workoutType) },
                                role = Role.RadioButton,
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = workoutType == currentType,
                            onClick = null,
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(workoutType.name.replace("_", " "))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        },
    )
}

@Composable
fun IntensitySelectionDialog(
    currentIntensity: WorkoutIntensity,
    onDismiss: () -> Unit,
    onSelect: (WorkoutIntensity) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Workout Intensity") },
        text = {
            Column {
                WorkoutIntensity.values().forEach { intensity ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = intensity == currentIntensity,
                                onClick = { onSelect(intensity) },
                                role = Role.RadioButton,
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = intensity == currentIntensity,
                            onClick = null,
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = intensity.name.replace("_", " "),
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                text = getIntensityDescription(intensity),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        },
    )
}

private fun getIntensityDescription(intensity: WorkoutIntensity): String {
    return when (intensity) {
        WorkoutIntensity.LOW -> "50-60% max HR, light effort"
        WorkoutIntensity.MODERATE -> "60-70% max HR, moderate effort"
        WorkoutIntensity.HIGH -> "70-85% max HR, vigorous effort"
        WorkoutIntensity.VERY_HIGH -> "85%+ max HR, maximum effort"
    }
}
