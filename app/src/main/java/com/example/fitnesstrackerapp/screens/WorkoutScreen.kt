package com.example.fitnesstrackerapp.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnesstrackerapp.data.Workout
import com.example.fitnesstrackerapp.viewmodel.WorkoutViewModel

/**
 * WorkoutScreen
 *
 * Allows users to log workouts and view past entries.
 * Uses Jetpack Compose + ViewModel + Room (StateFlow + collectAsStateWithLifecycle).
 */
@Composable
fun WorkoutScreen(viewModel: WorkoutViewModel = viewModel()) {
    val context = LocalContext.current

    // Form inputs
    var type by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Collect workouts from ViewModel (lifecycle-aware)
    val workouts by viewModel.allWorkouts.collectAsStateWithLifecycle(initialValue = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Log a Workout", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(12.dp))

        // Input fields
        OutlinedTextField(
            value = type,
            onValueChange = { type = it },
            label = { Text("Workout Type (e.g., Run)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = duration,
            onValueChange = { duration = it },
            label = { Text("Duration (minutes)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = distance,
            onValueChange = { distance = it },
            label = { Text("Distance (km)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = calories,
            onValueChange = { calories = it },
            label = { Text("Calories Burned") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val trimmedType = type.trim()
                if (trimmedType.isNotBlank()) {
                    val workout = Workout(
                        type = trimmedType,
                        duration = duration.toIntOrNull() ?: 0,
                        distance = distance.toDoubleOrNull() ?: 0.0,
                        calories = calories.toIntOrNull() ?: 0,
                        notes = notes.trim(),
                        date = System.currentTimeMillis()
                    )
                    viewModel.insertWorkout(workout)

                    type = ""
                    duration = ""
                    distance = ""
                    calories = ""
                    notes = ""

                    Toast.makeText(context, "Workout saved", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Please enter a workout type", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Workout")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Previous Workouts", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        // List of saved workouts
        LazyColumn {
            items(workouts) { workout ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Type: ${workout.type}")
                        Text("Duration: ${workout.duration} min")
                        Text("Distance: %.2f km".format(workout.distance))
                        Text("Calories: ${workout.calories}")
                        if (workout.notes.isNotBlank()) {
                            Text("Notes: ${workout.notes}")
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Text-based delete button (no icon used)
                        TextButton(
                            onClick = {
                                viewModel.deleteWorkout(workout)
                                Toast.makeText(context, "Workout deleted", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text("Delete Workout")
                        }
                    }
                }
            }
        }
    }
}
