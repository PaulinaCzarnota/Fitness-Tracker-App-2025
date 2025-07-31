package com.example.fitnesstrackerapp.repository

import com.example.fitnesstrackerapp.data.dao.FoodDao
import com.example.fitnesstrackerapp.data.entity.FoodEntry
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Repository for food tracking data operations
 */
class FoodRepository @Inject constructor(
    private val foodDao: FoodDao
) {
    fun getFoodEntriesByUser(userId: String): Flow<List<FoodEntry>> =
        foodDao.getFoodEntriesByUser(userId)

    suspend fun insertFoodEntry(foodEntry: FoodEntry): Long =
        foodDao.insertFoodEntry(foodEntry)

    suspend fun updateFoodEntry(foodEntry: FoodEntry) =
        foodDao.updateFoodEntry(foodEntry)

    suspend fun deleteFoodEntry(foodEntry: FoodEntry) =
        foodDao.deleteFoodEntry(foodEntry)

    fun getFoodEntriesByDateRange(userId: String, startDate: Long, endDate: Long): Flow<List<FoodEntry>> =
        foodDao.getFoodEntriesByDateRange(userId, startDate, endDate)

    suspend fun getTotalCaloriesConsumed(userId: String, startDate: Long, endDate: Long): Int? =
        foodDao.getTotalCaloriesConsumed(userId, startDate, endDate)

    fun getFoodEntriesByMealType(userId: String, mealType: String): Flow<List<FoodEntry>> =
        foodDao.getFoodEntriesByMealType(userId, mealType)
}
