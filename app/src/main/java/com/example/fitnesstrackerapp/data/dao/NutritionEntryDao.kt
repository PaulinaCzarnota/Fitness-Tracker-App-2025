package com.example.fitnesstrackerapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.fitnesstrackerapp.data.entity.MealType
import com.example.fitnesstrackerapp.data.entity.NutritionEntry
import com.example.fitnesstrackerapp.data.model.DailyNutritionSummary
import com.example.fitnesstrackerapp.data.model.FoodConsumptionStats
import com.example.fitnesstrackerapp.data.model.MacroTotals
import com.example.fitnesstrackerapp.data.model.MealDistribution
import com.example.fitnesstrackerapp.data.model.NutritionSummary
import com.example.fitnesstrackerapp.data.model.WeeklyNutritionSummary
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Nutrition Entry Data Access Object for the Fitness Tracker application.
 *
 * This DAO provides comprehensive database operations for NutritionEntry entities including
 * nutrition tracking, meal categorization, daily intake analysis, dietary statistics,
 * and advanced nutritional analytics. All operations are coroutine-based for optimal
 * performance and UI responsiveness.
 *
 * Key Features:
 * - Complete CRUD operations for nutrition entries
 * - Advanced nutritional analysis and calculations
 * - Meal type categorization and filtering
 * - Micronutrient tracking and analysis
 * - Date-based queries for dietary trends
 * - Nutritional quality scoring and recommendations
 * - Food frequency and consumption pattern analysis
 * - Comprehensive aggregation functions
 */
@Dao
interface NutritionEntryDao {
    // MARK: - Basic CRUD Operations

    /**
     * Inserts a new nutrition entry into the database.
     *
     * @param nutritionEntry NutritionEntry entity to insert
     * @return The ID of the inserted nutrition entry
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNutritionEntry(nutritionEntry: NutritionEntry): Long

    /**
     * Alternative insert method for compatibility.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(nutritionEntry: NutritionEntry): Long

    /**
     * Inserts multiple nutrition entries into the database.
     *
     * @param nutritionEntries List of NutritionEntry entities to insert
     * @return List of IDs of the inserted nutrition entries
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(nutritionEntries: List<NutritionEntry>): List<Long>

    /**
     * Updates an existing nutrition entry in the database.
     *
     * @param nutritionEntry NutritionEntry entity with updated data
     */
    @Update
    suspend fun updateNutritionEntry(nutritionEntry: NutritionEntry)

    /**
     * Alternative update method for compatibility.
     */
    @Update
    suspend fun update(nutritionEntry: NutritionEntry)

    /**
     * Deletes a nutrition entry from the database.
     *
     * @param nutritionEntry NutritionEntry entity to delete
     */
    @Delete
    suspend fun deleteNutritionEntry(nutritionEntry: NutritionEntry)

    /**
     * Alternative delete method for compatibility.
     */
    @Delete
    suspend fun delete(nutritionEntry: NutritionEntry)

    /**
     * Deletes a nutrition entry by its ID.
     *
     * @param nutritionEntryId Nutrition entry ID to delete
     */
    @Query("DELETE FROM nutrition_entries WHERE id = :nutritionEntryId")
    suspend fun deleteNutritionEntryById(nutritionEntryId: Long)

    /**
     * Gets a nutrition entry by its ID.
     *
     * @param nutritionEntryId Nutrition entry ID to retrieve
     * @return NutritionEntry entity or null if not found
     */
    @Query("SELECT * FROM nutrition_entries WHERE id = :nutritionEntryId LIMIT 1")
    suspend fun getNutritionEntryById(nutritionEntryId: Long): NutritionEntry?

    // MARK: - User-based Queries

    /**
     * Gets all nutrition entries for a specific user ordered by consumption date.
     *
     * @param userId User ID
     * @return Flow of list of nutrition entries
     */
    @Query("SELECT * FROM nutrition_entries WHERE user_id = :userId ORDER BY date_consumed DESC")
    fun getNutritionEntriesByUserId(userId: Long): Flow<List<NutritionEntry>>

    /**
     * Gets recent nutrition entries with limit.
     *
     * @param userId User ID
     * @param limit Maximum number of entries to return
     * @return Flow of recent nutrition entries
     */
    @Query("SELECT * FROM nutrition_entries WHERE user_id = :userId ORDER BY date_consumed DESC LIMIT :limit")
    fun getRecentNutritionEntries(userId: Long, limit: Int): Flow<List<NutritionEntry>>

    /**
     * Gets total nutrition entries count for a user.
     *
     * @param userId User ID
     * @return Total number of nutrition entries
     */
    @Query("SELECT COUNT(*) FROM nutrition_entries WHERE user_id = :userId")
    suspend fun getTotalNutritionEntriesCount(userId: Long): Int

    // MARK: - Date-based Queries

    /**
     * Gets nutrition entries for a specific date.
     *
     * @param userId User ID
     * @param date Date to get entries for
     * @return Flow of list of nutrition entries
     */
    @Query(
        """
        SELECT * FROM nutrition_entries
        WHERE user_id = :userId AND DATE(date_consumed/1000, 'unixepoch') = DATE(:date/1000, 'unixepoch')
        ORDER BY date_consumed DESC
    """,
    )
    fun getNutritionEntriesForDate(userId: Long, date: Date): Flow<List<NutritionEntry>>

    /**
     * Gets nutrition entries for a date range.
     *
     * @param userId User ID
     * @param startDate Start date
     * @param endDate End date
     * @return Flow of list of nutrition entries
     */
    @Query(
        """
        SELECT * FROM nutrition_entries
        WHERE user_id = :userId AND date_consumed BETWEEN :startDate AND :endDate
        ORDER BY date_consumed DESC
    """,
    )
    fun getNutritionEntriesForDateRange(userId: Long, startDate: Date, endDate: Date): Flow<List<NutritionEntry>>

    /**
     * Gets nutrition entries count for a specific date.
     *
     * @param userId User ID
     * @param date Date
     * @return Number of nutrition entries for the date
     */
    @Query(
        """
        SELECT COUNT(*) FROM nutrition_entries
        WHERE user_id = :userId AND DATE(date_consumed/1000, 'unixepoch') = DATE(:date/1000, 'unixepoch')
    """,
    )
    suspend fun getNutritionEntriesCountForDate(userId: Long, date: Date): Int

    // MARK: - Meal Type Queries

    /**
     * Gets nutrition entries by meal type for a specific date.
     *
     * @param userId User ID
     * @param date Date
     * @param mealType Meal type
     * @return Flow of list of nutrition entries
     */
    @Query(
        """
        SELECT * FROM nutrition_entries
        WHERE user_id = :userId
        AND DATE(date_consumed/1000, 'unixepoch') = DATE(:date/1000, 'unixepoch')
        AND meal_type = :mealType
        ORDER BY date_consumed DESC
    """,
    )
    fun getNutritionEntriesByMealType(userId: Long, mealType: MealType, date: Date): Flow<List<NutritionEntry>>

    /**
     * Gets meal distribution statistics.
     *
     * @param userId User ID
     * @param date Date
     * @return List of meal type calorie distributions
     */
    @Query(
        """
        SELECT meal_type AS meal_type, SUM(calories_per_serving * serving_size) AS calories
        FROM nutrition_entries
        WHERE user_id = :userId AND DATE(date_consumed/1000, 'unixepoch') = DATE(:date/1000, 'unixepoch')
        GROUP BY meal_type
    """,
    )
    suspend fun getMealDistributionForDate(userId: Long, date: Date): List<MealDistribution>

    // MARK: - Nutritional Analysis Queries

    /**
     * Gets total calories consumed for a specific date.
     *
     * @param userId User ID
     * @param date Date
     * @return Total calories for the date
     */
    @Query(
        """
        SELECT SUM(calories_per_serving * serving_size)
        FROM nutrition_entries
        WHERE user_id = :userId AND DATE(date_consumed/1000, 'unixepoch') = DATE(:date/1000, 'unixepoch')
    """,
    )
    suspend fun getTotalCaloriesForDate(userId: Long, date: Date): Double?

    /**
     * Gets total macronutrients for a specific date.
     *
     * @param userId User ID
     * @param date Date
     * @return MacroTotals with (total protein, total carbs, total fat) in grams
     */
    @Query(
        """
        SELECT
        SUM(protein_grams * serving_size) AS total_protein,
        SUM(carbs_grams * serving_size) AS total_carbs,
        SUM(fat_grams * serving_size) AS total_fat
        FROM nutrition_entries
        WHERE user_id = :userId AND DATE(date_consumed/1000, 'unixepoch') = DATE(:date/1000, 'unixepoch')
    """,
    )
    suspend fun getTotalMacrosForDate(userId: Long, date: Date): MacroTotals?

    /**
     * Gets comprehensive nutrition summary for a specific date.
     *
     * @param userId User ID
     * @param date Date
     * @return NutritionSummary with complete nutrition totals
     */
    @Query(
        """
        SELECT
        SUM(calories_per_serving * serving_size) AS total_calories,
        SUM(protein_grams * serving_size) AS total_protein,
        SUM(carbs_grams * serving_size) AS total_carbs,
        SUM(fat_grams * serving_size) AS total_fat,
        SUM(saturated_fat_grams * serving_size) AS total_saturated_fat,
        SUM(trans_fat_grams * serving_size) AS total_trans_fat,
        SUM(cholesterol_mg * serving_size) AS total_cholesterol,
        SUM(fiber_grams * serving_size) AS total_fiber,
        SUM(sugar_grams * serving_size) AS total_sugar,
        SUM(added_sugars_grams * serving_size) AS total_added_sugars,
        SUM(sodium_mg * serving_size) AS total_sodium,
        SUM(potassium_mg * serving_size) AS total_potassium,
        SUM(vitamin_c_mg * serving_size) AS total_vitamin_c,
        SUM(vitamin_d_mcg * serving_size) AS total_vitamin_d,
        SUM(calcium_mg * serving_size) AS total_calcium,
        SUM(iron_mg * serving_size) AS total_iron
        FROM nutrition_entries
        WHERE user_id = :userId AND DATE(date_consumed/1000, 'unixepoch') = DATE(:date/1000, 'unixepoch')
    """,
    )
    suspend fun getNutritionSummaryForDate(userId: Long, date: Date): NutritionSummary?

    /**
     * Gets nutrition summary for a date range.
     *
     * @param userId User ID
     * @param startDate Start date
     * @param endDate End date
     * @return NutritionSummary with nutrition totals
     */
    @Query(
        """
        SELECT
        SUM(calories_per_serving * serving_size) AS total_calories,
        SUM(protein_grams * serving_size) AS total_protein,
        SUM(carbs_grams * serving_size) AS total_carbs,
        SUM(fat_grams * serving_size) AS total_fat,
        SUM(saturated_fat_grams * serving_size) AS total_saturated_fat,
        SUM(trans_fat_grams * serving_size) AS total_trans_fat,
        SUM(cholesterol_mg * serving_size) AS total_cholesterol,
        SUM(fiber_grams * serving_size) AS total_fiber,
        SUM(sugar_grams * serving_size) AS total_sugar,
        SUM(added_sugars_grams * serving_size) AS total_added_sugars,
        SUM(sodium_mg * serving_size) AS total_sodium,
        SUM(potassium_mg * serving_size) AS total_potassium,
        SUM(vitamin_c_mg * serving_size) AS total_vitamin_c,
        SUM(vitamin_d_mcg * serving_size) AS total_vitamin_d,
        SUM(calcium_mg * serving_size) AS total_calcium,
        SUM(iron_mg * serving_size) AS total_iron
        FROM nutrition_entries
        WHERE user_id = :userId AND date_consumed BETWEEN :startDate AND :endDate
    """,
    )
    suspend fun getNutritionSummaryForDateRange(userId: Long, startDate: Date, endDate: Date): NutritionSummary?

    /**
     * Gets average daily calories for a user.
     *
     * @param userId User ID
     * @return Average daily calorie intake
     */
    @Query(
        """
        SELECT AVG(daily_calories) FROM (
            SELECT DATE(date_consumed) AS date, SUM(calories_per_serving * serving_size) AS daily_calories
            FROM nutrition_entries
            WHERE user_id = :userId
            GROUP BY DATE(date_consumed)
        )
    """,
    )
    suspend fun getAverageDailyCalories(userId: Long): Double?

    /**
     * Gets weekly nutrition summary.
     *
     * @param userId User ID
     * @param weekStart Start of the week
     * @param weekEnd End of the week
     * @return Weekly nutrition totals
     */
    @Query(
        """
        SELECT
        SUM(calories_per_serving * serving_size) AS weekly_calories,
        AVG(calories_per_serving * serving_size) AS avg_daily_calories,
        SUM(protein_grams * serving_size) AS weekly_protein,
        SUM(carbs_grams * serving_size) AS weekly_carbs,
        SUM(fat_grams * serving_size) AS weekly_fat
        FROM nutrition_entries
        WHERE user_id = :userId AND date_consumed BETWEEN :weekStart AND :weekEnd
    """,
    )
    suspend fun getWeeklyNutritionSummary(userId: Long, weekStart: Date, weekEnd: Date): WeeklyNutritionSummary?

    // MARK: - Food Analysis Queries

    /**
     * Gets most consumed foods.
     *
     * @param userId User ID
     * @param limit Number of top foods to return
     * @return List of most frequently consumed foods
     */
    @Query(
        """
        SELECT food_name AS food_name, COUNT(*) AS frequency, AVG(calories_per_serving * serving_size) AS avg_calories
        FROM nutrition_entries
        WHERE user_id = :userId
        GROUP BY food_name
        ORDER BY frequency DESC
        LIMIT :limit
    """,
    )
    suspend fun getMostConsumedFoods(userId: Long, limit: Int): List<FoodConsumptionStats>

    /**
     * Gets highest calorie foods consumed.
     *
     * @param userId User ID
     * @param limit Number of top foods to return
     * @return List of highest calorie foods
     */
    @Query(
        """
        SELECT food_name AS food_name, MAX(calories_per_serving * serving_size) AS avg_calories,
        AVG(calories_per_serving * serving_size) AS avg_calories,
        COUNT(*) AS frequency
        FROM nutrition_entries
        WHERE user_id = :userId
        GROUP BY food_name
        ORDER BY avg_calories DESC
        LIMIT :limit
    """,
    )
    suspend fun getHighestCalorieFoods(userId: Long, limit: Int): List<FoodConsumptionStats>

    /**
     * Gets foods with best nutritional quality scores.
     *
     * @param userId User ID
     * @param limit Number of top foods to return
     * @return List of foods with highest quality scores
     */
    @Query(
        """
        SELECT food_name AS food_name, AVG(confidence_level) AS avg_calories,
        COUNT(*) AS frequency,
        AVG(calories_per_serving * serving_size) AS avg_calories
        FROM nutrition_entries
        WHERE user_id = :userId AND confidence_level > 0.7
        GROUP BY food_name
        ORDER BY avg_calories DESC
        LIMIT :limit
    """,
    )
    suspend fun getBestQualityFoods(userId: Long, limit: Int): List<FoodConsumptionStats>

    // MARK: - Search and Filter Queries

    /**
     * Searches nutrition entries by food name.
     *
     * @param userId User ID
     * @param searchQuery Search query for food name
     * @return Flow of matching nutrition entries
     */
    @Query(
        """
        SELECT * FROM nutrition_entries
        WHERE user_id = :userId AND food_name LIKE '%' || :searchQuery || '%'
        ORDER BY date_consumed DESC
    """,
    )
    fun searchNutritionEntries(userId: Long, searchQuery: String): Flow<List<NutritionEntry>>

    /**
     * Gets nutrition entries by brand name.
     *
     * @param userId User ID
     * @param brandName Brand name to filter by
     * @return Flow of nutrition entries from the brand
     */
    @Query(
        """
        SELECT * FROM nutrition_entries
        WHERE user_id = :userId AND brand_name = :brandName
        ORDER BY date_consumed DESC
    """,
    )
    fun getNutritionEntriesByBrand(userId: Long, brandName: String): Flow<List<NutritionEntry>>

    /**
     * Gets homemade nutrition entries.
     *
     * @param userId User ID
     * @return Flow of homemade nutrition entries
     */
    @Query(
        """
        SELECT * FROM nutrition_entries
        WHERE user_id = :userId AND is_homemade = 1
        ORDER BY date_consumed DESC
    """,
    )
    fun getHomemadeNutritionEntries(userId: Long): Flow<List<NutritionEntry>>

    /**
     * Gets nutrition entries with high confidence level.
     *
     * @param userId User ID
     * @param minConfidence Minimum confidence level (0.0 to 1.0)
     * @return Flow of high-confidence nutrition entries
     */
    @Query(
        """
        SELECT * FROM nutrition_entries
        WHERE user_id = :userId AND confidence_level >= :minConfidence
        ORDER BY confidence_level DESC, date_consumed DESC
    """,
    )
    fun getHighConfidenceNutritionEntries(userId: Long, minConfidence: Double): Flow<List<NutritionEntry>>

    // MARK: - Micronutrient Analysis

    /**
     * Gets vitamin C intake for a date range.
     *
     * @param userId User ID
     * @param startDate Start date
     * @param endDate End date
     * @return Total vitamin C in mg
     */
    @Query(
        """
        SELECT SUM(vitamin_c_mg * serving_size)
        FROM nutrition_entries
        WHERE user_id = :userId AND date_consumed BETWEEN :startDate AND :endDate
    """,
    )
    suspend fun getTotalVitaminCForDateRange(userId: Long, startDate: Date, endDate: Date): Double?

    /**
     * Gets calcium intake for a date range.
     *
     * @param userId User ID
     * @param startDate Start date
     * @param endDate End date
     * @return Total calcium in mg
     */
    @Query(
        """
        SELECT SUM(calcium_mg * serving_size)
        FROM nutrition_entries
        WHERE user_id = :userId AND date_consumed BETWEEN :startDate AND :endDate
    """,
    )
    suspend fun getTotalCalciumForDateRange(userId: Long, startDate: Date, endDate: Date): Double?

    /**
     * Gets iron intake for a date range.
     *
     * @param userId User ID
     * @param startDate Start date
     * @param endDate End date
     * @return Total iron in mg
     */
    @Query(
        """
        SELECT SUM(iron_mg * serving_size)
        FROM nutrition_entries
        WHERE user_id = :userId AND date_consumed BETWEEN :startDate AND :endDate
    """,
    )
    suspend fun getTotalIronForDateRange(userId: Long, startDate: Date, endDate: Date): Double?

    /**
     * Gets sodium intake for a date.
     *
     * @param userId User ID
     * @param date Date
     * @return Total sodium in mg
     */
    @Query(
        """
        SELECT SUM(sodium_mg * serving_size)
        FROM nutrition_entries
        WHERE user_id = :userId AND DATE(date_consumed/1000, 'unixepoch') = DATE(:date/1000, 'unixepoch')
    """,
    )
    suspend fun getTotalSodiumForDate(userId: Long, date: Date): Double?

    // MARK: - Cleanup Operations

    /**
     * Deletes nutrition entries older than specified date.
     *
     * @param userId User ID
     * @param olderThan Cutoff date
     */
    @Query("DELETE FROM nutrition_entries WHERE user_id = :userId AND date_consumed < :olderThan")
    suspend fun deleteOldNutritionEntries(userId: Long, olderThan: Date)

    /**
     * Deletes all nutrition entries for a user.
     *
     * @param userId User ID
     */
    @Query("DELETE FROM nutrition_entries WHERE user_id = :userId")
    suspend fun deleteAllNutritionEntriesForUser(userId: Long)

    /**
     * Deletes all nutrition entries (for testing purposes only).
     */
    @Query("DELETE FROM nutrition_entries")
    suspend fun deleteAllNutritionEntries()

    // MARK: - Analytics and Reporting

    /**
     * Gets daily nutrition summaries for a date range.
     *
     * @param userId User ID
     * @param startDate Start date
     * @param endDate End date
     * @return List of daily nutrition summaries
     */
    @Query(
        """
        SELECT
        DATE(date_consumed/1000, 'unixepoch') AS date,
        SUM(calories_per_serving * serving_size) AS total_calories,
        SUM(protein_grams * serving_size) AS total_protein,
        SUM(carbs_grams * serving_size) AS total_carbs,
        SUM(fat_grams * serving_size) AS total_fat,
        SUM(fiber_grams * serving_size) AS total_fiber,
        SUM(sodium_mg * serving_size) AS total_sodium,
        COUNT(*) AS entry_count
        FROM nutrition_entries
        WHERE user_id = :userId AND date_consumed BETWEEN :startDate AND :endDate
        GROUP BY DATE(date_consumed/1000, 'unixepoch')
        ORDER BY date DESC
    """,
    )
    suspend fun getDailyNutritionSummaries(userId: Long, startDate: Date, endDate: Date): List<DailyNutritionSummary>

    /**
     * Gets nutrition entries with nutrition quality issues.
     *
     * @param userId User ID
     * @param maxSodium Maximum sodium per serving (mg)
     * @param minFiber Minimum fiber per serving (g)
     * @return Flow of nutrition entries that may need attention
     */
    @Query(
        """
        SELECT * FROM nutrition_entries
        WHERE user_id = :userId
        AND (sodium_mg > :maxSodium OR fiber_grams < :minFiber)
        ORDER BY date_consumed DESC
    """,
    )
    fun getNutritionEntriesWithQualityIssues(
        userId: Long,
        maxSodium: Double = 600.0,
        minFiber: Double = 1.0,
    ): Flow<List<NutritionEntry>>
}
