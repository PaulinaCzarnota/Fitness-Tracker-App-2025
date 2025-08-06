package com.example.fitnesstrackerapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.fitnesstrackerapp.data.entity.FoodEntry
import com.example.fitnesstrackerapp.data.entity.Nutrition
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Data Access Object for Nutrition and FoodEntry entity operations
 */
@Dao
interface NutritionDao {

    /**
     * Inserts a new nutrition record into the database
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNutrition(nutrition: Nutrition): Long

    /**
     * Updates an existing nutrition record
     */
    @Update
    suspend fun updateNutrition(nutrition: Nutrition)

    /**
     * Deletes a nutrition record
     */
    @Delete
    suspend fun deleteNutrition(nutrition: Nutrition)

    /**
     * Gets all nutrition entries for a specific user
     */
    @Query("SELECT * FROM nutrition WHERE userId = :userId ORDER BY date DESC")
    fun getNutritionEntriesForUser(userId: Long): Flow<List<Nutrition>>

    /**
     * Inserts a new food entry
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodEntry(foodEntry: FoodEntry): Long

    /**
     * Updates an existing food entry
     */
    @Update
    suspend fun updateFoodEntry(foodEntry: FoodEntry)

    /**
     * Deletes a food entry by ID
     */
    @Query("DELETE FROM food_entries WHERE id = :entryId")
    suspend fun deleteFoodEntry(entryId: Long)

    /**
     * Gets all food entries for a specific user
     */
    @Query("SELECT * FROM food_entries WHERE userId = :userId ORDER BY date DESC")
    fun getFoodEntriesForUser(userId: Long): Flow<List<FoodEntry>>

    /**
     * Gets food entries for a user within a specific date range
     */
    @Query("SELECT * FROM food_entries WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getFoodEntriesForUserAndDateRange(userId: Long, startDate: Date, endDate: Date): Flow<List<FoodEntry>>

    /**
     * Gets total calories consumed by a user on a specific date
     */
    @Query("SELECT COALESCE(SUM(calories), 0) FROM food_entries WHERE userId = :userId AND DATE(date) = DATE(:date)")
    suspend fun getTotalCaloriesForDate(userId: Long, date: Date): Int

    /**
     * Gets nutrition entries for a user on a specific date
     */
    @Query("SELECT * FROM nutrition WHERE userId = :userId AND DATE(date) = DATE(:date)")
    fun getNutritionForUserAndDate(userId: Long, date: Date): Flow<List<Nutrition>>
}
