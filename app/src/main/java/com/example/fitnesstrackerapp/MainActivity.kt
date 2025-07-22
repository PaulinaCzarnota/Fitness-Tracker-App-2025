package com.example.fitnesstrackerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fitnesstrackerapp.data.Workout
import com.example.fitnesstrackerapp.ui.theme.FitnessTrackerAppTheme
import com.example.fitnesstrackerapp.viewmodel.WorkoutViewModel
import com.example.fitnesstrackerapp.viewmodel.WorkoutViewModelFactory

/**
 * MainActivity is the app's entry point.
 * It sets the Compose UI and provides the WorkoutViewModel instance.
 */
class MainActivity : ComponentActivity() {

    // Initialize WorkoutViewModel using the factory with Application context
    private val viewModel: WorkoutViewModel by viewModels {
        WorkoutViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Apply the app's theme to all UI
            FitnessTrackerAppTheme {
                // Root Surface container with full size modifier
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Call WorkoutScreen and pass the ViewModel for data operations
                    WorkoutScreen(viewModel)
                }
            }
        }
    }
}

/**
 * Composable displaying workout input form and history list.
 * @param viewModel WorkoutViewModel instance to observe and insert workouts.
 */
@Composable
fun WorkoutScreen(viewModel: WorkoutViewModel) {
    // Local UI state variables for user inputs
    var type by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Observe the list of workouts from ViewModel as LiveData
    val workouts by viewModel.allWorkouts.observeAsState(emptyList())

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        // Input field for workout type
        OutlinedTextField(
            value = type,
            onValueChange = { type = it },
            label = { Text("Workout Type") },
            modifier = Modifier.fillMaxWidth()
        )

        // Input field for duration (in minutes)
        OutlinedTextField(
            value = duration,
            onValueChange = { duration = it },
            label = { Text("Duration (minutes)") },
            modifier = Modifier.fillMaxWidth()
        )

        // Input field for distance (in kilometers)
        OutlinedTextField(
            value = distance,
            onValueChange = { distance = it },
            label = { Text("Distance (km)") },
            modifier = Modifier.fillMaxWidth()
        )

        // Input field for calories burned
        OutlinedTextField(
            value = calories,
            onValueChange = { calories = it },
            label = { Text("Calories Burned") },
            modifier = Modifier.fillMaxWidth()
        )

        // Input field for optional notes
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Button to add workout entry
        Button(
            onClick = {
                // Validate that workout type is not blank before insertion
                if (type.isNotBlank()) {
                    // Create Workout object with safe parsing of numbers
                    val workout = Workout(
                        type = type.trim(),
                        duration = duration.toIntOrNull() ?: 0,
                        distance = distance.toDoubleOrNull() ?: 0.0,
                        calories = calories.toIntOrNull() ?: 0,
                        notes = notes.trim(),
                        date = System.currentTimeMillis() // Current time for timestamp
                    )
                    // Insert workout via ViewModel
                    viewModel.insertWorkout(workout)

                    // Reset input fields after successful insertion
                    type = ""
                    duration = ""
                    distance = ""
                    calories = ""
                    notes = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Workout")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Header for workout history list
        Text(
            text = "Workout History",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // LazyColumn displaying the list of workouts
        LazyColumn {
            items(workouts) { workout ->
                Text(
                    text = "• ${workout.type}, ${workout.duration} min, ${workout.distance} km, ${workout.calories} cal",
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}
