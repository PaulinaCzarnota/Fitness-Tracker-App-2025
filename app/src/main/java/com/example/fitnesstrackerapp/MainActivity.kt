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

class MainActivity : ComponentActivity() {

    private val viewModel: WorkoutViewModel by viewModels {
        WorkoutViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FitnessTrackerAppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    WorkoutScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun WorkoutScreen(viewModel: WorkoutViewModel) {
    var type by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val workouts by viewModel.allWorkouts.observeAsState(emptyList())

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        OutlinedTextField(
            value = type,
            onValueChange = { type = it },
            label = { Text("Type") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = duration,
            onValueChange = { duration = it },
            label = { Text("Duration (min)") },
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
            label = { Text("Calories") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (type.isNotBlank()) {
                    val workout = Workout(
                        type = type.trim(),
                        duration = duration.toIntOrNull() ?: 0,
                        distance = distance.toFloatOrNull() ?: 0f,
                        calories = calories.toIntOrNull() ?: 0,
                        notes = notes.trim(),
                        date = System.currentTimeMillis()
                    )
                    viewModel.addWorkout(workout)
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

        Text(
            text = "Workout History:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

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
