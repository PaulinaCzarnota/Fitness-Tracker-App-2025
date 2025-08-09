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
 * Enhanced NutritionSummary with additional comprehensive nutritional data.
 * Extends the basic nutrition tracking with advanced micronutrients and health metrics.
 */
data class NutritionSummary(
    @ColumnInfo(name = "total_calories") val totalCalories: Double = 0.0,
    @ColumnInfo(name = "total_protein") val totalProtein: Double = 0.0,
    @ColumnInfo(name = "total_carbs") val totalCarbs: Double = 0.0,
    @ColumnInfo(name = "total_fat") val totalFat: Double = 0.0,
    @ColumnInfo(name = "total_saturated_fat") val totalSaturatedFat: Double = 0.0,
    @ColumnInfo(name = "total_trans_fat") val totalTransFat: Double = 0.0,
    @ColumnInfo(name = "total_cholesterol") val totalCholesterol: Double = 0.0,
    @ColumnInfo(name = "total_fiber") val totalFiber: Double = 0.0,
    @ColumnInfo(name = "total_sugar") val totalSugar: Double = 0.0,
    @ColumnInfo(name = "total_added_sugars") val totalAddedSugars: Double = 0.0,
    @ColumnInfo(name = "total_sodium") val totalSodium: Double = 0.0,
    @ColumnInfo(name = "total_potassium") val totalPotassium: Double = 0.0,
    @ColumnInfo(name = "total_vitamin_c") val totalVitaminC: Double = 0.0,
    @ColumnInfo(name = "total_vitamin_d") val totalVitaminD: Double = 0.0,
    @ColumnInfo(name = "total_calcium") val totalCalcium: Double = 0.0,
    @ColumnInfo(name = "total_iron") val totalIron: Double = 0.0,
) {
    /**
     * Calculates macronutrient distribution percentages.
     * @return Triple of (protein%, carbs%, fat%) percentages
     */
    fun getMacroDistribution(): Triple<Double, Double, Double> {
        if (totalCalories <= 0) return Triple(0.0, 0.0, 0.0)

        val proteinCalories = totalProtein * 4
        val carbCalories = totalCarbs * 4
        val fatCalories = totalFat * 9

        val proteinPercent = (proteinCalories / totalCalories) * 100
        val carbPercent = (carbCalories / totalCalories) * 100
        val fatPercent = (fatCalories / totalCalories) * 100

        return Triple(proteinPercent, carbPercent, fatPercent)
    }

    /**
     * Checks if macronutrient distribution is balanced according to dietary guidelines.
     * @return true if macros are within recommended ranges
     */
    fun isBalancedMacros(): Boolean {
        val (protein, carbs, fat) = getMacroDistribution()
        return protein in 10.0..35.0 && carbs in 45.0..65.0 && fat in 20.0..35.0
    }

    /**
     * Gets nutritional quality score based on nutrient density and balance.
     * @return Score from 0.0 to 10.0
     */
    fun getQualityScore(): Double {
        if (totalCalories <= 0) return 0.0

        var score = 0.0

        // Macronutrient balance (3 points max)
        val (protein, carbs, fat) = getMacroDistribution()
        if (protein in 15.0..30.0) score += 1.0
        if (carbs in 45.0..60.0) score += 1.0
        if (fat in 25.0..35.0) score += 1.0

        // Micronutrient density (4 points max)
        val fiberPer1000Cal = (totalFiber / totalCalories) * 1000
        val vitaminCPer1000Cal = (totalVitaminC / totalCalories) * 1000
        val calciumPer1000Cal = (totalCalcium / totalCalories) * 1000
        val ironPer1000Cal = (totalIron / totalCalories) * 1000

        if (fiberPer1000Cal >= 14) score += 1.0
        if (vitaminCPer1000Cal >= 45) score += 1.0
        if (calciumPer1000Cal >= 500) score += 1.0
        if (ironPer1000Cal >= 8) score += 1.0

        // Negative factors (deduct up to 3 points)
        val sodiumPer1000Cal = (totalSodium / totalCalories) * 1000
        val saturatedFatPercent = (totalSaturatedFat * 9) / totalCalories * 100
        val addedSugarPercent = (totalAddedSugars * 4) / totalCalories * 100

        if (sodiumPer1000Cal > 1150) score -= 1.0
        if (saturatedFatPercent > 10) score -= 1.0
        if (addedSugarPercent > 10) score -= 1.0

        // Base score
        score += 3.0

        return score.coerceIn(0.0, 10.0)
    }

    companion object {
        /**
         * Creates an empty nutrition summary.
         */
        fun empty(): NutritionSummary = NutritionSummary()
    }
}

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
