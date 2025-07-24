package com.example.fitnesstrackerapp.screens

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnesstrackerapp.data.Diet
import com.example.fitnesstrackerapp.viewmodel.DietViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Converts a timestamp (milliseconds) to a readable date format (e.g., "24 Jul 2025").
 */
fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

/**
 * Composable screen to input diet entries and view a list of all logged entries.
 *
 * @param viewModel DietViewModel for accessing data layer (default = from DI)
 */
@Composable
fun DietScreen(viewModel: DietViewModel = viewModel()) {
    val context = LocalContext.current

    // Input fields
    var foodInput by remember { mutableStateOf("") }
    var caloriesInput by remember { mutableStateOf("") }

    // Observe diet list from Room DB via StateFlow with lifecycle-awareness
    val dietList by viewModel.allDiets.collectAsStateWithLifecycle(initialValue = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Title header
        Text(
            text = "Log Food Intake",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Input: Food name
        OutlinedTextField(
            value = foodInput,
            onValueChange = { foodInput = it },
            label = { Text("Food") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Input: Calories (number only)
        OutlinedTextField(
            value = caloriesInput,
            onValueChange = { caloriesInput = it },
            label = { Text("Calories") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Button to add entry to DB
        Button(
            onClick = {
                val food = foodInput.trim()
                val calories = caloriesInput.toIntOrNull()

                if (food.isNotBlank() && calories != null && calories > 0) {
                    val newEntry = Diet(
                        food = food,
                        calories = calories,
                        date = System.currentTimeMillis()
                    )
                    viewModel.insert(newEntry)

                    Toast.makeText(context, "Diet entry added", Toast.LENGTH_SHORT).show()

                    // Reset input fields
                    foodInput = ""
                    caloriesInput = ""
                } else {
                    Toast.makeText(context, "Please enter valid food and calories", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Diet Entry")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        // Sub-header
        Text(
            text = "Diet Log",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        // List of saved diet entries
        LazyColumn {
            items(dietList) { diet ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Food: ${diet.food}")
                        Text("Calories: ${diet.calories}")
                        Text("Date: ${formatDate(diet.date)}")
                    }
                }
            }
        }
    }
}
