package com.example.fitnesstrackerapp.fake

import com.example.fitnesstrackerapp.data.entity.FoodEntry
import com.example.fitnesstrackerapp.data.entity.MealType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.*

/**
 * Fake repository for testing food entry operations.
 * Provides in-memory storage and basic CRUD operations for food entries.
 */
class FakeFoodEntryRepository {
    private val foodEntries = mutableListOf<FoodEntry>()
    private var nextId = 1L

    suspend fun insertFoodEntry(foodEntry: FoodEntry): Long {
        val newEntry = foodEntry.copy(id = nextId++)
        foodEntries.add(newEntry)
        return newEntry.id
    }

    suspend fun updateFoodEntry(foodEntry: FoodEntry) {
        val index = foodEntries.indexOfFirst { it.id == foodEntry.id }
        if (index != -1) {
            foodEntries[index] = foodEntry
        }
    }

    suspend fun deleteFoodEntry(foodEntry: FoodEntry) {
        foodEntries.removeIf { it.id == foodEntry.id }
    }

    fun getFoodEntriesForDate(userId: Long, date: Date): Flow<List<FoodEntry>> {
        return flowOf(foodEntries.filter { it.userId == userId })
    }

    fun getFoodEntriesByMealType(userId: Long, date: Date, mealType: MealType): Flow<List<FoodEntry>> {
        return flowOf(foodEntries.filter { it.userId == userId && it.mealType == mealType })
    }

    suspend fun getTotalCaloriesForDate(userId: Long, date: Date): Double {
        return foodEntries.filter { it.userId == userId }.sumOf { it.caloriesPerServing * it.servingSize }
    }

    // Test helper methods
    fun addTestData(entries: List<FoodEntry>) {
        foodEntries.clear()
        foodEntries.addAll(entries.mapIndexed { index, entry ->
            entry.copy(id = (index + 1).toLong())
        })
        nextId = foodEntries.size.toLong() + 1
    }

    fun clearData() {
        foodEntries.clear()
        nextId = 1L
    }

    fun getTestData(): List<FoodEntry> = foodEntries.toList()

    fun getEntriesCount(): Int = foodEntries.size

    fun getFoodEntriesByUserId(userId: Long): Flow<List<FoodEntry>> {
        return flowOf(foodEntries.filter { it.userId == userId })
    }
}
