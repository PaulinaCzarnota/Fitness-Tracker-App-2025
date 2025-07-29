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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.fitnesstrackerapp.data.Workout
import com.example.fitnesstrackerapp.util.formatDate
import com.example.fitnesstrackerapp.viewmodel.WorkoutViewModel
import com.example.fitnesstrackerapp.viewmodel.WorkoutViewModelFactory
import java.text.SimpleDateFormat
import java.util.*
import com.example.fitnesstrackerapp.ui.components.BottomNavigationBar

/**
 * ProgressScreen
 *
 * Displays progress statistics and workout analytics.
 * Shows weekly summaries, charts, and workout history.
 *
 * @param navController Optional navigation controller for consistency with Navigation.kt.
 */
@Composable
fun ProgressScreen(navController: NavHostController? = null) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    // Initialize ViewModel with application context
    val viewModel: WorkoutViewModel = viewModel(
        factory = WorkoutViewModelFactory(application)
    )

    // Get workouts from the last 7 days
    val endDate = System.currentTimeMillis()
    val startDate = endDate - (7 * 24 * 60 * 60 * 1000) // 7 days ago
    val workouts by viewModel.getWorkoutsBetween(startDate, endDate).collectAsStateWithLifecycle(initialValue = emptyList())

    // --- Compute aggregate stats ---
    val totalWorkouts = workouts.size
    val totalCalories = workouts.sumOf { it.calories }
    val totalDistance = workouts.sumOf { it.distance }

    // Group calories burned by weekday for bar chart
    val caloriesByDay = remember(workouts) {
        val formatter = SimpleDateFormat("EEE", Locale.getDefault())
        val grouped = mutableMapOf<String, Int>()
        workouts.forEach {
            val day = formatter.format(Date(it.date))
            grouped[day] = grouped.getOrDefault(day, 0) + it.calories
        }
        // Ensure all 7 weekdays are represented in fixed order
        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").associateWith {
            grouped[it] ?: 0
        }
    }

    // App layout using Scaffold with BottomNavigationBar
    Scaffold(
        bottomBar = { 
            navController?.let { BottomNavigationBar(navController = it) }
        }
    ) { innerPadding ->
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Title
            Text("Weekly Progress", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(12.dp))

            // Summary stats
            Text("Workouts: $totalWorkouts")
            Text("Calories: $totalCalories kcal")
            Text("Distance: %.2f km".format(totalDistance))

            Spacer(modifier = Modifier.height(20.dp))

            // Bar Chart
            Text("Calories Burned by Day", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            BarChart(data = caloriesByDay)

            Spacer(modifier = Modifier.height(24.dp))

            // Workout history list
            Text("Workout History", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (workouts.isEmpty()) {
                Text("No workouts recorded in the last 7 days.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(workouts) { workout ->
                        WorkoutItem(workout)
                    }
                }
            }
        }
    }
}

/**
 * WorkoutItem
 *
 * Card displaying details of a single workout entry.
 *
 * @param workout A single workout instance.
 */
@Composable
fun WorkoutItem(workout: Workout) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Date: ${formatDate(workout.date)}", style = MaterialTheme.typography.labelLarge)
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
 * BarChart
 *
 * Composable displaying horizontal bars for each weekday's calorie total.
 *
 * @param data Map of weekday labels ("Mon", "Tue", ...) to calorie integers.
 */
@Composable
fun BarChart(data: Map<String, Int>) {
    val maxCalories = (data.values.maxOrNull() ?: 1).toFloat() // Avoid divide-by-zero

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
                        color = Color(0xFF3F51B5), // Indigo color
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(day, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
