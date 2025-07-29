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
import androidx.navigation.NavHostController
import com.example.fitnesstrackerapp.data.Diet
import com.example.fitnesstrackerapp.viewmodel.DietViewModel
import com.example.fitnesstrackerapp.viewmodel.DietViewModelFactory
import com.example.fitnesstrackerapp.ui.components.BottomNavigationBar
import com.example.fitnesstrackerapp.util.formatDate

/**
 * DietScreen
 *
 * Allows users to log their daily food intake and track nutrition.
 * Provides functionality to add, edit, delete, and view diet entries.
 *
 * @param navController Optional navigation controller for consistency with Navigation.kt.
 */
@Composable
fun DietScreen(navController: NavHostController? = null) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    // Initialize ViewModel with application context
    val viewModel: DietViewModel = viewModel(
        factory = DietViewModelFactory(application)
    )

    // State variables for diet input fields
    var food by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var editingDietId by remember { mutableStateOf<Int?>(null) }

    // Observe all diets from ViewModel
    val diets by viewModel.allDiets.observeAsState(initial = emptyList())

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
            // --- Input Form ---
            Text("Log Food Intake", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = food,
                onValueChange = { food = it },
                label = { Text("Food") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = calories,
                onValueChange = { calories = it },
                label = { Text("Calories") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Add or Update Entry Button
            Button(
                onClick = {
                    val foodTrimmed = food.trim()
                    val caloriesInt = calories.toIntOrNull()

                    if (foodTrimmed.isNotBlank() && caloriesInt != null && caloriesInt > 0) {
                        val diet = Diet(
                            id = editingDietId ?: 0,
                            food = foodTrimmed,
                            calories = caloriesInt,
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

                        // Reset fields
                        food = ""
                        calories = ""
                    } else {
                        Toast.makeText(context, "Enter valid food and calories", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (editingDietId == null) "Add Diet Entry" else "Update Entry")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Clear All Button
            Button(
                onClick = {
                    viewModel.resetAllDiets()
                    Toast.makeText(context, "All entries cleared", Toast.LENGTH_SHORT).show()
                    // Reset form fields as well
                    food = ""
                    calories = ""
                    editingDietId = null
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Clear All")
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // --- Diet Log Section ---
            Text("Diet Log", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (diets.isEmpty()) {
                Text("No entries yet.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(diets, key = { it.id }) { diet ->
                        DietCard(
                            diet = diet,
                            onEdit = {
                                food = it.food
                                calories = it.calories.toString()
                                editingDietId = it.id
                            },
                            onDelete = {
                                viewModel.removeDiet(it)
                                Toast.makeText(context, "Entry deleted", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * DietCard
 *
 * Displays a single diet log entry with edit and delete options.
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
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
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
                OutlinedButton(
                    onClick = { onDelete(diet) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            }
        }
    }
}
