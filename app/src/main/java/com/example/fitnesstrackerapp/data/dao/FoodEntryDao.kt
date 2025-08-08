package com.example.fitnesstrackerapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.fitnesstrackerapp.data.entity.FoodEntry
import com.example.fitnesstrackerapp.data.entity.MealType
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Food Entry Data Access Object for the Fitness Tracker application.
 *
 * This DAO provides comprehensive database operations for FoodEntry entities including
 * nutrition tracking, meal categorization, daily intake analysis, and dietary statistics.
 * All operations are coroutine-based for optimal performance and UI responsiveness.
 *
 * Key Features:
 * - Food entry creation, updates, and deletion
 * - Daily nutrition tracking and analysis
 * - Meal type categorization and filtering
 * - Nutritional statistics and macro calculations
 * - Date range queries for dietary trends
 * - Calorie and nutrient intake monitoring
 */
@Dao
interface FoodEntryDao {

    /**
     * Inserts a new food entry into the database.
     *
     * @param foodEntry FoodEntry entity to insert
     * @return The ID of the inserted food entry
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodEntry(foodEntry: FoodEntry): Long

    /**
     * Alternative insert method for compatibility.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(foodEntry: FoodEntry): Long

    /**
     * Inserts multiple food entries into the database.
     *
     * @param foodEntries List of FoodEntry entities to insert
     * @return List of IDs of the inserted food entries
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(foodEntries: List<FoodEntry>): List<Long>

    /**
     * Updates an existing food entry in the database.
     *
     * @param foodEntry FoodEntry entity with updated data
     */
    @Update
    suspend fun updateFoodEntry(foodEntry: FoodEntry)

    /**
     * Alternative update method for compatibility.
     */
    @Update
    suspend fun update(foodEntry: FoodEntry)

    /**
     * Deletes a food entry from the database.
     *
     * @param foodEntry FoodEntry entity to delete
     */
    @Delete
    suspend fun deleteFoodEntry(foodEntry: FoodEntry)

    /**
     * Alternative delete method for compatibility.
     */
    @Delete
    suspend fun delete(foodEntry: FoodEntry)

    /**
     * Deletes a food entry by its ID.
     *
     * @param foodEntryId Food entry ID to delete
     */
    @Query("DELETE FROM food_entries WHERE id = :foodEntryId")
    suspend fun deleteFoodEntryById(foodEntryId: Long)

    /**
     * Gets all food entries for a specific user ordered by consumption date.
     *
     * @param userId User ID
     * @return Flow of list of food entries
     */
    @Query("SELECT * FROM food_entries WHERE user_id = :userId ORDER BY date_consumed DESC")
    fun getFoodEntriesByUserId(userId: Long): Flow<List<FoodEntry>>

    /**
     * Gets food entries for a specific date.
     *
     * @param userId User ID
     * @param date Date to get entries for
     * @return Flow of list of food entries
     */
    @Query("SELECT * FROM food_entries WHERE user_id = :userId AND DATE(date_consumed) = DATE(:date) ORDER BY date_consumed DESC")
    fun getFoodEntriesForDate(userId: Long, date: Date): Flow<List<FoodEntry>>

    /**
     * Gets food entries for a date range.
     *
     * @param userId User ID
     * @param startDate Start date
     * @param endDate End date
     * @return Flow of list of food entries
     */
    @Query("SELECT * FROM food_entries WHERE user_id = :userId AND date_consumed BETWEEN :startDate AND :endDate ORDER BY date_consumed DESC")
    fun getFoodEntriesForDateRange(userId: Long, startDate: Date, endDate: Date): Flow<List<FoodEntry>>

    /**
     * Gets food entries by meal type for a specific date.
     *
     * @param userId User ID
     * @param date Date
     * @param mealType Meal type
     * @return Flow of list of food entries
     */
    @Query("SELECT * FROM food_entries WHERE user_id = :userId AND DATE(date_consumed) = DATE(:date) AND meal_type = :mealType ORDER BY date_consumed DESC")
    fun getFoodEntriesByMealType(userId: Long, date: Date, mealType: MealType): Flow<List<FoodEntry>>

    /**
     * Gets recent food entries with limit.
     *
     * @param userId User ID
     * @param limit Maximum number of entries to return
     * @return Flow of recent food entries
     */
    @Query("SELECT * FROM food_entries WHERE user_id = :userId ORDER BY date_consumed DESC LIMIT :limit")
    fun getRecentFoodEntries(userId: Long, limit: Int): Flow<List<FoodEntry>>

    /**
     * Gets total calories consumed for a specific date.
     *
     * @param userId User ID
     * @param date Date
     * @return Total calories for the date
     */
    @Query("SELECT SUM(calories_per_serving * serving_size) FROM food_entries WHERE user_id = :userId AND DATE(date_consumed) = DATE(:date)")
    suspend fun getTotalCaloriesForDate(userId: Long, date: Date): Double?

    /**
     * Gets total macronutrients for a specific date.
     *
     * @param userId User ID
     * @param date Date
     * @return Triple of (total protein, total carbs, total fat) in grams
     */
    @Query("""
        SELECT 
        SUM(protein_grams * serving_size), 
        SUM(carbs_grams * serving_size), 
        SUM(fat_grams * serving_size) 
        FROM food_entries 
        WHERE user_id = :userId AND DATE(date_consumed) = DATE(:date)
    """)
    suspend fun getTotalMacrosForDate(userId: Long, date: Date): Triple<Double?, Double?, Double?>

    /**
     * Gets nutrition summary for a date range.
     *
     * @param userId User ID
     * @param startDate Start date
     * @param endDate End date
     * @return Map with nutrition totals
     */
    @Query("""
        SELECT 
        SUM(calories_per_serving * serving_size) as total_calories,
        SUM(protein_grams * serving_size) as total_protein,
        SUM(carbs_grams * serving_size) as total_carbs,
        SUM(fat_grams * serving_size) as total_fat,
        SUM(fiber_grams * serving_size) as total_fiber,
        SUM(sugar_grams * serving_size) as total_sugar,
        SUM(sodium_mg * serving_size) as total_sodium
        FROM food_entries 
        WHERE user_id = :userId AND date_consumed BETWEEN :startDate AND :endDate
    """)
    suspend fun getNutritionSummaryForDateRange(userId: Long, startDate: Date, endDate: Date): Map<String, Double?>

    /**
     * Gets average daily calories for a user.
     *
     * @param userId User ID
     * @return Average daily calorie intake
     */
    @Query("""
        SELECT AVG(daily_calories) FROM (
            SELECT DATE(date_consumed) as date, SUM(calories_per_serving * serving_size) as daily_calories
            FROM food_entries 
            WHERE user_id = :userId 
            GROUP BY DATE(date_consumed)
        )
    """)
    suspend fun getAverageDailyCalories(userId: Long): Double?

    /**
     * Gets meal distribution statistics.
     *
     * @param userId User ID
     * @param date Date
     * @return List of meal type calorie distributions
     */
    @Query("""
        SELECT meal_type, SUM(calories_per_serving * serving_size) as calories
        FROM food_entries 
        WHERE user_id = :userId AND DATE(date_consumed) = DATE(:date)
        GROUP BY meal_type
    """)
    suspend fun getMealDistributionForDate(userId: Long, date: Date): List<Map<String, Any>>

    /**
     * Gets most consumed foods.
     *
     * @param userId User ID
     * @param limit Number of top foods to return
     * @return List of most frequently consumed foods
     */
    @Query("""
        SELECT food_name, COUNT(*) as frequency, AVG(calories_per_serving * serving_size) as avg_calories
        FROM food_entries 
        WHERE user_id = :userId 
        GROUP BY food_name 
        ORDER BY frequency DESC 
        LIMIT :limit
    """)
    suspend fun getMostConsumedFoods(userId: Long, limit: Int): List<Map<String, Any>>

    /**
     * Gets total food entries count for a user.
     *
     * @param userId User ID
     * @return Total number of food entries
     */
    @Query("SELECT COUNT(*) FROM food_entries WHERE user_id = :userId")
    suspend fun getTotalFoodEntriesCount(userId: Long): Int

    /**
     * Gets food entries count for a specific date.
     *
     * @param userId User ID
     * @param date Date
     * @return Number of food entries for the date
     */
    @Query("SELECT COUNT(*) FROM food_entries WHERE user_id = :userId AND DATE(date_consumed) = DATE(:date)")
    suspend fun getFoodEntriesCountForDate(userId: Long, date: Date): Int

    /**
     * Gets weekly nutrition summary.
     *
     * @param userId User ID
     * @param weekStart Start of the week
     * @param weekEnd End of the week
     * @return Weekly nutrition totals
     */
    @Query("""
        SELECT 
        SUM(calories_per_serving * serving_size) as weekly_calories,
        AVG(calories_per_serving * serving_size) as avg_daily_calories,
        SUM(protein_grams * serving_size) as weekly_protein,
        SUM(carbs_grams * serving_size) as weekly_carbs,
        SUM(fat_grams * serving_size) as weekly_fat
        FROM food_entries 
        WHERE user_id = :userId AND date_consumed BETWEEN :weekStart AND :weekEnd
    """)
    suspend fun getWeeklyNutritionSummary(userId: Long, weekStart: Date, weekEnd: Date): Map<String, Double?>

    /**
     * Searches food entries by food name.
     *
     * @param userId User ID
     * @param searchQuery Search query for food name
     * @return Flow of matching food entries
     */
    @Query("SELECT * FROM food_entries WHERE user_id = :userId AND food_name LIKE '%' || :searchQuery || '%' ORDER BY date_consumed DESC")
    fun searchFoodEntries(userId: Long, searchQuery: String): Flow<List<FoodEntry>>

    /**
     * Deletes food entries older than specified date.
     *
     * @param userId User ID
     * @param olderThan Cutoff date
     */
    @Query("DELETE FROM food_entries WHERE user_id = :userId AND date_consumed < :olderThan")
    suspend fun deleteOldFoodEntries(userId: Long, olderThan: Date)

    /**
     * Deletes all food entries for a user.
     *
     * @param userId User ID
     */
    @Query("DELETE FROM food_entries WHERE user_id = :userId")
    suspend fun deleteAllFoodEntriesForUser(userId: Long)

    /**
     * Deletes all food entries (for testing purposes only).
     */
    @Query("DELETE FROM food_entries")
    suspend fun deleteAllFoodEntries()
}
