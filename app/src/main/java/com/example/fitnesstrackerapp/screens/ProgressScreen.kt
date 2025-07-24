package com.example.fitnesstrackerapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnesstrackerapp.viewmodel.WorkoutViewModel
import com.example.fitnesstrackerapp.data.Workout
import com.example.fitnesstrackerapp.util.formatDate

/**
 * ProgressScreen
 *
 * Displays overall progress summary and workout history.
 */
@Composable
fun ProgressScreen(viewModel: WorkoutViewModel = viewModel()) {
    val workouts by viewModel.allWorkouts.observeAsState(emptyList())

    val totalWorkouts = workouts.size
    val totalCalories = workouts.sumOf { it.calories }
    val totalDistance = workouts.sumOf { it.distance }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Progress Overview", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(12.dp))

        Text("Total Workouts: $totalWorkouts", style = MaterialTheme.typography.titleMedium)
        Text("Total Calories Burned: $totalCalories", style = MaterialTheme.typography.titleMedium)
        Text("Total Distance: %.2f km".format(totalDistance), style = MaterialTheme.typography.titleMedium)

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Text("Workout History", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(workouts) { workout ->
                WorkoutItem(workout)
            }
        }
    }
}

@Composable
fun WorkoutItem(workout: Workout) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Date: ${formatDate(workout.date)}")
            Text("Type: ${workout.type}")
            Text("Duration: ${workout.duration} min")
            Text("Distance: %.2f km".format(workout.distance))
            Text("Calories: ${workout.calories}")
            if (workout.notes.isNotBlank()) {
                Text("Notes: ${workout.notes}")
            }
        }
    }
}
