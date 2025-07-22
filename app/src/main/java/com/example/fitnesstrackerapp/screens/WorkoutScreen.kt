package com.example.fitnesstrackerapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnesstrackerapp.data.Workout
import com.example.fitnesstrackerapp.viewmodel.WorkoutViewModel

/**
 * Screen for logging and displaying workout entries.
 */
@Composable
fun WorkoutScreen(viewModel: WorkoutViewModel = viewModel()) {
    // Form input states
    var type by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Observing the list of workouts from ViewModel
    val workouts by viewModel.allWorkouts.collectAsState(initial = emptyList())

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Log a Workout",
            style = MaterialTheme.typography.headlineSmall
        )

        // Input fields
        OutlinedTextField(
            value = type,
            onValueChange = { type = it },
            label = { Text("Type (e.g., Running)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = duration,
            onValueChange = { duration = it },
            label = { Text("Duration (minutes)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = distance,
            onValueChange = { distance = it },
            label = { Text("Distance (km)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = calories,
            onValueChange = { calories = it },
            label = { Text("Calories Burned") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth()
        )

        // Add workout button
        Button(
            onClick = {
                if (type.isNotBlank() && duration.isNotBlank()) {
                    viewModel.addWorkout(
                        Workout(
                            type = type,
                            duration = duration.toIntOrNull() ?: 0,
                            distance = distance.toDoubleOrNull() ?: 0.0,
                            calories = calories.toIntOrNull() ?: 0,
                            notes = notes
                        )
                    )
                    // Clear form
                    type = ""
                    duration = ""
                    distance = ""
                    calories = ""
                    notes = ""
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Add Workout")
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        Text(
            text = "Previous Workouts",
            style = MaterialTheme.typography.titleMedium
        )

        // Display list of previous workouts
        LazyColumn {
            items(workouts) { workout ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Type: ${workout.type}")
                        Text("Duration: ${workout.duration} min")
                        Text("Distance: ${workout.distance} km")
                        Text("Calories: ${workout.calories}")
                        if (workout.notes.isNotBlank()) {
                            Text("Notes: ${workout.notes}")
                        }
                    }
                }
            }
        }
    }
}
