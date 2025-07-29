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
import androidx.navigation.NavHostController
import com.example.fitnesstrackerapp.data.Workout
import com.example.fitnesstrackerapp.viewmodel.WorkoutViewModel
import com.example.fitnesstrackerapp.viewmodel.WorkoutViewModelFactory
import com.example.fitnesstrackerapp.ui.components.BottomNavigationBar
import java.text.SimpleDateFormat
import java.util.*

/**
 * WorkoutScreen
 *
 * Allows the user to log workout sessions (type, duration, distance, calories, notes),
 * view a history of logged workouts, delete individual sessions, and clear all history.
 *
 * @param navController Optional navigation controller for consistency with Navigation.kt.
 */
@Composable
fun WorkoutScreen(navController: NavHostController? = null) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    // Obtain the ViewModel via factory
    val viewModel: WorkoutViewModel = viewModel(
        factory = WorkoutViewModelFactory(application)
    )

    // Form input state for new workout
    var type by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Collect workout list from ViewModel (StateFlow)
    val workouts by viewModel.allWorkouts.collectAsStateWithLifecycle(initialValue = emptyList())

    // App layout using Scaffold with BottomNavigationBar
    Scaffold(
        bottomBar = { 
            navController?.let { BottomNavigationBar(navController = it) }
        }
    ) { innerPadding ->
        // Layout container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // ── Workout Entry Header ──
            Text("Log New Workout", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(12.dp))

            // ── Input Form ──
            WorkoutInputField("Workout Type", type, { type = it }, KeyboardType.Text)
            WorkoutInputField("Duration (minutes)", duration, { duration = it }, KeyboardType.Number)
            WorkoutInputField("Distance (km)", distance, { distance = it }, KeyboardType.Number)
            WorkoutInputField("Calories Burned", calories, { calories = it }, KeyboardType.Number)
            WorkoutInputField("Notes (optional)", notes, { notes = it }, KeyboardType.Text)

            Spacer(modifier = Modifier.height(16.dp))

            // ── Save Button ──
            Button(
                onClick = {
                    // Validate input before saving
                    val isValid = type.isNotBlank()
                            && duration.toIntOrNull() != null
                            && distance.toDoubleOrNull() != null
                            && calories.toIntOrNull() != null

                    if (isValid) {
                        // Construct and insert workout
                        val workout = Workout(
                            type = type.trim(),
                            duration = duration.toInt(),
                            distance = distance.toDouble(),
                            calories = calories.toInt(),
                            notes = notes.trim(),
                            date = System.currentTimeMillis()
                        )
                        viewModel.insertWorkout(workout)

                        // Reset input fields
                        type = ""
                        duration = ""
                        distance = ""
                        calories = ""
                        notes = ""

                        Toast.makeText(context, "Workout saved!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Please complete all required fields correctly.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Workout")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Workout History Header ──
            Text("Workout History", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            // ── List of Workouts ──
            if (workouts.isEmpty()) {
                Text("No workouts logged yet.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(workouts, key = { it.id }) { workout ->
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

                // ── Clear All Workouts Button ──
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
}

/**
 * WorkoutInputField
 *
 * Reusable input field used for entering workout details.
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
        onValueChange = onChange,
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
 * Displays details of an individual workout session.
 * Allows deletion of the session.
 */
@Composable
fun WorkoutCard(
    workout: Workout,
    onDelete: (Workout) -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    val formattedDate = dateFormat.format(Date(workout.date))

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Type: ${workout.type}")
            Text("Duration: ${workout.duration} min")
            Text("Distance: %.2f km".format(workout.distance))
            Text("Calories: ${workout.calories}")
            if (workout.notes.isNotBlank()) {
                Text("Notes: ${workout.notes}")
            }
            Text("Date: $formattedDate")

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onDelete(workout) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete")
            }
        }
    }
}
