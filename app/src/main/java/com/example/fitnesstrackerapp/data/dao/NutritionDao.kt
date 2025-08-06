package com.example.fitnesstrackerapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.fitnesstrackerapp.data.entity.MealType
import com.example.fitnesstrackerapp.data.entity.Nutrition
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Data Access Object for Nutrition entity operations.
 *
 * Responsibilities:
 * - Insert, update, delete nutrition records
 * - Query nutrition by user, date, and meal type
 * - Provide nutrition statistics and daily summaries
 * - Handle calorie and macronutrient tracking
 */
@Dao
interface NutritionDao {

    /**
     * Inserts a new nutrition record into the database.
     *
     * @param nutrition Nutrition entity to insert
     * @return The ID of the inserted nutrition record
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNutrition(nutrition: Nutrition): Long

    /**
     * Updates an existing nutrition record in the database.
     *
     * @param nutrition Nutrition entity with updated data
     */
    @Update
    suspend fun updateNutrition(nutrition: Nutrition)

    /**
     * Deletes a nutrition record from the database.
     *
     * @param nutrition Nutrition entity to delete
     */
    @Delete
    suspend fun deleteNutrition(nutrition: Nutrition)

    /**
     * Deletes a nutrition record by its ID.
     *
     * @param nutritionId Nutrition ID to delete
     */
    @Query("DELETE FROM nutrition WHERE id = :nutritionId")
    suspend fun deleteNutritionById(nutritionId: Long)

    /**
     * Gets a nutrition record by its ID.
     *
     * @param nutritionId Nutrition ID to search for
     * @return Nutrition entity or null if not found
     */
    @Query("SELECT * FROM nutrition WHERE id = :nutritionId LIMIT 1")
    suspend fun getNutritionById(nutritionId: Long): Nutrition?

    /**
     * Gets all nutrition records for a specific user.
     *
     * @param userId User ID
     * @return Flow of list of nutrition records ordered by date and meal type
     */
    @Query("SELECT * FROM nutrition WHERE userId = :userId ORDER BY date DESC, mealType ASC")
    fun getNutritionByUser(userId: Long): Flow<List<Nutrition>>

    /**
     * Gets nutrition records for a specific date.
     *
     * @param userId User ID
     * @param date Date to search for
     * @return Flow of list of nutrition records for the date
     */
    @Query("SELECT * FROM nutrition WHERE userId = :userId AND date = :date ORDER BY mealType ASC")
    fun getNutritionByDate(userId: Long, date: Date): Flow<List<Nutrition>>

    /**
     * Gets nutrition records for a specific meal type and date.
     *
     * @param userId User ID
     * @param date Date to search for
     * @param mealType Meal type to filter by
     * @return Flow of list of nutrition records
     */
    @Query("SELECT * FROM nutrition WHERE userId = :userId AND date = :date AND mealType = :mealType")
    fun getNutritionByMealType(userId: Long, date: Date, mealType: MealType): Flow<List<Nutrition>>

    /**
     * Gets nutrition records within a date range.
     *
     * @param userId User ID
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return Flow of list of nutrition records in date range
     */
    @Query("SELECT * FROM nutrition WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC, mealType ASC")
    fun getNutritionInDateRange(userId: Long, startDate: Date, endDate: Date): Flow<List<Nutrition>>

    /**
     * Gets recent nutrition records with limit.
     *
     * @param userId User ID
     * @param limit Maximum number of records to return
     * @return Flow of list of recent nutrition records
     */
    @Query("SELECT * FROM nutrition WHERE userId = :userId ORDER BY date DESC, createdAt DESC LIMIT :limit")
    fun getRecentNutrition(userId: Long, limit: Int): Flow<List<Nutrition>>

    /**
     * Gets daily calorie summary for a specific date.
     *
     * @param userId User ID
     * @param date Date to calculate for
     * @return Total calories for the date
     */
    @Query("SELECT SUM(calories) FROM nutrition WHERE userId = :userId AND date = :date")
    suspend fun getDailyCalories(userId: Long, date: Date): Float?

    /**
     * Gets daily macronutrient summary for a specific date.
     *
     * @param userId User ID
     * @param date Date to calculate for
     * @return Daily nutrition summary
     */
    @Query("""
        SELECT 
            SUM(calories) as totalCalories,
            SUM(protein) as totalProtein,
            SUM(carbohydrates) as totalCarbs,
            SUM(fat) as totalFat,
            SUM(fiber) as totalFiber,
            SUM(sugar) as totalSugar,
            SUM(sodium) as totalSodium
        FROM nutrition 
        WHERE userId = :userId AND date = :date
    """)
    suspend fun getDailyNutritionSummary(userId: Long, date: Date): DailyNutritionSummary?

    /**
     * Gets calories by meal type for a specific date.
     *
     * @param userId User ID
     * @param date Date to calculate for
     * @return Map of meal types to calorie totals
     */
    @Query("SELECT mealType, SUM(calories) as calories FROM nutrition WHERE userId = :userId AND date = :date GROUP BY mealType")
    suspend fun getCaloriesByMealType(userId: Long, date: Date): Map<MealType, Float>

    /**
     * Gets weekly nutrition summary.
     *
     * @param userId User ID
     * @param weekStart Start of week
     * @param weekEnd End of week
     * @return Weekly nutrition statistics
     */
    @Query("""
        SELECT 
            SUM(calories) as totalCalories,
            SUM(protein) as totalProtein,
            SUM(carbohydrates) as totalCarbs,
            SUM(fat) as totalFat,
            AVG(calories) as avgDailyCalories,
            COUNT(DISTINCT date) as daysLogged
        FROM nutrition 
        WHERE userId = :userId AND date BETWEEN :weekStart AND :weekEnd
    """)
    suspend fun getWeeklyNutritionSummary(userId: Long, weekStart: Date, weekEnd: Date): WeeklyNutritionSummary?

    /**
     * Gets most frequently consumed foods.
     *
     * @param userId User ID
     * @param limit Number of top foods to return
     * @return List of food names with consumption count
     */
    @Query("SELECT foodName, COUNT(*) as count FROM nutrition WHERE userId = :userId GROUP BY foodName ORDER BY count DESC LIMIT :limit")
    suspend fun getTopFoods(userId: Long, limit: Int): List<FoodFrequency>

    /**
     * Gets total nutrition record count for a user.
     *
     * @param userId User ID
     * @return Number of nutrition records
     */
    @Query("SELECT COUNT(*) FROM nutrition WHERE userId = :userId")
    suspend fun getNutritionRecordCount(userId: Long): Int

    /**
     * Gets average daily calories over a period.
     *
     * @param userId User ID
     * @param startDate Start date
     * @param endDate End date
     * @return Average daily calories
     */
    @Query("""
        SELECT AVG(dailyCalories) FROM (
            SELECT date, SUM(calories) as dailyCalories 
            FROM nutrition 
            WHERE userId = :userId AND date BETWEEN :startDate AND :endDate 
            GROUP BY date
        )
    """)
    suspend fun getAverageDailyCalories(userId: Long, startDate: Date, endDate: Date): Float?

    /**
     * Gets days with complete nutrition logging (all main meals).
     *
     * @param userId User ID
     * @param startDate Start date
     * @param endDate End date
     * @return Number of days with complete logging
     */
    @Query("""
        SELECT COUNT(DISTINCT date) FROM nutrition 
        WHERE userId = :userId 
        AND date BETWEEN :startDate AND :endDate 
        AND date IN (
            SELECT date FROM nutrition 
            WHERE userId = :userId 
            AND mealType IN ('BREAKFAST', 'LUNCH', 'DINNER') 
            GROUP BY date 
            HAVING COUNT(DISTINCT mealType) = 3
        )
    """)
    suspend fun getCompleteDays(userId: Long, startDate: Date, endDate: Date): Int

    /**
     * Searches nutrition records by food name.
     *
     * @param userId User ID
     * @param searchQuery Search term
     * @return Flow of matching nutrition records
     */
    @Query("SELECT * FROM nutrition WHERE userId = :userId AND foodName LIKE '%' || :searchQuery || '%' ORDER BY date DESC")
    fun searchNutritionByFood(userId: Long, searchQuery: String): Flow<List<Nutrition>>

    /**
     * Deletes all nutrition records for a user.
     *
     * @param userId User ID
     */
    @Query("DELETE FROM nutrition WHERE userId = :userId")
    suspend fun deleteAllUserNutrition(userId: Long)

    /**
     * Gets highest calorie day for a user.
     *
     * @param userId User ID
     * @return Date and total calories for highest calorie day
     */
    @Query("""
        SELECT date, SUM(calories) as totalCalories 
        FROM nutrition 
        WHERE userId = :userId 
        GROUP BY date 
        ORDER BY totalCalories DESC 
        LIMIT 1
    """)
    suspend fun getHighestCalorieDay(userId: Long): DailyCalorieTotal?
}

/**
 * Data class for daily nutrition summary.
 */
data class DailyNutritionSummary(
    val totalCalories: Float,
    val totalProtein: Float,
    val totalCarbs: Float,
    val totalFat: Float,
    val totalFiber: Float,
    val totalSugar: Float,
    val totalSodium: Float
)

/**
 * Data class for weekly nutrition summary.
 */
data class WeeklyNutritionSummary(
    val totalCalories: Float,
    val totalProtein: Float,
    val totalCarbs: Float,
    val totalFat: Float,
    val avgDailyCalories: Float,
    val daysLogged: Int
)

/**
 * Data class for food frequency tracking.
 */
data class FoodFrequency(
    val foodName: String,
    val count: Int
)

/**
 * Data class for daily calorie totals.
 */
data class DailyCalorieTotal(
    val date: Date,
    val totalCalories: Float
)
