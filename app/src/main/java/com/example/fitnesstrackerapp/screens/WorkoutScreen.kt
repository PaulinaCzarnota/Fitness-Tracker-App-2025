package com.example.fitnesstrackerapp.screens

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
 * WorkoutScreen
 *
 * Screen that allows users to:
 * - Log a new workout
 * - View all saved workouts
 * - Delete individual workouts
 * - Clear all workouts
 */
@Composable
fun WorkoutScreen() {
    val context = LocalContext.current

    // Initialize ViewModel with factory
    val viewModel: WorkoutViewModel = viewModel(
        factory = WorkoutViewModelFactory(context.applicationContext as Application)
    )

    // Form input state
    var type by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // List of all workouts from database
    val workouts by viewModel.allWorkouts.collectAsStateWithLifecycle(initialValue = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Log New Workout", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(12.dp))

        // --- Input Fields ---
        WorkoutInputField("Workout Type", type, { type = it }, KeyboardType.Text)
        WorkoutInputField("Duration (minutes)", duration, { duration = it }, KeyboardType.Number)
        WorkoutInputField("Distance (km)", distance, { distance = it }, KeyboardType.Number)
        WorkoutInputField("Calories Burned", calories, { calories = it }, KeyboardType.Number)
        WorkoutInputField("Notes (optional)", notes, { notes = it }, KeyboardType.Text)

        Spacer(modifier = Modifier.height(16.dp))

        // --- Save Button ---
        Button(
            onClick = {
                val isValid = type.isNotBlank()
                        && duration.toIntOrNull() != null
                        && distance.toDoubleOrNull() != null
                        && calories.toIntOrNull() != null

                if (isValid) {
                    val workout = Workout(
                        type = type.trim(),
                        duration = duration.toInt(),
                        distance = distance.toDouble(),
                        calories = calories.toInt(),
                        notes = notes.trim(),
                        date = System.currentTimeMillis()
                    )
                    viewModel.insertWorkout(workout)

                    // Clear inputs
                    type = ""; duration = ""; distance = ""; calories = ""; notes = ""
                    Toast.makeText(context, "Workout saved!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Please fill all fields correctly.", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Workout")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Workout History Header ---
        Text("Workout History", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        // --- Workout List ---
        if (workouts.isEmpty()) {
            Text("No workouts logged yet.")
        } else {
            LazyColumn {
                items(workouts) { workout ->
                    WorkoutCard(
                        workout = workout,
                        onDelete = {
                            viewModel.deleteWorkout(it)
                            Toast.makeText(context, "Workout deleted!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Clear All Button ---
            Button(
                onClick = {
                    viewModel.clearAllWorkouts()
                    Toast.makeText(context, "All workouts cleared!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Clear All Workouts")
            }
        }
    }
}

/**
 * WorkoutInputField
 *
 * Reusable input field for workout form.
 */
@Composable
fun WorkoutInputField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    keyboardType: KeyboardType
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onChange(it) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        singleLine = true
    )
}

/**
 * WorkoutCard
 *
 * Displays individual workout entry with a delete button.
 */
@Composable
fun WorkoutCard(
    workout: Workout,
    onDelete: (Workout) -> Unit
) {
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
            Text("Duration: ${workout.duration} minutes")
            Text("Distance: %.2f km".format(workout.distance))
            Text("Calories: ${workout.calories}")
            if (workout.notes.isNotBlank()) {
                Text("Notes: ${workout.notes}")
            }
            Text("Date: $formattedDate")

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onDelete(workout) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Delete")
            }
        }
    }
}
