package com.example.fitnesstrackerapp.screens

import android.app.Application
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitnesstrackerapp.data.Workout
import com.example.fitnesstrackerapp.viewmodel.WorkoutViewModel
import com.example.fitnesstrackerapp.viewmodel.WorkoutViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

/**
 * Composable screen for logging and viewing workouts.
 */
@Composable
fun WorkoutScreen() {
    val context = LocalContext.current

    // ViewModel with application context using factory
    val viewModel: WorkoutViewModel = viewModel(
        factory = WorkoutViewModelFactory(context.applicationContext as Application)
    )

    // State variables for input fields
    var type by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Collect list of workouts from ViewModel
    val workouts by viewModel.allWorkouts.collectAsStateWithLifecycle(initialValue = emptyList())

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text("Log New Workout", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(8.dp))

        // Type input
        OutlinedTextField(
            value = type,
            onValueChange = { type = it },
            label = { Text("Workout Type") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text)
        )

        // Duration input
        OutlinedTextField(
            value = duration,
            onValueChange = { duration = it },
            label = { Text("Duration (minutes)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )

        // Distance input
        OutlinedTextField(
            value = distance,
            onValueChange = { distance = it },
            label = { Text("Distance (km)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )

        // Calories input
        OutlinedTextField(
            value = calories,
            onValueChange = { calories = it },
            label = { Text("Calories Burned") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )

        // Notes input
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Save button
        Button(
            onClick = {
                if (type.isNotBlank()) {
                    val workout = Workout(
                        type = type.trim(),
                        duration = duration.toIntOrNull() ?: 0,
                        distance = distance.toDoubleOrNull() ?: 0.0,
                        calories = calories.toIntOrNull() ?: 0,
                        notes = notes.trim(),
                        date = System.currentTimeMillis()
                    )
                    viewModel.insertWorkout(workout)

                    // Reset fields
                    type = ""
                    duration = ""
                    distance = ""
                    calories = ""
                    notes = ""

                    Toast.makeText(context, "Workout saved!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Workout type is required.", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Workout")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Workout History", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        // Display list of workouts
        LazyColumn {
            items(workouts) { workout ->
                WorkoutCard(workout = workout)
            }
        }
    }
}

/**
 * Displays a single workout entry.
 */
@Composable
fun WorkoutCard(workout: Workout) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    val formattedDate = dateFormat.format(Date(workout.date))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Type: ${workout.type}")
            Text("Duration: ${workout.duration} mins")
            Text("Distance: ${workout.distance} km")
            Text("Calories: ${workout.calories}")
            Text("Notes: ${workout.notes}")
            Text("Date: $formattedDate")
        }
    }
}
