/**
 * Enhanced Nutrition Tracking System with Offline Food Database
 *
 * This comprehensive nutrition system provides:
 * - Offline food database with macro calculations
 * - Food search and logging with auto-complete
 * - Daily, weekly, and monthly nutrition summaries
 * - Macro breakdown visualization (pie charts)
 * - Meal planning and calorie goal tracking
 * - Custom food entry creation
 */

package com.example.fitnesstrackerapp.ui.nutrition

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitnesstrackerapp.data.entity.*
import com.example.fitnesstrackerapp.repository.NutritionSummary
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.text.SimpleDateFormat
import java.util.*

/**
 * Enhanced nutrition screen with comprehensive food tracking
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedNutritionScreen(
    nutritionViewModel: NutritionViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by nutritionViewModel.uiState.collectAsStateWithLifecycle()
    var selectedDate by remember { mutableStateOf(Date()) }
    var showAddFoodDialog by remember { mutableStateOf(false) }
    var showFoodSearchDialog by remember { mutableStateOf(false) }
    var selectedMealType by remember { mutableStateOf(MealType.BREAKFAST) }
    var showNutritionSummaryDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header with date selector
        NutritionHeader(
            selectedDate = selectedDate,
            onDateChange = {
                selectedDate = it
                nutritionViewModel.loadFoodEntriesForDate(it)
            },
            onShowSummary = { showNutritionSummaryDialog = true },
        )

        // Daily nutrition summary card
        DailyNutritionSummaryCard(
            totalCalories = uiState.totalCalories,
            dailyGoal = uiState.dailyCalorieGoal,
            proteinGrams = uiState.totalProtein,
            carbsGrams = uiState.totalCarbs,
            fatGrams = uiState.totalFat,
            fiberGrams = uiState.totalFiber,
        )

        // Macro breakdown chart
        MacroBreakdownChart(
            proteinGrams = uiState.totalProtein,
            carbsGrams = uiState.totalCarbs,
            fatGrams = uiState.totalFat,
        )

        // Meal sections
        MealType.entries.forEach { mealType ->
            MealSection(
                mealType = mealType,
                foodEntries = uiState.foodEntries.filter { it.mealType == mealType },
                onAddFood = {
                    selectedMealType = mealType
                    showFoodSearchDialog = true
                },
                onEditEntry = { entry ->
                    // Handle editing food entry
                },
                onDeleteEntry = { entry ->
                    nutritionViewModel.deleteFoodEntry(entry)
                },
            )
        }

        // Quick add custom food button
        OutlinedButton(
            onClick = {
                showAddFoodDialog = true
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Custom Food")
        }
    }

    // Dialogs
    if (showFoodSearchDialog) {
        FoodSearchDialog(
            mealType = selectedMealType,
            onDismiss = { showFoodSearchDialog = false },
            onFoodSelected = { food, quantity, servingSize ->
                nutritionViewModel.addFoodEntry(
                    foodName = food.name,
                    calories = (food.caloriesPerServing * quantity).toInt(),
                    proteinGrams = food.proteinPerServing * quantity,
                    carbsGrams = food.carbsPerServing * quantity,
                    fatGrams = food.fatPerServing * quantity,
                    fiberGrams = food.fiberPerServing * quantity,
                    mealType = selectedMealType,
                    servingSize = servingSize,
                    quantity = quantity,
                )
                showFoodSearchDialog = false
            },
        )
    }

    if (showAddFoodDialog) {
        AddCustomFoodDialog(
            mealType = selectedMealType,
            onDismiss = { showAddFoodDialog = false },
            onAddFood = { name, calories, protein, carbs, fat, fiber, servingSize, quantity ->
                nutritionViewModel.addFoodEntry(
                    foodName = name,
                    calories = calories,
                    proteinGrams = protein,
                    carbsGrams = carbs,
                    fatGrams = fat,
                    fiberGrams = fiber,
                    mealType = selectedMealType,
                    servingSize = servingSize,
                    quantity = quantity,
                )
                showAddFoodDialog = false
            },
        )
    }

    if (showNutritionSummaryDialog) {
        NutritionSummaryDialog(
            summary = uiState.nutritionSummary,
            onDismiss = { showNutritionSummaryDialog = false },
        )
    }

    // Handle loading state and errors
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    }

    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show snackbar for error
        }
    }
}

/**
 * Header with date selection and summary button
 */
@Composable
fun NutritionHeader(
    selectedDate: Date,
    onDateChange: (Date) -> Unit,
    onShowSummary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Nutrition Tracking",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )

                val dateFormat = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault())
                Text(
                    text = dateFormat.format(selectedDate),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Date picker button (simplified)
                IconButton(onClick = { /* Implement date picker */ }) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Select date")
                }

                // Summary button
                IconButton(onClick = onShowSummary) {
                    Icon(Icons.Default.Analytics, contentDescription = "View summary")
                }
            }
        }
    }
}

/**
 * Daily nutrition summary card with macros
 */
@Composable
fun DailyNutritionSummaryCard(
    totalCalories: Double,
    dailyGoal: Int,
    proteinGrams: Double,
    carbsGrams: Double,
    fatGrams: Double,
    fiberGrams: Double,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Today's Nutrition",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )

            // Calories progress
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Calories",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = "${totalCalories.toInt()} / $dailyGoal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }

                val progress = (totalCalories / dailyGoal.toDouble()).toFloat().coerceIn(0f, 1.5f)
                LinearProgressIndicator(
                    progress = { minOf(progress, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    color = if (progress > 1f) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                )

                if (progress > 1f) {
                    Text(
                        text = "Over target by ${((progress - 1) * dailyGoal).toInt()} calories",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }

            // Macronutrients grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                MacroItem(
                    label = "Protein",
                    value = "${proteinGrams.toInt()}g",
                    color = Color(0xFF4CAF50),
                )
                MacroItem(
                    label = "Carbs",
                    value = "${carbsGrams.toInt()}g",
                    color = Color(0xFF2196F3),
                )
                MacroItem(
                    label = "Fat",
                    value = "${fatGrams.toInt()}g",
                    color = Color(0xFFFF9800),
                )
                MacroItem(
                    label = "Fiber",
                    value = "${fiberGrams.toInt()}g",
                    color = Color(0xFF9C27B0),
                )
            }
        }
    }
}

/**
 * Individual macro item display
 */
@Composable
fun MacroItem(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * Macro breakdown pie chart
 */
@Composable
fun MacroBreakdownChart(
    proteinGrams: Double,
    carbsGrams: Double,
    fatGrams: Double,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "Macro Breakdown",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            if (proteinGrams + carbsGrams + fatGrams > 0) {
                AndroidView(
                    factory = { context ->
                        PieChart(context).apply {
                            description.isEnabled = false
                            isRotationEnabled = true
                            setUsePercentValues(true)
                            setDrawEntryLabels(false)
                            centerText = "Macros"
                            setCenterTextSize(16f)
                            holeRadius = 40f
                            transparentCircleRadius = 45f
                            legend.isEnabled = true
                        }
                    },
                    update = { pieChart ->
                        val entries = mutableListOf<PieEntry>()

                        if (proteinGrams > 0) {
                            entries.add(PieEntry(proteinGrams.toFloat(), "Protein"))
                        }
                        if (carbsGrams > 0) {
                            entries.add(PieEntry(carbsGrams.toFloat(), "Carbs"))
                        }
                        if (fatGrams > 0) {
                            entries.add(PieEntry(fatGrams.toFloat(), "Fat"))
                        }

                        if (entries.isNotEmpty()) {
                            val dataSet = PieDataSet(entries, "").apply {
                                colors = listOf(
                                    Color(0xFF4CAF50).toArgb(), // Green for protein
                                    Color(0xFF2196F3).toArgb(), // Blue for carbs
                                    Color(0xFFFF9800).toArgb(), // Orange for fat
                                )
                                valueTextSize = 12f
                                valueTextColor = android.graphics.Color.WHITE
                            }

                            pieChart.data = PieData(dataSet)
                            pieChart.animateY(1000)
                            pieChart.invalidate()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No nutrition data yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/**
 * Meal section with food entries
 */
@Composable
fun MealSection(
    mealType: MealType,
    foodEntries: List<FoodEntry>,
    onAddFood: () -> Unit,
    onEditEntry: (FoodEntry) -> Unit,
    onDeleteEntry: (FoodEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Meal header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        when (mealType) {
                            MealType.BREAKFAST -> Icons.Default.LightMode
                            MealType.LUNCH -> Icons.Default.WbSunny
                            MealType.DINNER -> Icons.Default.DarkMode
                            MealType.SNACK -> Icons.Default.Cookie
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = mealType.name.lowercase()
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                val mealCalories = foodEntries.sumOf { it.getTotalCalories() }
                Text(
                    text = "${mealCalories.toInt()} kcal",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            // Food entries
            if (foodEntries.isEmpty()) {
                OutlinedButton(
                    onClick = onAddFood,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Food")
                }
            } else {
                foodEntries.forEach { entry ->
                    FoodEntryCard(
                        entry = entry,
                        onEdit = { onEditEntry(entry) },
                        onDelete = { onDeleteEntry(entry) },
                    )
                }

                OutlinedButton(
                    onClick = onAddFood,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add More")
                }
            }
        }
    }
}

/**
 * Individual food entry card
 */
@Composable
fun FoodEntryCard(
    entry: FoodEntry,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.foodName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )

                    if (entry.servingSize.isNotBlank()) {
                        Text(
                            text = "${entry.quantity}x ${entry.servingSize}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "${entry.getTotalCalories().toInt()} kcal",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                    )

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(20.dp),
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }

            // Macro breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                MacroText("P: ${entry.getTotalProtein().toInt()}g")
                MacroText("C: ${entry.getTotalCarbs().toInt()}g")
                MacroText("F: ${entry.getTotalFat().toInt()}g")
                if (entry.getTotalFiber() > 0) {
                    MacroText("Fi: ${entry.getTotalFiber().toInt()}g")
                }
            }
        }
    }
}

/**
 * Macro text component
 */
@Composable
fun MacroText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/**
 * Food search dialog with offline database
 */
@Composable
fun FoodSearchDialog(
    mealType: MealType,
    onDismiss: () -> Unit,
    onFoodSelected: (FoodItem, Float, String) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFood by remember { mutableStateOf<FoodItem?>(null) }
    var quantity by remember { mutableStateOf("1") }
    var servingSize by remember { mutableStateOf("") }

    // Mock food database - in a real app, this would be loaded from JSON or database
    val foodDatabase = remember {
        listOf(
            FoodItem("Apple", 52.0, 0.3, 14.0, 0.2, 2.4, "medium (182g)"),
            FoodItem("Banana", 89.0, 1.1, 23.0, 0.3, 2.6, "medium (118g)"),
            FoodItem("White Rice", 130.0, 2.7, 28.0, 0.3, 0.4, "100g"),
            FoodItem("Chicken Breast", 165.0, 31.0, 0.0, 3.6, 0.0, "100g"),
            FoodItem("Broccoli", 34.0, 2.8, 7.0, 0.4, 2.6, "100g"),
            FoodItem("Whole Wheat Bread", 247.0, 13.0, 41.0, 4.2, 7.0, "100g"),
            FoodItem("Greek Yogurt", 59.0, 10.0, 3.6, 0.4, 0.0, "100g"),
            FoodItem("Almonds", 579.0, 21.0, 22.0, 50.0, 12.0, "100g"),
            FoodItem("Salmon", 208.0, 25.0, 0.0, 12.0, 0.0, "100g"),
            FoodItem("Sweet Potato", 86.0, 1.6, 20.0, 0.1, 3.0, "100g"),
        )
    }

    val filteredFoods = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            foodDatabase
        } else {
            foodDatabase.filter {
                it.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Add Food - ${mealType.name}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search foods...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                // Food list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    items(filteredFoods) { food ->
                        FoodSearchItem(
                            food = food,
                            isSelected = selectedFood == food,
                            onSelect = {
                                selectedFood = food
                                servingSize = food.servingSize
                            },
                        )
                    }
                }

                // Selection details
                selectedFood?.let { food ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = food.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                OutlinedTextField(
                                    value = quantity,
                                    onValueChange = { quantity = it },
                                    label = { Text("Quantity") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f),
                                )

                                OutlinedTextField(
                                    value = servingSize,
                                    onValueChange = { servingSize = it },
                                    label = { Text("Serving") },
                                    modifier = Modifier.weight(2f),
                                )
                            }

                            val qty = quantity.toFloatOrNull() ?: 1f
                            Text(
                                text = "Nutrition: ${(food.caloriesPerServing * qty).toInt()} kcal, " +
                                    "${(food.proteinPerServing * qty).toInt()}g protein, " +
                                    "${(food.carbsPerServing * qty).toInt()}g carbs, " +
                                    "${(food.fatPerServing * qty).toInt()}g fat",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            selectedFood?.let { food ->
                                val qty = quantity.toFloatOrNull() ?: 1f
                                onFoodSelected(food, qty, servingSize)
                            }
                        },
                        enabled = selectedFood != null && quantity.toFloatOrNull() != null,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Add Food")
                    }
                }
            }
        }
    }
}

/**
 * Food search item
 */
@Composable
fun FoodSearchItem(
    food: FoodItem,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = food.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Per ${food.servingSize}: ${food.caloriesPerServing.toInt()} kcal",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

/**
 * Add custom food dialog
 */
@Composable
fun AddCustomFoodDialog(
    mealType: MealType,
    onDismiss: () -> Unit,
    onAddFood: (String, Int, Double, Double, Double, Double, String, Float) -> Unit,
) {
    var foodName by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var fiber by remember { mutableStateOf("") }
    var servingSize by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Add Custom Food - ${mealType.name}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )

                OutlinedTextField(
                    value = foodName,
                    onValueChange = { foodName = it },
                    label = { Text("Food Name") },
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = servingSize,
                    onValueChange = { servingSize = it },
                    label = { Text("Serving Size (e.g., '100g', '1 cup')") },
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it },
                    label = { Text("Calories per serving") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = protein,
                        onValueChange = { protein = it },
                        label = { Text("Protein (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                    )

                    OutlinedTextField(
                        value = carbs,
                        onValueChange = { carbs = it },
                        label = { Text("Carbs (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = fat,
                        onValueChange = { fat = it },
                        label = { Text("Fat (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                    )

                    OutlinedTextField(
                        value = fiber,
                        onValueChange = { fiber = it },
                        label = { Text("Fiber (g)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            onAddFood(
                                foodName,
                                calories.toIntOrNull() ?: 0,
                                protein.toDoubleOrNull() ?: 0.0,
                                carbs.toDoubleOrNull() ?: 0.0,
                                fat.toDoubleOrNull() ?: 0.0,
                                fiber.toDoubleOrNull() ?: 0.0,
                                servingSize,
                                quantity.toFloatOrNull() ?: 1f,
                            )
                        },
                        enabled = foodName.isNotBlank() && calories.isNotBlank(),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Add Food")
                    }
                }
            }
        }
    }
}

/**
 * Nutrition summary dialog
 */
@Composable
fun NutritionSummaryDialog(
    summary: NutritionSummary?,
    onDismiss: () -> Unit,
) {
    summary?.let { nutritionSummary ->
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Nutrition Summary") },
            text = {
                Column {
                    Text("Total Calories: ${nutritionSummary.totalCalories.toInt()}")
                    Text("Protein: ${nutritionSummary.totalProtein.toInt()}g")
                    Text("Carbohydrates: ${nutritionSummary.totalCarbohydrates.toInt()}g")
                    Text("Fat: ${nutritionSummary.totalFat.toInt()}g")
                    Text("Fiber: ${nutritionSummary.totalFiber.toInt()}g")
                    Text("Food Entries: ${nutritionSummary.entryCount}")
                }
            },
            confirmButton = {
                Button(onClick = onDismiss) {
                    Text("Close")
                }
            },
        )
    }
}

/**
 * Food item data class for offline database
 */
data class FoodItem(
    val name: String,
    val caloriesPerServing: Double,
    val proteinPerServing: Double,
    val carbsPerServing: Double,
    val fatPerServing: Double,
    val fiberPerServing: Double,
    val servingSize: String,
)
