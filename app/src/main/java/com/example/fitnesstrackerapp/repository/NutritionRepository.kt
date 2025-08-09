/**
 * Repository for handling nutrition-related data operations in the Fitness Tracker application.
 *
 * This repository provides a clean API for nutrition data management, abstracting the
 * database layer and providing business logic for nutrition tracking operations. It handles
 * CRUD operations, nutritional analysis, meal planning, and dietary recommendations.
 *
 * Key Features:
 * - Complete CRUD operations for nutrition entries
 * - Advanced nutritional analysis and calculations
 * - Dietary pattern recognition and recommendations
 * - Nutritional goal tracking and progress monitoring
 * - Food quality scoring and health insights
 * - Meal planning support and optimization
 * - Micronutrient deficiency detection
 * - Data caching for improved performance
 */

package com.example.fitnesstrackerapp.repository

import com.example.fitnesstrackerapp.data.dao.NutritionEntryDao
import com.example.fitnesstrackerapp.data.entity.MealType
import com.example.fitnesstrackerapp.data.entity.NutritionEntry
import com.example.fitnesstrackerapp.data.model.DailyNutritionSummary
import com.example.fitnesstrackerapp.data.model.FoodConsumptionStats
import com.example.fitnesstrackerapp.data.model.MacroTotals
import com.example.fitnesstrackerapp.data.model.MealDistribution
import com.example.fitnesstrackerapp.data.model.NutritionSummary
import com.example.fitnesstrackerapp.data.model.WeeklyNutritionSummary
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.Date

/**
 * Repository for handling nutrition-related data operations.
 *
 * Provides a high-level interface for nutrition data management, including
 * persistence operations, nutritional analysis, and dietary insights.
 * All operations are designed to be thread-safe and use coroutines for async execution.
 */
class NutritionRepository(
    private val nutritionEntryDao: NutritionEntryDao,
) {
    // MARK: - Basic CRUD Operations

    /**
     * Inserts a new nutrition entry into the database.
     *
     * @param nutritionEntry The nutrition entry to be inserted
     * @return The auto-generated ID of the inserted nutrition entry
     * @throws Exception if the insertion fails
     */
    suspend fun insertNutritionEntry(nutritionEntry: NutritionEntry): Long {
        require(nutritionEntry.isValid()) { "Invalid nutrition entry data" }
        return nutritionEntryDao.insertNutritionEntry(nutritionEntry)
    }

    /**
     * Updates an existing nutrition entry in the database.
     *
     * @param nutritionEntry The nutrition entry with updated information
     * @throws Exception if the update fails or entry doesn't exist
     */
    suspend fun updateNutritionEntry(nutritionEntry: NutritionEntry) {
        require(nutritionEntry.isValid()) { "Invalid nutrition entry data" }
        nutritionEntryDao.updateNutritionEntry(nutritionEntry)
    }

    /**
     * Deletes a nutrition entry from the database by its ID.
     *
     * @param nutritionEntryId The unique identifier of the nutrition entry to delete
     * @throws Exception if the deletion fails
     */
    suspend fun deleteNutritionEntry(nutritionEntryId: Long) {
        nutritionEntryDao.deleteNutritionEntryById(nutritionEntryId)
    }

    /**
     * Gets a nutrition entry by its ID.
     *
     * @param nutritionEntryId The unique identifier of the nutrition entry
     * @return NutritionEntry entity or null if not found
     */
    suspend fun getNutritionEntryById(nutritionEntryId: Long): NutritionEntry? {
        return nutritionEntryDao.getNutritionEntryById(nutritionEntryId)
    }

    /**
     * Gets all nutrition entries for a specific user as a reactive Flow.
     *
     * @param userId The unique identifier of the user
     * @return Flow emitting list of nutrition entries, updates automatically when data changes
     */
    fun getNutritionEntriesByUserId(userId: Long): Flow<List<NutritionEntry>> {
        return nutritionEntryDao.getNutritionEntriesByUserId(userId)
    }

    /**
     * Gets recent nutrition entries for a user, limited by count.
     *
     * @param userId The unique identifier of the user
     * @param limit The maximum number of recent entries to retrieve
     * @return Flow emitting list of recent nutrition entries ordered by date (newest first)
     */
    fun getRecentNutritionEntries(userId: Long, limit: Int = 20): Flow<List<NutritionEntry>> {
        return nutritionEntryDao.getRecentNutritionEntries(userId, limit)
    }

    // MARK: - Date-based Operations

    /**
     * Gets nutrition entries for a specific date.
     *
     * @param userId The unique identifier of the user
     * @param date The date to retrieve entries for
     * @return Flow emitting list of nutrition entries for the specified date
     */
    fun getNutritionEntriesForDate(userId: Long, date: Date): Flow<List<NutritionEntry>> {
        return nutritionEntryDao.getNutritionEntriesForDate(userId, date)
    }

    /**
     * Gets nutrition entries for a date range.
     *
     * @param userId The unique identifier of the user
     * @param startDate The start date (inclusive) of the range
     * @param endDate The end date (inclusive) of the range
     * @return Flow emitting list of nutrition entries within the specified date range
     */
    fun getNutritionEntriesForDateRange(userId: Long, startDate: Date, endDate: Date): Flow<List<NutritionEntry>> {
        return nutritionEntryDao.getNutritionEntriesForDateRange(userId, startDate, endDate)
    }

    /**
     * Gets nutrition entries for today.
     *
     * @param userId The unique identifier of the user
     * @return Flow emitting list of today's nutrition entries
     */
    fun getTodaysNutritionEntries(userId: Long): Flow<List<NutritionEntry>> {
        val today = Date()
        return getNutritionEntriesForDate(userId, today)
    }

    // MARK: - Meal Type Operations

    /**
     * Gets nutrition entries by meal type for a specific date.
     *
     * @param userId The unique identifier of the user
     * @param mealType The type of meal to filter by
     * @param date The specific date (defaults to today)
     * @return Flow emitting list of nutrition entries for the meal type
     */
    fun getNutritionEntriesByMealType(userId: Long, mealType: MealType, date: Date = Date()): Flow<List<NutritionEntry>> {
        return nutritionEntryDao.getNutritionEntriesByMealType(userId, mealType, date)
    }

    /**
     * Gets meal distribution statistics for a date.
     *
     * @param userId The unique identifier of the user
     * @param date The date to analyze (defaults to today)
     * @return List of meal type calorie distributions
     */
    suspend fun getMealDistribution(userId: Long, date: Date = Date()): List<MealDistribution> {
        return nutritionEntryDao.getMealDistributionForDate(userId, date)
    }

    // MARK: - Nutritional Analysis

    /**
     * Gets total calories consumed for a specific date.
     *
     * @param userId The unique identifier of the user
     * @param date The date to analyze (defaults to today)
     * @return Total calories consumed, or 0.0 if no entries found
     */
    suspend fun getTotalCaloriesForDate(userId: Long, date: Date = Date()): Double {
        return nutritionEntryDao.getTotalCaloriesForDate(userId, date) ?: 0.0
    }

    /**
     * Gets macronutrient totals for a specific date.
     *
     * @param userId The unique identifier of the user
     * @param date The date to analyze (defaults to today)
     * @return MacroTotals containing protein, carbs, and fat totals
     */
    suspend fun getMacroTotalsForDate(userId: Long, date: Date = Date()): MacroTotals {
        return nutritionEntryDao.getTotalMacrosForDate(userId, date) ?: MacroTotals(0.0, 0.0, 0.0)
    }

    /**
     * Gets comprehensive nutrition summary for a specific date.
     *
     * @param userId The unique identifier of the user
     * @param date The date to analyze (defaults to today)
     * @return Complete nutrition summary including all tracked nutrients
     */
    suspend fun getNutritionSummaryForDate(userId: Long, date: Date = Date()): NutritionSummary {
        return nutritionEntryDao.getNutritionSummaryForDate(userId, date) ?: NutritionSummary.empty()
    }

    /**
     * Gets nutrition summary for a date range.
     *
     * @param userId The unique identifier of the user
     * @param startDate The start date of the range
     * @param endDate The end date of the range
     * @return Aggregated nutrition summary for the date range
     */
    suspend fun getNutritionSummaryForDateRange(userId: Long, startDate: Date, endDate: Date): NutritionSummary {
        return nutritionEntryDao.getNutritionSummaryForDateRange(userId, startDate, endDate) ?: NutritionSummary.empty()
    }

    /**
     * Gets weekly nutrition summary from Monday to Sunday of the current week.
     *
     * @param userId The unique identifier of the user
     * @return WeeklyNutritionSummary containing aggregated weekly statistics
     */
    suspend fun getWeeklyNutritionSummary(userId: Long): WeeklyNutritionSummary {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val weekStart = calendar.time

        calendar.add(Calendar.DAY_OF_WEEK, 6)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        val weekEnd = calendar.time

        return nutritionEntryDao.getWeeklyNutritionSummary(userId, weekStart, weekEnd)
            ?: WeeklyNutritionSummary(0.0, 0.0, 0.0, 0.0, 0.0)
    }

    /**
     * Gets average daily calories for a user.
     *
     * @param userId The unique identifier of the user
     * @return Average daily calorie intake, or null if no data available
     */
    suspend fun getAverageDailyCalories(userId: Long): Double? {
        return nutritionEntryDao.getAverageDailyCalories(userId)
    }

    // MARK: - Food Analysis and Insights

    /**
     * Gets most frequently consumed foods.
     *
     * @param userId The unique identifier of the user
     * @param limit The maximum number of foods to return
     * @return List of most consumed foods with frequency and calorie information
     */
    suspend fun getMostConsumedFoods(userId: Long, limit: Int = 10): List<FoodConsumptionStats> {
        return nutritionEntryDao.getMostConsumedFoods(userId, limit)
    }

    /**
     * Gets highest calorie foods consumed.
     *
     * @param userId The unique identifier of the user
     * @param limit The maximum number of foods to return
     * @return List of highest calorie foods with consumption statistics
     */
    suspend fun getHighestCalorieFoods(userId: Long, limit: Int = 10): List<FoodConsumptionStats> {
        return nutritionEntryDao.getHighestCalorieFoods(userId, limit)
    }

    /**
     * Gets foods with the best nutritional quality scores.
     *
     * @param userId The unique identifier of the user
     * @param limit The maximum number of foods to return
     * @return List of highest quality foods based on nutritional density
     */
    suspend fun getBestQualityFoods(userId: Long, limit: Int = 10): List<FoodConsumptionStats> {
        return nutritionEntryDao.getBestQualityFoods(userId, limit)
    }

    // MARK: - Search and Filtering

    /**
     * Searches nutrition entries by food name.
     *
     * @param userId The unique identifier of the user
     * @param searchQuery The search term for food names
     * @return Flow emitting list of matching nutrition entries
     */
    fun searchNutritionEntries(userId: Long, searchQuery: String): Flow<List<NutritionEntry>> {
        return nutritionEntryDao.searchNutritionEntries(userId, searchQuery)
    }

    /**
     * Gets nutrition entries by brand name.
     *
     * @param userId The unique identifier of the user
     * @param brandName The brand name to filter by
     * @return Flow emitting list of nutrition entries from the specified brand
     */
    fun getNutritionEntriesByBrand(userId: Long, brandName: String): Flow<List<NutritionEntry>> {
        return nutritionEntryDao.getNutritionEntriesByBrand(userId, brandName)
    }

    /**
     * Gets homemade food entries.
     *
     * @param userId The unique identifier of the user
     * @return Flow emitting list of homemade nutrition entries
     */
    fun getHomemadeNutritionEntries(userId: Long): Flow<List<NutritionEntry>> {
        return nutritionEntryDao.getHomemadeNutritionEntries(userId)
    }

    /**
     * Gets high-confidence nutrition entries (reliable nutritional data).
     *
     * @param userId The unique identifier of the user
     * @param minConfidence The minimum confidence level (0.0 to 1.0)
     * @return Flow emitting list of high-confidence nutrition entries
     */
    fun getHighConfidenceNutritionEntries(userId: Long, minConfidence: Double = 0.8): Flow<List<NutritionEntry>> {
        return nutritionEntryDao.getHighConfidenceNutritionEntries(userId, minConfidence)
    }

    // MARK: - Micronutrient Analysis

    /**
     * Gets vitamin C intake for a date range.
     *
     * @param userId The unique identifier of the user
     * @param startDate The start date of the range
     * @param endDate The end date of the range
     * @return Total vitamin C intake in mg, or 0.0 if no data available
     */
    suspend fun getVitaminCIntake(userId: Long, startDate: Date, endDate: Date): Double {
        return nutritionEntryDao.getTotalVitaminCForDateRange(userId, startDate, endDate) ?: 0.0
    }

    /**
     * Gets calcium intake for a date range.
     *
     * @param userId The unique identifier of the user
     * @param startDate The start date of the range
     * @param endDate The end date of the range
     * @return Total calcium intake in mg, or 0.0 if no data available
     */
    suspend fun getCalciumIntake(userId: Long, startDate: Date, endDate: Date): Double {
        return nutritionEntryDao.getTotalCalciumForDateRange(userId, startDate, endDate) ?: 0.0
    }

    /**
     * Gets iron intake for a date range.
     *
     * @param userId The unique identifier of the user
     * @param startDate The start date of the range
     * @param endDate The end date of the range
     * @return Total iron intake in mg, or 0.0 if no data available
     */
    suspend fun getIronIntake(userId: Long, startDate: Date, endDate: Date): Double {
        return nutritionEntryDao.getTotalIronForDateRange(userId, startDate, endDate) ?: 0.0
    }

    /**
     * Gets sodium intake for a specific date.
     *
     * @param userId The unique identifier of the user
     * @param date The date to analyze (defaults to today)
     * @return Total sodium intake in mg, or 0.0 if no data available
     */
    suspend fun getSodiumIntakeForDate(userId: Long, date: Date = Date()): Double {
        return nutritionEntryDao.getTotalSodiumForDate(userId, date) ?: 0.0
    }

    // MARK: - Health Insights and Recommendations

    /**
     * Analyzes nutritional quality and provides health insights.
     *
     * @param userId The unique identifier of the user
     * @param date The date to analyze (defaults to today)
     * @return Map of health insights and recommendations
     */
    suspend fun getNutritionalHealthInsights(userId: Long, date: Date = Date()): Map<String, Any> {
        val summary = getNutritionSummaryForDate(userId, date)
        val insights = mutableMapOf<String, Any>()

        // Macronutrient balance analysis
        val totalCalories = summary.totalCalories
        if (totalCalories > 0) {
            val proteinPercent = (summary.totalProtein * 4) / totalCalories * 100
            val carbPercent = (summary.totalCarbs * 4) / totalCalories * 100
            val fatPercent = (summary.totalFat * 9) / totalCalories * 100

            insights["macroBalance"] = mapOf(
                "protein" to proteinPercent,
                "carbs" to carbPercent,
                "fat" to fatPercent,
                "isBalanced" to (proteinPercent in 10.0..35.0 && carbPercent in 45.0..65.0 && fatPercent in 20.0..35.0),
            )
        }

        // Micronutrient status
        insights["micronutrients"] = mapOf(
            "vitaminC" to summary.totalVitaminC,
            "calcium" to summary.totalCalcium,
            "iron" to summary.totalIron,
            "sodium" to summary.totalSodium,
            "highSodium" to (summary.totalSodium > 2300), // Daily recommended limit
            "goodFiber" to (summary.totalFiber >= 25), // Daily recommended minimum for adults
        )

        // Overall nutritional quality score
        val qualityScore = calculateNutritionalQualityScore(summary)
        insights["overallQuality"] = mapOf(
            "score" to qualityScore,
            "rating" to when {
                qualityScore >= 8.0 -> "Excellent"
                qualityScore >= 6.0 -> "Good"
                qualityScore >= 4.0 -> "Fair"
                qualityScore >= 2.0 -> "Poor"
                else -> "Very Poor"
            },
        )

        return insights
    }

    /**
     * Gets nutrition entries that may have quality issues (high sodium, low fiber, etc.).
     *
     * @param userId The unique identifier of the user
     * @param maxSodium Maximum acceptable sodium per serving (mg)
     * @param minFiber Minimum acceptable fiber per serving (g)
     * @return Flow emitting list of nutrition entries that may need attention
     */
    fun getNutritionEntriesWithQualityIssues(
        userId: Long,
        maxSodium: Double = 600.0,
        minFiber: Double = 1.0,
    ): Flow<List<NutritionEntry>> {
        return nutritionEntryDao.getNutritionEntriesWithQualityIssues(userId, maxSodium, minFiber)
    }

    /**
     * Provides personalized dietary recommendations based on user's eating patterns.
     *
     * @param userId The unique identifier of the user
     * @param daysBack Number of days to analyze for patterns
     * @return List of personalized recommendations
     */
    suspend fun getPersonalizedRecommendations(userId: Long, daysBack: Int = 7): List<String> {
        val recommendations = mutableListOf<String>()
        val endDate = Date()
        val calendar = Calendar.getInstance()
        calendar.time = endDate
        calendar.add(Calendar.DAY_OF_YEAR, -daysBack)
        val startDate = calendar.time

        val summary = getNutritionSummaryForDateRange(userId, startDate, endDate)
        val avgDailyCalories = summary.totalCalories / daysBack

        // Calorie recommendations
        if (avgDailyCalories < 1200) {
            recommendations.add("Consider increasing your daily calorie intake for better energy levels.")
        } else if (avgDailyCalories > 3000) {
            recommendations.add("Consider reducing portion sizes or choosing lower-calorie alternatives.")
        }

        // Macronutrient recommendations
        val proteinRatio = (summary.totalProtein * 4) / summary.totalCalories
        if (proteinRatio < 0.10) {
            recommendations.add("Add more protein-rich foods like lean meats, beans, or Greek yogurt.")
        }

        // Micronutrient recommendations
        val avgDailySodium = summary.totalSodium / daysBack
        if (avgDailySodium > 2300) {
            recommendations.add("Reduce sodium intake by choosing fresh foods over processed ones.")
        }

        val avgDailyFiber = summary.totalFiber / daysBack
        if (avgDailyFiber < 25) {
            recommendations.add("Increase fiber intake with more fruits, vegetables, and whole grains.")
        }

        // Food variety recommendations
        val mostConsumed = getMostConsumedFoods(userId, 5)
        val topFoodFreq = mostConsumed.firstOrNull()?.frequency ?: 0
        if (topFoodFreq > daysBack * 0.5) {
            recommendations.add("Try diversifying your diet with new foods for better nutritional variety.")
        }

        return recommendations.ifEmpty { listOf("Great job maintaining a balanced diet! Keep up the good work.") }
    }

    // MARK: - Analytics and Reporting

    /**
     * Gets daily nutrition summaries for a date range.
     *
     * @param userId The unique identifier of the user
     * @param startDate The start date of the range
     * @param endDate The end date of the range
     * @return List of daily nutrition summaries for trend analysis
     */
    suspend fun getDailyNutritionSummaries(userId: Long, startDate: Date, endDate: Date): List<DailyNutritionSummary> {
        return nutritionEntryDao.getDailyNutritionSummaries(userId, startDate, endDate)
    }

    /**
     * Gets comprehensive nutrition statistics for a user.
     *
     * @param userId The unique identifier of the user
     * @return Map containing various nutrition statistics
     */
    suspend fun getNutritionStatistics(userId: Long): Map<String, Any> {
        val totalEntries = nutritionEntryDao.getTotalNutritionEntriesCount(userId)
        val avgCalories = getAverageDailyCalories(userId)
        val mostConsumed = getMostConsumedFoods(userId, 3)
        val weeklyStats = getWeeklyNutritionSummary(userId)

        return mapOf(
            "totalEntries" to totalEntries,
            "avgDailyCalories" to (avgCalories ?: 0.0),
            "topFoods" to mostConsumed,
            "weeklyCalories" to (weeklyStats.weeklyCalories ?: 0.0),
            "weeklyProtein" to (weeklyStats.weeklyProtein ?: 0.0),
            "weeklyCarbs" to (weeklyStats.weeklyCarbs ?: 0.0),
            "weeklyFat" to (weeklyStats.weeklyFat ?: 0.0),
        )
    }

    // MARK: - Data Management

    /**
     * Deletes old nutrition entries to free up storage space.
     *
     * @param userId The unique identifier of the user
     * @param olderThanDays Number of days to keep (entries older than this will be deleted)
     */
    suspend fun deleteOldNutritionEntries(userId: Long, olderThanDays: Int = 365) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -olderThanDays)
        val cutoffDate = calendar.time
        nutritionEntryDao.deleteOldNutritionEntries(userId, cutoffDate)
    }

    /**
     * Deletes all nutrition entries for a user (for account deletion).
     *
     * @param userId The unique identifier of the user
     */
    suspend fun deleteAllNutritionEntriesForUser(userId: Long) {
        nutritionEntryDao.deleteAllNutritionEntriesForUser(userId)
    }

    // MARK: - Private Helper Methods

    /**
     * Calculates a comprehensive nutritional quality score.
     *
     * @param summary The nutrition summary to analyze
     * @return Quality score from 0.0 to 10.0
     */
    private fun calculateNutritionalQualityScore(summary: NutritionSummary): Double {
        if (summary.totalCalories <= 0) return 0.0

        var score = 0.0

        // Macronutrient balance (up to 3 points)
        val proteinPercent = (summary.totalProtein * 4) / summary.totalCalories
        val carbPercent = (summary.totalCarbs * 4) / summary.totalCalories
        val fatPercent = (summary.totalFat * 9) / summary.totalCalories

        if (proteinPercent in 0.15..0.30) score += 1.0
        if (carbPercent in 0.45..0.60) score += 1.0
        if (fatPercent in 0.25..0.35) score += 1.0

        // Micronutrient density (up to 4 points)
        val fiberPer1000Cal = (summary.totalFiber / summary.totalCalories) * 1000
        val vitaminCPer1000Cal = (summary.totalVitaminC / summary.totalCalories) * 1000
        val calciumPer1000Cal = (summary.totalCalcium / summary.totalCalories) * 1000
        val ironPer1000Cal = (summary.totalIron / summary.totalCalories) * 1000

        if (fiberPer1000Cal >= 14) score += 1.0 // 14g per 1000 calories is good
        if (vitaminCPer1000Cal >= 45) score += 1.0 // 45mg per 1000 calories
        if (calciumPer1000Cal >= 500) score += 1.0 // 500mg per 1000 calories
        if (ironPer1000Cal >= 8) score += 1.0 // 8mg per 1000 calories

        // Negative factors (up to 3 points deducted)
        val sodiumPer1000Cal = (summary.totalSodium / summary.totalCalories) * 1000
        val saturatedFatPercent = (summary.totalSaturatedFat * 9) / summary.totalCalories
        val addedSugarPercent = (summary.totalAddedSugars * 4) / summary.totalCalories

        if (sodiumPer1000Cal > 1150) score -= 1.0 // High sodium
        if (saturatedFatPercent > 0.10) score -= 1.0 // High saturated fat
        if (addedSugarPercent > 0.10) score -= 1.0 // High added sugars

        // Base quality points
        score += 3.0

        return score.coerceIn(0.0, 10.0)
    }
}
