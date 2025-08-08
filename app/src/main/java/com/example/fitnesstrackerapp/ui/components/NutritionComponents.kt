package com.example.fitnesstrackerapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fitnesstrackerapp.data.entity.FoodEntry

/**
 * UI components for nutrition tracking
 */
@Composable
fun NutritionSummary(
    foodEntries: List<FoodEntry>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Daily Summary",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            val totalCalories = foodEntries.sumOf { it.calories }
            val totalProtein = foodEntries.sumOf { it.protein }
            val totalCarbs = foodEntries.sumOf { it.carbs }
            val totalFat = foodEntries.sumOf { it.fat }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                NutritionItem("Calories", totalCalories.toString())
                NutritionItem("Protein", "${totalProtein.toInt()}g")
                NutritionItem("Carbs", "${totalCarbs.toInt()}g")
                NutritionItem("Fat", "${totalFat.toInt()}g")
            }
        }
    }
}

@Composable
private fun NutritionItem(
    label: String,
    value: String
) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
