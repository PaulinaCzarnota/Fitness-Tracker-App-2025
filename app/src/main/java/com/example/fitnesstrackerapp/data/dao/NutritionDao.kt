package com.example.fitnesstrackerapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.fitnesstrackerapp.data.entity.FoodEntry
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Data Access Object for Nutrition and FoodEntry entity operations
 */
@Dao
interface NutritionDao {





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
     * Deletes a food entry
     */
    @Delete
    suspend fun deleteFoodEntry(foodEntry: FoodEntry)

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
     * Gets food entries for a specific date
     */
    @Query("SELECT * FROM food_entries WHERE user_id = :userId AND date(logged_at) = date(:date) ORDER BY logged_at DESC")
    fun getFoodEntriesForDate(userId: Long, date: Long): Flow<List<FoodEntry>>

    /**
     * Gets food entries for a date range
     */
    @Query("SELECT * FROM food_entries WHERE user_id = :userId AND logged_at BETWEEN :startDate AND :endDate ORDER BY logged_at DESC")
    fun getFoodEntriesForDateRange(userId: Long, startDate: Long, endDate: Long): Flow<List<FoodEntry>>

    /**
     * Gets all food entries for a user
     */
    @Query("SELECT * FROM food_entries WHERE user_id = :userId ORDER BY logged_at DESC")
    fun getAllFoodEntries(userId: Long): Flow<List<FoodEntry>>
}
