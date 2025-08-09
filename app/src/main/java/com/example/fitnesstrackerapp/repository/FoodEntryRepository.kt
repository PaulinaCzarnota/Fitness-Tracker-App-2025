/**
 * FoodEntry Repository
 *
 * Repository for managing food entry and nutrition tracking data in the Fitness Tracker App.
 *
 * This class serves as the single source of truth for food-related data operations,
 * providing a clean API for nutrition tracking, meal categorization, and dietary analytics.
 * It abstracts the data sources from the rest of the app with proper validation.
 *
 * Key Features:
 * - Comprehensive CRUD operations for food entries
 * - Daily and date-range nutrition summaries
 * - Meal type categorization and filtering
 * - Nutritional statistics and macro calculations
 * - Food consumption analytics and trends
 * - Data validation and business logic enforcement
 *
 * @property foodEntryDao The Data Access Object for food entry database operations.
 */
package com.example.fitnesstrackerapp.repository

import com.example.fitnesstrackerapp.data.dao.FoodEntryDao
import com.example.fitnesstrackerapp.data.entity.FoodEntry
import com.example.fitnesstrackerapp.data.entity.MealType
import com.example.fitnesstrackerapp.data.model.FoodConsumptionStats
import com.example.fitnesstrackerapp.data.model.MacroTotals
import com.example.fitnesstrackerapp.data.model.MealDistribution
import com.example.fitnesstrackerapp.data.model.NutritionSummary
import com.example.fitnesstrackerapp.data.model.WeeklyNutritionSummary
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Repository for managing food entry and nutrition tracking operations.
 *
 * Provides a comprehensive API for food logging, nutrition analysis,
 * and dietary tracking with proper validation and error handling.
 */
class FoodEntryRepository(private val foodEntryDao: FoodEntryDao) {

    // region CRUD Operations

    /**
     * Inserts a new food entry into the database.
     *
     * @param foodEntry The food entry to insert
     * @return The row ID of the newly inserted food entry
     * @throws IllegalArgumentException If the food entry data is invalid
     */
    suspend fun insertFoodEntry(foodEntry: FoodEntry): Long {
        require(foodEntry.userId > 0) { "Food entry must have a valid user ID" }
        require(foodEntry.foodName.isNotBlank()) { "Food name cannot be blank" }
        require(foodEntry.servingSize > 0) { "Serving size must be positive" }
        require(foodEntry.caloriesPerServing >= 0) { "Calories per serving cannot be negative" }
        require(foodEntry.isValid()) { "Food entry data is invalid" }

        return foodEntryDao.insertFoodEntry(foodEntry)
    }

    /**
     * Updates an existing food entry in the database.
     *
     * @param foodEntry The food entry with updated values
     * @throws IllegalArgumentException If the food entry ID is invalid
     */
    suspend fun updateFoodEntry(foodEntry: FoodEntry) {
        require(foodEntry.id > 0) { "Food entry must have a valid ID" }
        require(foodEntry.isValid()) { "Food entry data is invalid" }

        foodEntryDao.updateFoodEntry(foodEntry)
    }

    /**
     * Deletes a food entry from the database.
     *
     * @param foodEntry The food entry to delete
     * @throws IllegalArgumentException If the food entry ID is invalid
     */
    suspend fun deleteFoodEntry(foodEntry: FoodEntry) {
        require(foodEntry.id > 0) { "Cannot delete food entry with invalid ID" }
        foodEntryDao.deleteFoodEntry(foodEntry)
    }

    /**
     * Deletes a food entry by its ID.
     *
     * @param foodEntryId The ID of the food entry to delete
     * @throws IllegalArgumentException If the ID is invalid
     */
    suspend fun deleteFoodEntryById(foodEntryId: Long) {
        require(foodEntryId > 0) { "Food entry ID must be positive" }
        foodEntryDao.deleteFoodEntryById(foodEntryId)
    }

    // endregion

    // region Bulk Operations

    /**
     * Inserts multiple food entries into the database.
     *
     * @param foodEntries The list of food entries to insert
     * @return List of row IDs for the inserted food entries
     * @throws IllegalArgumentException If any food entry is invalid
     */
    suspend fun insertAllFoodEntries(foodEntries: List<FoodEntry>): List<Long> {
        require(foodEntries.isNotEmpty()) { "Food entries list cannot be empty" }

        foodEntries.forEachIndexed { index, foodEntry ->
            require(foodEntry.isValid()) { "Food entry at index $index is invalid" }
        }

        return foodEntryDao.insertAll(foodEntries)
    }

    /**
     * Deletes all food entries for a specific user.
     *
     * @param userId The ID of the user whose food entries should be deleted
     * @throws IllegalArgumentException If the user ID is invalid
     */
    suspend fun deleteAllFoodEntriesForUser(userId: Long) {
        require(userId > 0) { "User ID must be positive" }
        foodEntryDao.deleteAllFoodEntriesForUser(userId)
    }

    // endregion

    // region Query Operations

    /**
     * Gets all food entries for a specific user.
     *
     * @param userId The ID of the user
     * @return Flow of list of food entries ordered by consumption date
     */
    fun getFoodEntriesByUserId(userId: Long): Flow<List<FoodEntry>> {
        require(userId > 0) { "User ID must be positive" }
        return foodEntryDao.getFoodEntriesByUserId(userId)
    }

    /**
     * Gets food entries for a specific date.
     *
     * @param userId The ID of the user
     * @param date The date to get entries for
     * @return Flow of list of food entries for the date
     */
    fun getFoodEntriesForDate(userId: Long, date: Date): Flow<List<FoodEntry>> {
        require(userId > 0) { "User ID must be positive" }
        return foodEntryDao.getFoodEntriesForDate(userId, date)
    }

    /**
     * Gets food entries within a date range.
     *
     * @param userId The ID of the user
     * @param startDate The start date of the range
     * @param endDate The end date of the range
     * @return Flow of list of food entries in the date range
     */
    fun getFoodEntriesForDateRange(userId: Long, startDate: Date, endDate: Date): Flow<List<FoodEntry>> {
        require(userId > 0) { "User ID must be positive" }
        require(!startDate.after(endDate)) { "Start date cannot be after end date" }

        return foodEntryDao.getFoodEntriesForDateRange(userId, startDate, endDate)
    }

    /**
     * Gets food entries by meal type for a specific date.
     *
     * @param userId The ID of the user
     * @param date The date
     * @param mealType The meal type to filter by
     * @return Flow of list of food entries for the meal type
     */
    fun getFoodEntriesByMealType(userId: Long, date: Date, mealType: MealType): Flow<List<FoodEntry>> {
        require(userId > 0) { "User ID must be positive" }
        return foodEntryDao.getFoodEntriesByMealType(userId, mealType, date)
    }

    /**
     * Gets recent food entries with limit.
     *
     * @param userId The ID of the user
     * @param limit Maximum number of entries to return
     * @return Flow of recent food entries
     */
    fun getRecentFoodEntries(userId: Long, limit: Int = 20): Flow<List<FoodEntry>> {
        require(userId > 0) { "User ID must be positive" }
        require(limit > 0) { "Limit must be positive" }

        return foodEntryDao.getRecentFoodEntries(userId, limit)
    }

    /**
     * Searches food entries by food name.
     *
     * @param userId The ID of the user
     * @param searchQuery The search query for food name
     * @return Flow of matching food entries
     */
    fun searchFoodEntries(userId: Long, searchQuery: String): Flow<List<FoodEntry>> {
        require(userId > 0) { "User ID must be positive" }
        require(searchQuery.isNotBlank()) { "Search query cannot be blank" }

        return foodEntryDao.searchFoodEntries(userId, searchQuery.trim())
    }

    // endregion

    // region Nutrition Analysis

    /**
     * Gets total calories consumed for a specific date.
     *
     * @param userId The ID of the user
     * @param date The date to get calories for
     * @return Total calories for the date, or 0.0 if no entries
     */
    suspend fun getTotalCaloriesForDate(userId: Long, date: Date): Double {
        require(userId > 0) { "User ID must be positive" }
        return foodEntryDao.getTotalCaloriesForDate(userId, date) ?: 0.0
    }

    /**
     * Gets total macronutrients for a specific date.
     *
     * @param userId The ID of the user
     * @param date The date to get macros for
     * @return MacroTotals with protein, carbs, and fat totals
     */
    suspend fun getTotalMacrosForDate(userId: Long, date: Date): MacroTotals {
        require(userId > 0) { "User ID must be positive" }
        return foodEntryDao.getTotalMacrosForDate(userId, date) ?: MacroTotals(0.0, 0.0, 0.0)
    }

    /**
     * Gets comprehensive nutrition summary for a date range.
     *
     * @param userId The ID of the user
     * @param startDate The start date of the range
     * @param endDate The end date of the range
     * @return NutritionSummary with all nutrition totals
     */
    suspend fun getNutritionSummaryForDateRange(userId: Long, startDate: Date, endDate: Date): NutritionSummary {
        require(userId > 0) { "User ID must be positive" }
        require(!startDate.after(endDate)) { "Start date cannot be after end date" }

        return foodEntryDao.getNutritionSummaryForDateRange(userId, startDate, endDate)
            ?: NutritionSummary(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
    }

    /**
     * Gets weekly nutrition summary.
     *
     * @param userId The ID of the user
     * @param weekStart The start of the week
     * @param weekEnd The end of the week
     * @return WeeklyNutritionSummary with weekly totals and averages
     */
    suspend fun getWeeklyNutritionSummary(userId: Long, weekStart: Date, weekEnd: Date): WeeklyNutritionSummary {
        require(userId > 0) { "User ID must be positive" }
        require(!weekStart.after(weekEnd)) { "Week start cannot be after week end" }

        return foodEntryDao.getWeeklyNutritionSummary(userId, weekStart, weekEnd)
            ?: WeeklyNutritionSummary(0.0, 0.0, 0.0, 0.0, 0.0)
    }

    /**
     * Gets average daily calories for a user.
     *
     * @param userId The ID of the user
     * @return Average daily calorie intake
     */
    suspend fun getAverageDailyCalories(userId: Long): Double {
        require(userId > 0) { "User ID must be positive" }
        return foodEntryDao.getAverageDailyCalories(userId) ?: 0.0
    }

    /**
     * Gets meal distribution statistics for a specific date.
     *
     * @param userId The ID of the user
     * @param date The date to analyze
     * @return List of meal type calorie distributions
     */
    suspend fun getMealDistributionForDate(userId: Long, date: Date): List<MealDistribution> {
        require(userId > 0) { "User ID must be positive" }
        return foodEntryDao.getMealDistributionForDate(userId, date)
    }

    /**
     * Gets most consumed foods.
     *
     * @param userId The ID of the user
     * @param limit Number of top foods to return
     * @return List of most frequently consumed foods
     */
    suspend fun getMostConsumedFoods(userId: Long, limit: Int = 10): List<FoodConsumptionStats> {
        require(userId > 0) { "User ID must be positive" }
        require(limit > 0) { "Limit must be positive" }

        return foodEntryDao.getMostConsumedFoods(userId, limit)
    }

    // endregion

    // region Statistics

    /**
     * Gets total food entries count for a user.
     *
     * @param userId The ID of the user
     * @return Total number of food entries
     */
    suspend fun getTotalFoodEntriesCount(userId: Long): Int {
        require(userId > 0) { "User ID must be positive" }
        return foodEntryDao.getTotalFoodEntriesCount(userId)
    }

    /**
     * Gets food entries count for a specific date.
     *
     * @param userId The ID of the user
     * @param date The date to count entries for
     * @return Number of food entries for the date
     */
    suspend fun getFoodEntriesCountForDate(userId: Long, date: Date): Int {
        require(userId > 0) { "User ID must be positive" }
        return foodEntryDao.getFoodEntriesCountForDate(userId, date)
    }

    // endregion

    // region Data Maintenance

    /**
     * Deletes food entries older than specified date.
     *
     * @param userId The ID of the user
     * @param olderThan The cutoff date for deletion
     * @throws IllegalArgumentException If user ID is invalid
     */
    suspend fun deleteOldFoodEntries(userId: Long, olderThan: Date) {
        require(userId > 0) { "User ID must be positive" }
        foodEntryDao.deleteOldFoodEntries(userId, olderThan)
    }

    /**
     * Validates food entry data before operations.
     *
     * @param foodEntry The food entry to validate
     * @return true if valid, false otherwise
     */
    fun validateFoodEntry(foodEntry: FoodEntry): Boolean {
        return foodEntry.isValid() &&
            foodEntry.userId > 0 &&
            foodEntry.foodName.isNotBlank() &&
            foodEntry.servingSize > 0 &&
            foodEntry.caloriesPerServing >= 0
    }

    // endregion
}
