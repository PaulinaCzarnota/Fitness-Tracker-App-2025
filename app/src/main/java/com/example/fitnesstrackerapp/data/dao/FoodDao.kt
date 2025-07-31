package com.example.fitnesstrackerapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.fitnesstrackerapp.data.entity.FoodEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    
    @Insert
    suspend fun insertFoodEntry(foodEntry: FoodEntry): Long
    
    @Update
    suspend fun updateFoodEntry(foodEntry: FoodEntry)
    
    @Delete
    suspend fun deleteFoodEntry(foodEntry: FoodEntry)
    
    @Query("SELECT * FROM food_entries WHERE userId = :userId ORDER BY date DESC")
    fun getFoodEntriesByUser(userId: String): Flow<List<FoodEntry>>
    
    @Query("SELECT * FROM food_entries WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getFoodEntriesByDateRange(userId: String, startDate: Long, endDate: Long): Flow<List<FoodEntry>>
    
    @Query("SELECT SUM(calories) FROM food_entries WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalCaloriesConsumed(userId: String, startDate: Long, endDate: Long): Int?
    
    @Query("SELECT * FROM food_entries WHERE userId = :userId AND mealType = :mealType ORDER BY date DESC")
    fun getFoodEntriesByMealType(userId: String, mealType: String): Flow<List<FoodEntry>>
}
