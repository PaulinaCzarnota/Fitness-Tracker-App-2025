package com.example.fitnesstrackerapp.ui.nutrition

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitnesstrackerapp.ViewModelFactoryProvider
import com.example.fitnesstrackerapp.data.entity.FoodEntry
import com.example.fitnesstrackerapp.data.entity.MealType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionScreen(
    modifier: Modifier = Modifier,
    authViewModel: com.example.fitnesstrackerapp.ui.auth.AuthViewModel,
) {
    val activity = LocalContext.current as ComponentActivity
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    val userId = authState.user?.id ?: 1L

    // Initialize NutritionViewModel with user ID
    val nutritionViewModel: NutritionViewModel = ViewModelFactoryProvider.getNutritionViewModel(activity, userId)
    val uiState by nutritionViewModel.uiState.collectAsState()
    var showAddFoodDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        // Header
        Text(
            text = "Nutrition",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Daily summary card
        DailySummaryCard(
            totalCalories = uiState.totalCalories.toInt(),
            targetCalories = 2000,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Add food button
        Button(
            onClick = { showAddFoodDialog = true },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Food")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Food entries list
        LazyColumn {
            items(uiState.foodEntries) { entry ->
                FoodEntryItem(entry = entry)
            }
        }
    }

    if (showAddFoodDialog) {
        AddFoodEntryDialog(
            onDismiss = { showAddFoodDialog = false },
            onAddFoodEntry = { name, calories, mealType ->
                nutritionViewModel.addFoodEntry(name, calories.toDouble(), mealType)
                showAddFoodDialog = false
            },
        )
    }
}

@Composable
private fun DailySummaryCard(
    totalCalories: Int,
    targetCalories: Int,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "Today's Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$totalCalories / $targetCalories kcal",
                style = MaterialTheme.typography.headlineSmall,
            )
        }
    }
}

@Composable
fun FoodEntryItem(entry: FoodEntry) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(text = entry.foodName, style = MaterialTheme.typography.titleMedium)
            Text(text = "Meal: ${entry.mealType}", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "${entry.calories} kcal", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodEntryDialog(onDismiss: () -> Unit, onAddFoodEntry: (String, Int, MealType) -> Unit) {
    var name by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var selectedMealType by remember { mutableStateOf(MealType.BREAKFAST) }
    var expandedMealType by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Food Entry") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Food Name") },
                )
                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it },
                    label = { Text("Calories") },
                )

                // Meal Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedMealType,
                    onExpandedChange = { expandedMealType = !expandedMealType },
                ) {
                    OutlinedTextField(
                        value = selectedMealType.name,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Meal Type") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMealType)
                        },
                        modifier = Modifier.menuAnchor(),
                    )
                    ExposedDropdownMenu(
                        expanded = expandedMealType,
                        onDismissRequest = { expandedMealType = false },
                    ) {
                        MealType.entries.forEach { mealType ->
                            DropdownMenuItem(
                                text = { Text(mealType.name) },
                                onClick = {
                                    selectedMealType = mealType
                                    expandedMealType = false
                                },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAddFoodEntry(name, calories.toIntOrNull() ?: 0, selectedMealType)
                },
                enabled = name.isNotBlank() && calories.isNotBlank(),
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
