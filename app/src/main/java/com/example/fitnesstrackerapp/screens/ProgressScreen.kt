package com.example.fitnesstrackerapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnesstrackerapp.viewmodel.WorkoutViewModel
import com.example.fitnesstrackerapp.data.Workout
import java.text.SimpleDateFormat
import java.util.*

/**
 * Screen that displays a summary of workout progress
 * and a detailed history of past workouts.
 */
@Composable
fun ProgressScreen(viewModel: WorkoutViewModel = viewModel()) {
    // Observe the list of workouts from ViewModel
    val workouts by viewModel.allWorkouts.observeAsState(emptyList())

    // Summary calculations
    val totalCalories = workouts.sumOf { it.calories }
    val totalDistance = workouts.sumOf { it.distance }
    val totalWorkouts = workouts.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text("Progress Overview", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))

        // Summary metrics
        Text("Total Workouts: $totalWorkouts")
        Text("Total Calories Burned: $totalCalories")
        Text("Total Distance: ${"%.2f".format(totalDistance)} km")

        Divider(modifier = Modifier.padding(vertical = 12.dp))

        Text("Workout History", style = MaterialTheme.typography.titleMedium)

        // Workout history list
        LazyColumn {
            items(workouts) { workout ->
                WorkoutItem(workout = workout)
            }
        }
    }
}

/**
 * Composable that displays a single workout item in a Card.
 */
@Composable
fun WorkoutItem(workout: Workout) {
    // Format the workout date
    val formatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text("Date: ${formatter.format(Date(workout.date))}")
            Text("Type: ${workout.type}")
            Text("Duration: ${workout.duration} min")
            Text("Distance: ${"%.2f".format(workout.distance)} km")
            Text("Calories: ${workout.calories}")
            if (workout.notes.isNotBlank()) {
                Text("Notes: ${workout.notes}")
            }
        }
    }
}
