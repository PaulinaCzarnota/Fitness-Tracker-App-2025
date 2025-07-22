package com.example.fitnesstrackerapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnesstrackerapp.data.Diet
import com.example.fitnesstrackerapp.viewmodel.DietViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Composable screen for logging and displaying diet entries (food and calories).
 */
@Composable
fun DietScreen(viewModel: DietViewModel = viewModel()) {
    // Local UI state for form inputs
    var food by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }

    // Observe diet entries from the ViewModel
    val dietList by viewModel.allDiets.observeAsState(emptyList())

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Log Food Intake",
            style = MaterialTheme.typography.headlineSmall
        )

        // Food input field
        OutlinedTextField(
            value = food,
            onValueChange = { food = it },
            label = { Text("Food") },
            modifier = Modifier.fillMaxWidth()
        )

        // Calories input field
        OutlinedTextField(
            value = calories,
            onValueChange = { calories = it },
            label = { Text("Calories") },
            modifier = Modifier.fillMaxWidth()
        )

        // Button to add a new diet entry
        Button(
            onClick = {
                if (food.isNotBlank() && calories.isNotBlank()) {
                    viewModel.addDiet(
                        Diet(
                            food = food.trim(),
                            calories = calories.toIntOrNull() ?: 0
                        )
                    )
                    // Clear form
                    food = ""
                    calories = ""
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Add Diet Entry")
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        Text(
            text = "Diet Log",
            style = MaterialTheme.typography.titleMedium
        )

        // List of all diet entries
        LazyColumn {
            items(dietList) { diet ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Food: ${diet.food}")
                        Text("Calories: ${diet.calories}")
                        Text(
                            "Date: ${
                                SimpleDateFormat(
                                    "yyyy-MM-dd",
                                    Locale.getDefault()
                                ).format(Date(diet.date))
                            }"
                        )
                    }
                }
            }
        }
    }
}
