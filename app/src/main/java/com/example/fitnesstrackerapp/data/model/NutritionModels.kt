package com.example.fitnesstrackerapp.data.model

/**
 * Data model for daily nutrition summaries.
 * Used for tracking nutritional intake patterns over time.
 */
data class DailyNutritionSummary(
    val date: String,
    val totalCalories: Double,
    val totalProtein: Double,
    val totalCarbs: Double,
    val totalFat: Double,
    val totalFiber: Double,
    val totalSodium: Double,
    val entryCount: Int,
)

/**
 * Data model for notification analytics and performance metrics.
 * Used for analyzing notification delivery success rates and user engagement.
 */
data class NotificationAnalytics(
    val deliveryChannel: String,
    val totalAttempts: Int,
    val successfulAttempts: Int,
    val successRate: Double,
)

/**
 * Data model for comprehensive notification performance metrics.
 * Used for monitoring and optimizing notification system performance.
 */
data class NotificationPerformanceMetrics(
    val totalEvents: Int,
    val avgProcessingDuration: Double?,
    val minProcessingDuration: Double?,
    val maxProcessingDuration: Double?,
    val avgDeliveryDuration: Double?,
    val successfulEvents: Int,
    val failedEvents: Int,
) {
    /**
     * Calculates the success rate as a percentage.
     */
    fun getSuccessRate(): Double {
        return if (totalEvents > 0) (successfulEvents.toDouble() / totalEvents) * 100 else 0.0
    }

    /**
     * Calculates the failure rate as a percentage.
     */
    fun getFailureRate(): Double {
        return if (totalEvents > 0) (failedEvents.toDouble() / totalEvents) * 100 else 0.0
    }
}

/**
 * Enhanced NutritionSummary with additional comprehensive nutritional data.
 * Extends the basic nutrition tracking with advanced micronutrients and health metrics.
 */
data class NutritionSummary(
    val totalCalories: Double = 0.0,
    val totalProtein: Double = 0.0,
    val totalCarbs: Double = 0.0,
    val totalFat: Double = 0.0,
    val totalSaturatedFat: Double = 0.0,
    val totalTransFat: Double = 0.0,
    val totalCholesterol: Double = 0.0,
    val totalFiber: Double = 0.0,
    val totalSugar: Double = 0.0,
    val totalAddedSugars: Double = 0.0,
    val totalSodium: Double = 0.0,
    val totalPotassium: Double = 0.0,
    val totalVitaminC: Double = 0.0,
    val totalVitaminD: Double = 0.0,
    val totalCalcium: Double = 0.0,
    val totalIron: Double = 0.0,
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
