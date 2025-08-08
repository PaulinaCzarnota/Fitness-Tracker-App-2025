package com.example.fitnesstrackerapp.data.model

import androidx.room.ColumnInfo

/**
 * Data class representing macronutrient totals for a specific period.
 */
data class MacroTotals(
    @ColumnInfo(name = "total_protein") val totalProtein: Double?,
    @ColumnInfo(name = "total_carbs") val totalCarbs: Double?,
    @ColumnInfo(name = "total_fat") val totalFat: Double?,
)

/**
 * Data class representing comprehensive nutrition summary for a date range.
 */
data class NutritionSummary(
    @ColumnInfo(name = "total_calories") val totalCalories: Double?,
    @ColumnInfo(name = "total_protein") val totalProtein: Double?,
    @ColumnInfo(name = "total_carbs") val totalCarbs: Double?,
    @ColumnInfo(name = "total_fat") val totalFat: Double?,
    @ColumnInfo(name = "total_fiber") val totalFiber: Double?,
    @ColumnInfo(name = "total_sugar") val totalSugar: Double?,
    @ColumnInfo(name = "total_sodium") val totalSodium: Double?,
)

/**
 * Data class representing meal type calorie distribution.
 */
data class MealDistribution(
    @ColumnInfo(name = "meal_type") val mealType: String,
    @ColumnInfo(name = "calories") val calories: Double?,
)

/**
 * Data class representing most consumed food statistics.
 */
data class FoodConsumptionStats(
    @ColumnInfo(name = "food_name") val foodName: String,
    @ColumnInfo(name = "frequency") val frequency: Int,
    @ColumnInfo(name = "avg_calories") val avgCalories: Double?,
)

/**
 * Data class representing weekly nutrition summary.
 */
data class WeeklyNutritionSummary(
    @ColumnInfo(name = "weekly_calories") val weeklyCalories: Double?,
    @ColumnInfo(name = "avg_daily_calories") val avgDailyCalories: Double?,
    @ColumnInfo(name = "weekly_protein") val weeklyProtein: Double?,
    @ColumnInfo(name = "weekly_carbs") val weeklyCarbs: Double?,
    @ColumnInfo(name = "weekly_fat") val weeklyFat: Double?,
)
