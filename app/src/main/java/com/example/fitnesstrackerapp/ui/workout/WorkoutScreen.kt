/**
 * Workout Screen
 *
 * Responsibilities:
 * - Displays active workout session interface
 * - Shows workout history and statistics
 * - Provides workout creation and management
 * - Tracks workout progress and timing
 */
package com.example.fitnesstrackerapp.ui.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Square
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitnesstrackerapp.ViewModelFactoryProvider
import com.example.fitnesstrackerapp.data.entity.Workout
import com.example.fitnesstrackerapp.data.entity.WorkoutType
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutScreen(
    modifier: Modifier = Modifier,
    authViewModel: com.example.fitnesstrackerapp.ui.auth.AuthViewModel,
    activity: androidx.activity.ComponentActivity,
) {
    // Get the current user ID for the ViewModel (fallback to 1L for demo purposes)
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val userId = authState.user?.id ?: 1L

    // Initialize WorkoutViewModel with user ID
    val workoutViewModel: WorkoutViewModel = remember(userId) {
        ViewModelFactoryProvider.getWorkoutViewModel(activity, userId)
    }
    val uiState by workoutViewModel.uiState.collectAsStateWithLifecycle()
    var showNewWorkoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        // Header
        Text(
            text = "Workouts",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Current workout status
        if (uiState.isWorkoutActive) {
            ActiveWorkoutCard(
                onStopWorkout = { workoutViewModel.stopWorkout() },
            )
        } else {
            // Start new workout button
            Button(
                onClick = { showNewWorkoutDialog = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start New Workout")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Workout history section
        Text(
            text = "Recent Workouts",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.workouts.isEmpty()) {
            Text(
                text = "No workouts yet. Start your first workout!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(uiState.workouts, key = { it.id }) { workout ->
                    WorkoutHistoryItem(
                        workout = workout,
                        onDelete = { workoutViewModel.deleteWorkout(workout) },
                    )
                }
            }
        }
    }

    // New workout dialog
    if (showNewWorkoutDialog) {
        NewWorkoutDialog(
            onDismiss = { showNewWorkoutDialog = false },
            onConfirm = { type, duration, _, _ ->
                // Convert String to WorkoutType enum and match WorkoutViewModel.startWorkout(type: WorkoutType, title: String)
                val workoutType = try {
                    WorkoutType.valueOf(type)
                } catch (e: IllegalArgumentException) {
                    WorkoutType.OTHER
                }
                val workoutTitle = "${workoutType.name} - ${duration}min"
                workoutViewModel.startWorkout(workoutType, workoutTitle)
                showNewWorkoutDialog = false
            },
        )
    }
}

@Composable
private fun ActiveWorkoutCard(
    onStopWorkout: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "Workout in Progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Running • 25:34",
                style = MaterialTheme.typography.bodyLarge,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onStopWorkout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Icon(Icons.Default.Square, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Stop Workout")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewWorkoutDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int, Int, String) -> Unit,
) {
    var workoutType by remember { mutableStateOf("RUNNING") }
    var duration by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Workout") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Workout type dropdown
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = { },
                ) {
                    OutlinedTextField(
                        value = workoutType,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Workout Type") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Duration (minutes)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = distance,
                    onValueChange = { distance = it },
                    label = { Text("Distance (km)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        workoutType,
                        duration.toIntOrNull() ?: 0,
                        distance.toIntOrNull() ?: 0,
                        notes,
                    )
                },
            ) {
                Text("Start")
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
private fun WorkoutHistoryItem(
    workout: Workout,
    onDelete: () -> Unit,
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = workout.workoutType.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )

                Text(
                    text = "${workout.duration} min • ${workout.distance} km",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Text(
                    text = dateFormat.format(workout.startTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete workout",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
