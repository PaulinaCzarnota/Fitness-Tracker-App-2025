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

/**
 * Data Access Object for FoodEntry entity operations.
 *
 * Responsibilities:
 * - Insert, update, delete food entries
 * - Query food entries by user, date, and meal type
 * - Calculate nutritional totals and analytics
 * - Handle daily nutrition tracking data
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
    suspend fun update(foodEntry: FoodEntry)

    /**
     * Deletes a food entry from the database.
     *
     * @param foodEntry FoodEntry entity to delete
     */
    @Delete
    suspend fun delete(foodEntry: FoodEntry)

    /**
     * Deletes a food entry by its ID.
     *
     * @param foodEntryId Food entry ID to delete
     */
    @Query("DELETE FROM food_entries WHERE id = :foodEntryId")
    suspend fun deleteById(foodEntryId: Long)

    /**
     * Gets all food entries for a specific user ordered by consumption date.
     *
     * @param userId User ID
     * @return Flow of list of food entries
     */
    @Query("SELECT * FROM food_entries WHERE user_id = :userId ORDER BY date_consumed DESC")
    fun getFoodEntriesByUser(userId: Long): Flow<List<FoodEntry>>

    /**
     * Gets food entries for a specific date.
     *
     * @param userId User ID
     * @param date Date to get entries for
     * @return Flow of list of food entries
     */
    @Query("SELECT * FROM food_entries WHERE user_id = :userId AND date(date_consumed/1000, 'unixepoch') = date(:date/1000, 'unixepoch') ORDER BY date_consumed DESC")
    fun getFoodEntriesByDate(userId: Long, date: Long): Flow<List<FoodEntry>>

    /**
     * Gets food entries for a specific date.
     */
    @Query("SELECT * FROM food_entries WHERE user_id = :userId AND DATE(logged_at) = DATE(:date) ORDER BY logged_at DESC")
    fun getFoodEntriesForDate(userId: Long, date: Long): Flow<List<FoodEntry>>

    /**
     * Gets food entries for a date range.
     */
    @Query("SELECT * FROM food_entries WHERE user_id = :userId AND logged_at BETWEEN :startDate AND :endDate ORDER BY logged_at DESC")
    fun getFoodEntriesForDateRange(userId: Long, startDate: Long, endDate: Long): Flow<List<FoodEntry>>

    /**
     * Gets total calories consumed for a specific date.
     *
     * @param userId User ID
     * @param date Date to calculate calories for
     * @return Flow of total calories
     */
    @Query("SELECT COALESCE(SUM(calories_per_serving * serving_size), 0.0) FROM food_entries WHERE user_id = :userId AND date(date_consumed/1000, 'unixepoch') = date(:date/1000, 'unixepoch')")
    fun getTotalCaloriesForDate(userId: Long, date: Long): Flow<Double>

    /**
     * Gets food entries by meal type.
     *
     * @param userId User ID
     * @param mealType Type of meal
     * @return Flow of list of food entries
     */
    @Query("SELECT * FROM food_entries WHERE user_id = :userId AND meal_type = :mealType ORDER BY date_consumed DESC")
    fun getFoodEntriesByMealType(userId: Long, mealType: MealType): Flow<List<FoodEntry>>

    /**
     * Gets food entries within a date range.
     *
     * @param userId User ID
     * @param startDate Start date (timestamp)
     * @param endDate End date (timestamp)
     * @return Flow of list of food entries
     */
    @Query("SELECT * FROM food_entries WHERE user_id = :userId AND date_consumed BETWEEN :startDate AND :endDate ORDER BY date_consumed DESC")
    fun getFoodEntriesInDateRange(userId: Long, startDate: Long, endDate: Long): Flow<List<FoodEntry>>

    /**
     * Deletes all food entries for a specific user.
     *
     * @param userId User ID
     */
    @Query("DELETE FROM food_entries WHERE user_id = :userId")
    suspend fun deleteAllUserEntries(userId: Long)

    /**
     * Gets food entries for a specific date and meal type.
     *
     * @param userId User ID
     * @param date Date to get entries for
     * @param mealType Type of meal
     * @return Flow of list of food entries
     */
    @Query("SELECT * FROM food_entries WHERE user_id = :userId AND date(date_consumed/1000, 'unixepoch') = date(:date/1000, 'unixepoch') AND meal_type = :mealType ORDER BY date_consumed DESC")
    fun getFoodEntriesByDateAndMeal(userId: Long, date: Long, mealType: MealType): Flow<List<FoodEntry>>

    /**
     * Gets total protein consumed for a specific date.
     *
     * @param userId User ID
     * @param date Date to calculate protein for
     * @return Total protein in grams
     */
    @Query("SELECT COALESCE(SUM(protein_grams * serving_size), 0.0) FROM food_entries WHERE user_id = :userId AND date(date_consumed/1000, 'unixepoch') = date(:date/1000, 'unixepoch')")
    suspend fun getTotalProteinForDate(userId: Long, date: Long): Double

    /**
     * Gets total carbohydrates consumed for a specific date.
     *
     * @param userId User ID
     * @param date Date to calculate carbs for
     * @return Total carbs in grams
     */
    @Query("SELECT COALESCE(SUM(carbs_grams * serving_size), 0.0) FROM food_entries WHERE user_id = :userId AND date(date_consumed/1000, 'unixepoch') = date(:date/1000, 'unixepoch')")
    suspend fun getTotalCarbsForDate(userId: Long, date: Long): Double

    /**
     * Gets total fat consumed for a specific date.
     *
     * @param userId User ID
     * @param date Date to calculate fat for
     * @return Total fat in grams
     */
    @Query("SELECT COALESCE(SUM(fat_grams * serving_size), 0.0) FROM food_entries WHERE user_id = :userId AND date(date_consumed/1000, 'unixepoch') = date(:date/1000, 'unixepoch')")
    suspend fun getTotalFatForDate(userId: Long, date: Long): Double

    /**
     * Gets total fiber consumed for a specific date.
     *
     * @param userId User ID
     * @param date Date to calculate fiber for
     * @return Total fiber in grams
     */
    @Query("SELECT COALESCE(SUM(fiber_grams * serving_size), 0.0) FROM food_entries WHERE user_id = :userId AND date(date_consumed/1000, 'unixepoch') = date(:date/1000, 'unixepoch')")
    suspend fun getTotalFiberForDate(userId: Long, date: Long): Double

    /**
     * Gets recent food entries with limit.
     *
     * @param userId User ID
     * @param limit Maximum number of entries to return
     * @return Flow of list of recent food entries
     */
    @Query("SELECT * FROM food_entries WHERE user_id = :userId ORDER BY date_consumed DESC LIMIT :limit")
    fun getRecentFoodEntries(userId: Long, limit: Int): Flow<List<FoodEntry>>

    /**
     * Gets total calories consumed today.
     *
     * @param userId User ID
     * @return Flow of total calories for today
     */
    @Query("SELECT COALESCE(SUM(calories_per_serving * serving_size), 0.0) FROM food_entries WHERE user_id = :userId AND date(date_consumed/1000, 'unixepoch') = date('now')")
    fun getTodayCalories(userId: Long): Flow<Double>

    /**
     * Gets food entries count for a specific date.
     *
     * @param userId User ID
     * @param date Date to count entries for
     * @return Number of food entries
     */
    @Query("SELECT COUNT(*) FROM food_entries WHERE user_id = :userId AND date(date_consumed/1000, 'unixepoch') = date(:date/1000, 'unixepoch')")
    suspend fun getFoodEntryCountForDate(userId: Long, date: Long): Int

    /**
     * Gets nutrition summary for a specific date.
     *
     * @param userId User ID
     * @param date Date to get summary for
     * @return Nutrition summary data class
     */
    @Query("""
        SELECT 
            COALESCE(SUM(calories_per_serving * serving_size), 0.0) as totalCalories,
            COALESCE(SUM(protein_grams * serving_size), 0.0) as totalProtein,
            COALESCE(SUM(carbs_grams * serving_size), 0.0) as totalCarbs,
            COALESCE(SUM(fat_grams * serving_size), 0.0) as totalFat,
            COALESCE(SUM(fiber_grams * serving_size), 0.0) as totalFiber
        FROM food_entries 
        WHERE user_id = :userId AND date(date_consumed/1000, 'unixepoch') = date(:date/1000, 'unixepoch')
    """)
    suspend fun getNutritionSummaryForDate(userId: Long, date: Long): NutritionSummary

    /**
     * Data class for nutrition summary.
     */
    data class NutritionSummary(
        val totalCalories: Double,
        val totalProtein: Double,
        val totalCarbs: Double,
        val totalFat: Double,
        val totalFiber: Double
    )
}
