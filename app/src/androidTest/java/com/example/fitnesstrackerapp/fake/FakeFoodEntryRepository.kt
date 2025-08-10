package com.example.fitnesstrackerapp.fake

import com.example.fitnesstrackerapp.data.entity.FoodEntry
import com.example.fitnesstrackerapp.data.entity.MealType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.Date

/**
 * Fake repository for testing food entry operations.
 * Provides in-memory storage and basic CRUD operations for food entries.
 */
class FakeFoodEntryRepository {
    private val foodEntries = mutableListOf<FoodEntry>()
    private var nextId = 1L

    /**
     * Inserts a new food entry and returns its generated ID.
     */
    fun insertFoodEntry(foodEntry: FoodEntry): Long {
        val newEntry = foodEntry.copy(id = nextId++)
        foodEntries.add(newEntry)
        return newEntry.id
    }

    /**
     * Updates an existing food entry by ID.
     */
    fun updateFoodEntry(foodEntry: FoodEntry) {
        val index = foodEntries.indexOfFirst { it.id == foodEntry.id }
        if (index != -1) {
            foodEntries[index] = foodEntry
        }
    }

    /**
     * Deletes a food entry by ID.
     */
    fun deleteFoodEntry(foodEntry: FoodEntry) {
        foodEntries.removeIf { it.id == foodEntry.id }
    }

    /**
     * Returns all food entries for a user. Ignores the date parameter for simplicity in tests.
     */
    fun getFoodEntriesForDate(userId: Long, date: Date): Flow<List<FoodEntry>> {
        return flowOf(foodEntries.filter { it.userId == userId })
    }

    /**
     * Returns all food entries for a user and meal type. Ignores the date parameter for simplicity in tests.
     */
    fun getFoodEntriesByMealType(userId: Long, date: Date, mealType: MealType): Flow<List<FoodEntry>> {
        return flowOf(foodEntries.filter { it.userId == userId && it.mealType == mealType })
    }

    /**
     * Returns the total calories for a user. Ignores the date parameter for simplicity in tests.
     */
    fun getTotalCaloriesForDate(userId: Long, date: Date): Double {
        return foodEntries.filter { it.userId == userId }.sumOf { it.caloriesPerServing * it.servingSize }
    }

    // Test helper methods
    fun addTestData(entries: List<FoodEntry>) {
        foodEntries.clear()
        foodEntries.addAll(
            entries.mapIndexed { index, entry ->
                entry.copy(id = (index + 1).toLong())
            },
        )
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
