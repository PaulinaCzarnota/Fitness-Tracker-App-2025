package com.example.fitnesstrackerapp.screens

import android.app.Application
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnesstrackerapp.data.Workout
import com.example.fitnesstrackerapp.util.formatDate
import com.example.fitnesstrackerapp.viewmodel.WorkoutViewModel
import com.example.fitnesstrackerapp.viewmodel.WorkoutViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

/**
 * ProgressScreen
 *
 * Displays a summary of the user’s workout activity over the past 7 days:
 * - Total number of workouts
 * - Calories and distance summary
 * - Bar chart of calories burned by weekday
 * - List of workout entries
 */
@Composable
fun ProgressScreen() {
    val context = LocalContext.current
    val viewModel: WorkoutViewModel = viewModel(
        factory = WorkoutViewModelFactory(context.applicationContext as Application)
    )

    // --- Calculate range: last 7 days ---
    val end = System.currentTimeMillis()
    val start = end - 7 * 24 * 60 * 60 * 1000L // 7 days in milliseconds

    // --- Fetch workouts in the past week ---
    val workoutsInWeek by produceState(initialValue = emptyList<Workout>()) {
        viewModel.getWorkoutsBetween(start, end).collect { value = it }
    }

    // --- Summary Metrics ---
    val totalWorkouts = workoutsInWeek.size
    val totalCalories = workoutsInWeek.sumOf { it.calories }
    val totalDistance = workoutsInWeek.sumOf { it.distance }

    // --- Calories Grouped by Weekday ---
    val caloriesByDay = remember(workoutsInWeek) {
        val formatter = SimpleDateFormat("EEE", Locale.getDefault()) // e.g., Mon, Tue
        val dailyCalories = mutableMapOf<String, Int>()
        workoutsInWeek.forEach {
            val day = formatter.format(Date(it.date))
            dailyCalories[day] = dailyCalories.getOrDefault(day, 0) + it.calories
        }

        // Preserve Mon–Sun order
        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").associateWith {
            dailyCalories[it] ?: 0
        }
    }

    // --- Layout ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Weekly Progress", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(12.dp))

        Text("Workouts this week: $totalWorkouts")
        Text("Calories burned: $totalCalories")
        Text("Distance: %.2f km".format(totalDistance))

        Spacer(modifier = Modifier.height(20.dp))

        Text("Calories by Day", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        BarChart(data = caloriesByDay)

        Spacer(modifier = Modifier.height(24.dp))
        Text("Workouts List", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        if (workoutsInWeek.isEmpty()) {
            Text("No workouts recorded in the last 7 days.")
        } else {
            LazyColumn {
                items(workoutsInWeek) { workout ->
                    WorkoutItem(workout)
                }
            }
        }
    }
}

/**
 * WorkoutItem – Single workout display card
 */
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

/**
 * BarChart – Visualizes calories per weekday
 */
@Composable
fun BarChart(data: Map<String, Int>) {
    val maxCalories = (data.values.maxOrNull() ?: 1).toFloat()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        data.forEach { (day, calories) ->
            val heightRatio = calories / maxCalories
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Canvas(
                    modifier = Modifier
                        .height((heightRatio * 120).dp)
                        .fillMaxWidth()
                ) {
                    drawRoundRect(
                        color = Color(0xFF3F51B5),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(day, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
