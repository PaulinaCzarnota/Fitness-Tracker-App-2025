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
import androidx.compose.runtime.livedata.observeAsState
import com.example.fitnesstrackerapp.data.Diet
import com.example.fitnesstrackerapp.util.formatDate
import com.example.fitnesstrackerapp.viewmodel.DietViewModel
import com.example.fitnesstrackerapp.viewmodel.DietViewModelFactory

/**
 * DietScreen
 *
 * Screen to log and display diet entries (food + calories).
 * Integrates with Room database via DietViewModel.
 * Supports add, edit, delete, and clear operations.
 */
@Composable
fun DietScreen() {
    val context = LocalContext.current

    // Use DietViewModelFactory to construct the ViewModel with database access
    val viewModel: DietViewModel = viewModel(
        factory = DietViewModelFactory(context.applicationContext as Application)
    )

    // State for form inputs and edit tracking
    var foodInput by remember { mutableStateOf("") }
    var caloriesInput by remember { mutableStateOf("") }
    var editingDietId by remember { mutableStateOf<Int?>(null) }

    // Live data list of diet logs
    val dietList by viewModel.allDiets.observeAsState(emptyList())

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

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

        // Calories input
        OutlinedTextField(
            value = caloriesInput,
            onValueChange = { caloriesInput = it },
            label = { Text("Calories") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Add or Update button
        Button(
            onClick = {
                val food = foodInput.trim()
                val calories = caloriesInput.toIntOrNull()

                if (food.isNotBlank() && calories != null && calories > 0) {
                    val diet = Diet(
                        id = editingDietId ?: 0,
                        food = food,
                        calories = calories,
                        date = System.currentTimeMillis()
                    )

                    if (editingDietId == null) {
                        viewModel.addDiet(diet)
                        Toast.makeText(context, "Diet entry added", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.editDiet(diet)
                        Toast.makeText(context, "Diet entry updated", Toast.LENGTH_SHORT).show()
                        editingDietId = null
                    }

                    foodInput = ""
                    caloriesInput = ""
                } else {
                    Toast.makeText(context, "Please enter valid food and calories", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (editingDietId == null) "Add Diet Entry" else "Update Entry")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Clear All button
        Button(
            onClick = {
                viewModel.resetAllDiets()
                Toast.makeText(context, "All entries cleared", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Clear All")
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        // Diet log list
        Text("Diet Log", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        if (dietList.isEmpty()) {
            Text("No entries yet.")
        } else {
            LazyColumn {
                items(dietList) { diet ->
                    DietCard(
                        diet = diet,
                        onEdit = {
                            foodInput = it.food
                            caloriesInput = it.calories.toString()
                            editingDietId = it.id
                        },
                        onDelete = {
                            viewModel.removeDiet(it)
                            Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

/**
 * DietCard
 *
 * Displays one diet entry in the list.
 * Provides Edit and Delete buttons for interaction.
 */
@Composable
fun DietCard(
    diet: Diet,
    onEdit: (Diet) -> Unit,
    onDelete: (Diet) -> Unit
) {
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
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                OutlinedButton(onClick = { onEdit(diet) }) {
                    Text("Edit")
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(onClick = { onDelete(diet) }) {
                    Text("Delete")
                }
            }
        }
    }
}
