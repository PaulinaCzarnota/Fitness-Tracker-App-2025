package com.example.fitnesstrackerapp.repository

import com.example.fitnesstrackerapp.data.dao.FoodEntryDao
import com.example.fitnesstrackerapp.data.entity.FoodEntry
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.Date

class NutritionRepository(private val foodEntryDao: FoodEntryDao) {
    fun getTodaysFoodEntries(userId: Long): Flow<List<FoodEntry>> {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        return foodEntryDao.getFoodEntriesForDate(userId, today)
    }

    suspend fun addFoodEntry(foodEntry: FoodEntry): Long {
        return foodEntryDao.insert(foodEntry)
    }

    suspend fun updateFoodEntry(foodEntry: FoodEntry) {
        foodEntryDao.update(foodEntry)
    }

    suspend fun deleteFoodEntry(foodEntry: FoodEntry) {
        foodEntryDao.delete(foodEntry)
    }

    fun getCaloriesForDate(userId: Long, date: Date): Flow<Double> {
        return foodEntryDao.getTotalCaloriesForDate(userId, date.time)
    }

    fun getFoodEntriesForDateRange(userId: Long, startDate: Date, endDate: Date): Flow<List<FoodEntry>> {
        return foodEntryDao.getFoodEntriesForDateRange(userId, startDate.time, endDate.time)
    }
}
