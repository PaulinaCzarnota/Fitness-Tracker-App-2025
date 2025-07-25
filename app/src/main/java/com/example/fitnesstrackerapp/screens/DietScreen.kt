package com.example.fitnesstrackerapp.screens

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
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fitnesstrackerapp.data.Diet
import com.example.fitnesstrackerapp.viewmodel.DietViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility function to format a timestamp (milliseconds) to a date string.
 */
fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

/**
 * DietScreen
 *
 * Allows users to log diet entries (food and calories) and view past entries.
 */
@Composable
fun DietScreen(viewModel: DietViewModel = viewModel()) {
    val context = LocalContext.current

    // State for input fields
    var foodInput by remember { mutableStateOf("") }
    var caloriesInput by remember { mutableStateOf("") }

    // Observe diet entries as LiveData using observeAsState (since ViewModel exposes LiveData)
    val dietList by viewModel.allDiets.observeAsState(emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Log Food Intake", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(12.dp))

        // Food input
        OutlinedTextField(
            value = foodInput,
            onValueChange = { foodInput = it },
            label = { Text("Food") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Calories input with keyboard number type
        OutlinedTextField(
            value = caloriesInput,
            onValueChange = { caloriesInput = it },
            label = { Text("Calories") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                val food = foodInput.trim()
                val calories = caloriesInput.toIntOrNull()

                if (food.isNotBlank() && calories != null && calories > 0) {
                    val newDiet = Diet(
                        food = food,
                        calories = calories,
                        date = System.currentTimeMillis()
                    )
                    viewModel.addDiet(newDiet)

                    Toast.makeText(context, "Diet entry added", Toast.LENGTH_SHORT).show()

                    // Reset inputs
                    foodInput = ""
                    caloriesInput = ""
                } else {
                    Toast.makeText(
                        context,
                        "Please enter valid food and calorie values",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Diet Entry")
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Text("Diet Log", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

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
