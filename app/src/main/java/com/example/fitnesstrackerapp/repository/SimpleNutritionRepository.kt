/**
 * Simple Nutrition Repository for managing food entries and nutrition tracking.
 *
 * This repository provides basic nutrition tracking functionality using only
 * the FoodEntry DAO and standard Android SDK components. It handles:
 * - Food entry logging and management
 * - Daily nutrition summary calculations
 * - Basic nutrition goal tracking
 *
 * This implementation focuses on core functionality without external API dependencies,
 * making it suitable for offline-first nutrition tracking.
 */

package com.example.fitnesstrackerapp.repository

import android.util.Log
import com.example.fitnesstrackerapp.data.dao.FoodEntryDao
import com.example.fitnesstrackerapp.data.entity.FoodEntry
import com.example.fitnesstrackerapp.data.entity.MealType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Date

/**
 * Simple repository implementation for nutrition tracking functionality.
 *
 * Provides basic food entry management and nutrition calculations using
 * only local database storage through Room DAO operations.
 *
 * @param foodEntryDao DAO for food entry database operations
 */
class SimpleNutritionRepository(
    private val foodEntryDao: FoodEntryDao,
) {
    companion object {
        private const val TAG = "SimpleNutritionRepository"
    }

    /**
     * Gets all food entries for a specific user.
     *
     * @param userId The ID of the user
     * @return Flow of food entries list
     */
    fun getAllFoodEntriesForUser(userId: Long): Flow<List<FoodEntry>> {
        return try {
            foodEntryDao.getFoodEntriesByUserId(userId)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching food entries for user: $userId", e)
            flow { emit(emptyList()) }
        }
    }

    /**
     * Gets food entries for a specific user and date.
     *
     * @param userId The ID of the user
     * @param date The date to get entries for
     * @return Flow of food entries list
     */
    fun getFoodEntriesForDate(userId: Long, date: Date): Flow<List<FoodEntry>> {
        return try {
            foodEntryDao.getFoodEntriesForDate(userId, date)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching food entries for date: $date", e)
            flow { emit(emptyList()) }
        }
    }

    /**
     * Gets food entries for a specific user, date, and meal type.
     *
     * @param userId The ID of the user
     * @param date The date to get entries for
     * @param mealType The meal type to filter by
     * @return Flow of food entries list
     */
    fun getFoodEntriesByMealType(userId: Long, date: Date, mealType: MealType): Flow<List<FoodEntry>> {
        return try {
            foodEntryDao.getFoodEntriesByMealType(userId, mealType, date)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching food entries for meal type: $mealType", e)
            flow { emit(emptyList()) }
        }
    }

    /**
     * Adds a new food entry to the database.
     *
     * @param foodEntry The food entry to add
     * @return The ID of the inserted food entry
     */
    suspend fun addFoodEntry(foodEntry: FoodEntry): Long {
        return try {
            val id = foodEntryDao.insertFoodEntry(foodEntry)
            Log.d(TAG, "Food entry added successfully with ID: $id")
            id
        } catch (e: Exception) {
            Log.e(TAG, "Error adding food entry", e)
            throw e
        }
    }

    /**
     * Updates an existing food entry in the database.
     *
     * @param foodEntry The food entry to update
     */
    suspend fun updateFoodEntry(foodEntry: FoodEntry) {
        try {
            foodEntryDao.updateFoodEntry(foodEntry)
            Log.d(TAG, "Food entry updated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating food entry with ID: ${foodEntry.id}", e)
            throw e
        }
    }

    /**
     * Deletes a food entry from the database.
     *
     * @param foodEntry The food entry to delete
     */
    suspend fun deleteFoodEntry(foodEntry: FoodEntry) {
        try {
            foodEntryDao.deleteFoodEntry(foodEntry)
            Log.d(TAG, "Food entry deleted successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting food entry with ID: ${foodEntry.id}", e)
            throw e
        }
    }

    /**
     * Gets recent food entries for a user.
     *
     * @param userId The ID of the user
     * @param limit Maximum number of entries to return
     * @return Flow of recent food entries
     */
    fun getRecentFoodEntries(userId: Long, limit: Int = 10): Flow<List<FoodEntry>> {
        return try {
            foodEntryDao.getRecentFoodEntries(userId, limit)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching recent food entries for user: $userId", e)
            flow { emit(emptyList()) }
        }
    }

    /**
     * Calculates daily nutrition summary for a user and date.
     *
     * This method aggregates all food entries for a specific date and calculates
     * total nutrition values including calories, protein, carbs, and fats.
     *
     * @param userId The ID of the user
     * @param date The date to calculate summary for
     * @return Flow of nutrition summary data
     */
    fun getDailyNutritionSummary(userId: Long, date: Date): Flow<NutritionSummary> {
        return flow {
            try {
                val entries = foodEntryDao.getFoodEntriesForDate(userId, date)
                entries.collect { foodEntries ->
                    val summary = calculateNutritionSummary(foodEntries)
                    emit(summary)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error calculating daily nutrition summary", e)
                emit(NutritionSummary.empty())
            }
        }
    }

    /**
     * Gets nutrition summary for a date range.
     *
     * @param userId The ID of the user
     * @param startDate Start date of the range
     * @param endDate End date of the range
     * @return Flow of nutrition summary data
     */
    fun getNutritionSummaryForRange(userId: Long, startDate: Date, endDate: Date): Flow<NutritionSummary> {
        return flow {
            try {
                val entries = foodEntryDao.getFoodEntriesForDateRange(userId, startDate, endDate)
                entries.collect { foodEntries ->
                    val summary = calculateNutritionSummary(foodEntries)
                    emit(summary)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error calculating nutrition summary for range", e)
                emit(NutritionSummary.empty())
            }
        }
    }

    /**
     * Private helper method to calculate nutrition summary from food entries.
     *
     * @param foodEntries List of food entries to calculate from
     * @return Calculated nutrition summary
     */
    private fun calculateNutritionSummary(foodEntries: List<FoodEntry>): NutritionSummary {
        return if (foodEntries.isEmpty()) {
            NutritionSummary.empty()
        } else {
            val totalCalories = foodEntries.sumOf { it.getTotalCalories() }
            val totalProtein = foodEntries.sumOf { it.getTotalProtein() }
            val totalCarbs = foodEntries.sumOf { it.getTotalCarbs() }
            val totalFat = foodEntries.sumOf { it.getTotalFat() }
            val totalFiber = foodEntries.sumOf { it.getTotalFiber() }

            NutritionSummary(
                totalCalories = totalCalories,
                totalProtein = totalProtein,
                totalCarbohydrates = totalCarbs,
                totalFat = totalFat,
                totalFiber = totalFiber,
                entryCount = foodEntries.size,
                lastUpdated = Date(),
            )
        }
    }

    /**
     * Deletes all food entries for a specific user.
     * WARNING: This operation is irreversible.
     *
     * @param userId The ID of the user
     */
    suspend fun deleteAllFoodEntriesForUser(userId: Long) {
        try {
            foodEntryDao.deleteAllFoodEntriesForUser(userId)
            Log.d(TAG, "Deleted food entries for user: $userId")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting all food entries for user: $userId", e)
            throw e
        }
    }
}

/**
 * Data class representing a nutrition summary for a given time period.
 *
 * Contains aggregated nutrition information calculated from multiple food entries.
 *
 * @param totalCalories Total calories consumed
 * @param totalProtein Total protein in grams
 * @param totalCarbohydrates Total carbohydrates in grams
 * @param totalFat Total fat in grams
 * @param totalFiber Total fiber in grams
 * @param entryCount Number of food entries included in calculation
 * @param lastUpdated Timestamp when summary was last calculated
 */
data class NutritionSummary(
    val totalCalories: Double = 0.0,
    val totalProtein: Double = 0.0,
    val totalCarbohydrates: Double = 0.0,
    val totalFat: Double = 0.0,
    val totalFiber: Double = 0.0,
    val entryCount: Int = 0,
    val lastUpdated: Date = Date(),
) {
    companion object {
        /**
         * Creates an empty nutrition summary with all values set to zero.
         *
         * @return Empty nutrition summary instance
         */
        fun empty(): NutritionSummary = NutritionSummary()
    }

    /**
     * Calculates the percentage of calories from each macronutrient.
     *
     * @return Triple containing (protein%, carbs%, fat%) percentages
     */
    fun getMacroPercentages(): Triple<Double, Double, Double> {
        return if (totalCalories > 0) {
            val proteinCalories = totalProtein * 4 // 4 calories per gram of protein
            val carbCalories = totalCarbohydrates * 4 // 4 calories per gram of carbs
            val fatCalories = totalFat * 9 // 9 calories per gram of fat

            val proteinPercentage = (proteinCalories / totalCalories) * 100
            val carbPercentage = (carbCalories / totalCalories) * 100
            val fatPercentage = (fatCalories / totalCalories) * 100

            Triple(proteinPercentage, carbPercentage, fatPercentage)
        } else {
            Triple(0.0, 0.0, 0.0)
        }
    }

    /**
     * Checks if the nutrition summary meets basic recommended guidelines.
     *
     * @return Boolean indicating if the summary meets basic guidelines
     */
    fun meetsBasicGuidelines(): Boolean {
        val (proteinPercent, carbPercent, fatPercent) = getMacroPercentages()
        return proteinPercent >= 10 && // At least 10% protein
            carbPercent >= 45 && carbPercent <= 65 && // 45-65% carbohydrates
            fatPercent >= 20 && fatPercent <= 35 // 20-35% fat
    }
}
