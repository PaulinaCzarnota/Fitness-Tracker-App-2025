package com.example.fitnesstrackerapp.data.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitnesstrackerapp.data.database.AppDatabase
import com.example.fitnesstrackerapp.data.entity.*
import com.example.fitnesstrackerapp.util.test.TestHelper
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.runner.RunWith
import java.io.IOException
import java.util.*

/**
 * Comprehensive unit tests for FoodEntryDao.
 *
 * Tests all nutrition-related database operations including:
 * - CRUD operations for food entries
 * - Meal type filtering and categorization
 * - Nutritional data aggregation and calculation
 * - Date-based queries for nutrition tracking
 * - Calorie and macro nutrient summaries
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class FoodEntryDaoTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AppDatabase
    private lateinit var foodEntryDao: FoodEntryDao
    private lateinit var userDao: UserDao

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        )
            .allowMainThreadQueries()
            .build()

        foodEntryDao = database.foodEntryDao()
        userDao = database.userDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndGetFoodEntry() = runTest {
        // Create user first
        val user = TestHelper.createTestUser(email = "food@test.com", username = "fooduser")
        val userId = userDao.insertUser(user)

        // Create and insert food entry
        val foodEntry = TestHelper.createTestFoodEntry(
            userId = userId,
            foodName = "Grilled Chicken",
            caloriesPerServing = 165.0,
            servingSize = 100.0,
            mealType = MealType.LUNCH,
            proteinGrams = 31.0,
        )

        val foodEntryId = foodEntryDao.insertFoodEntry(foodEntry)
        assertThat(foodEntryId).isGreaterThan(0)

        val retrievedEntry = foodEntryDao.getFoodEntryById(foodEntryId)
        assertThat(retrievedEntry).isNotNull()
        assertThat(retrievedEntry?.foodName).isEqualTo("Grilled Chicken")
        assertThat(retrievedEntry?.caloriesPerServing).isEqualTo(165.0)
        assertThat(retrievedEntry?.servingSize).isEqualTo(100.0)
        assertThat(retrievedEntry?.mealType).isEqualTo(MealType.LUNCH)
        assertThat(retrievedEntry?.proteinGrams).isEqualTo(31.0)
        assertThat(retrievedEntry?.userId).isEqualTo(userId)
    }

    @Test
    fun getFoodEntriesByUser() = runTest {
        val user = TestHelper.createTestUser(email = "entries@test.com", username = "entriesuser")
        val userId = userDao.insertUser(user)

        // Insert multiple food entries
        val entries = listOf(
            TestHelper.createTestFoodEntry(userId = userId, foodName = "Apple", mealType = MealType.BREAKFAST),
            TestHelper.createTestFoodEntry(userId = userId, foodName = "Rice", mealType = MealType.LUNCH),
            TestHelper.createTestFoodEntry(userId = userId, foodName = "Salad", mealType = MealType.DINNER),
        )

        entries.forEach { entry ->
            foodEntryDao.insertFoodEntry(entry)
        }

        val userEntries = foodEntryDao.getFoodEntriesByUserId(userId).first()
        assertThat(userEntries).hasSize(3)

        // Should be ordered by date consumed DESC
        val foodNames = userEntries.map { it.foodName }
        assertThat(foodNames).containsExactly("Salad", "Rice", "Apple")
    }

    @Test
    fun getFoodEntriesByMealType() = runTest {
        val user = TestHelper.createTestUser(email = "mealtype@test.com", username = "mealtypeuser")
        val userId = userDao.insertUser(user)

        // Insert entries for different meal types
        foodEntryDao.insertFoodEntry(
            TestHelper.createTestFoodEntry(userId = userId, foodName = "Breakfast Item 1", mealType = MealType.BREAKFAST),
        )
        foodEntryDao.insertFoodEntry(
            TestHelper.createTestFoodEntry(userId = userId, foodName = "Breakfast Item 2", mealType = MealType.BREAKFAST),
        )
        foodEntryDao.insertFoodEntry(
            TestHelper.createTestFoodEntry(userId = userId, foodName = "Lunch Item", mealType = MealType.LUNCH),
        )

        val breakfastEntries = foodEntryDao.getFoodEntriesByMealType(userId, MealType.BREAKFAST).first()
        assertThat(breakfastEntries).hasSize(2)
        assertThat(breakfastEntries.all { it.mealType == MealType.BREAKFAST }).isTrue()

        val lunchEntries = foodEntryDao.getFoodEntriesByMealType(userId, MealType.LUNCH).first()
        assertThat(lunchEntries).hasSize(1)
        assertThat(lunchEntries[0].foodName).isEqualTo("Lunch Item")
    }

    @Test
    fun getFoodEntriesByDate() = runTest {
        val user = TestHelper.createTestUser(email = "bydate@test.com", username = "bydateuser")
        val userId = userDao.insertUser(user)

        val targetDate = Date()
        val todayStart = Date(targetDate.time - (targetDate.time % (24 * 60 * 60 * 1000)))
        Date(todayStart.time + (24 * 60 * 60 * 1000) - 1)

        // Insert entries for today
        val todayEntry1 = TestHelper.createTestFoodEntry(
            userId = userId,
            foodName = "Today Breakfast",
        ).copy(dateConsumed = targetDate)

        val todayEntry2 = TestHelper.createTestFoodEntry(
            userId = userId,
            foodName = "Today Lunch",
        ).copy(dateConsumed = targetDate)

        // Insert entry for yesterday
        val yesterday = Date(targetDate.time - (24 * 60 * 60 * 1000))
        val yesterdayEntry = TestHelper.createTestFoodEntry(
            userId = userId,
            foodName = "Yesterday Dinner",
        ).copy(dateConsumed = yesterday)

        foodEntryDao.insertFoodEntry(todayEntry1)
        foodEntryDao.insertFoodEntry(todayEntry2)
        foodEntryDao.insertFoodEntry(yesterdayEntry)

        val todayEntries = foodEntryDao.getFoodEntriesByDate(userId, targetDate).first()
        assertThat(todayEntries).hasSize(2)
        assertThat(todayEntries.map { it.foodName }).containsExactly("Today Lunch", "Today Breakfast")
    }

    @Test
    fun getFoodEntriesByDateRange() = runTest {
        val user = TestHelper.createTestUser(email = "daterange@test.com", username = "daterangeuser")
        val userId = userDao.insertUser(user)

        val currentTime = System.currentTimeMillis()
        val day1 = Date(currentTime - (2 * 24 * 60 * 60 * 1000)) // 2 days ago
        val day2 = Date(currentTime - (1 * 24 * 60 * 60 * 1000)) // 1 day ago
        val day3 = Date(currentTime) // today
        val day4 = Date(currentTime + (1 * 24 * 60 * 60 * 1000)) // tomorrow

        // Insert entries for different dates
        foodEntryDao.insertFoodEntry(
            TestHelper.createTestFoodEntry(userId = userId, foodName = "Day 1").copy(dateConsumed = day1),
        )
        foodEntryDao.insertFoodEntry(
            TestHelper.createTestFoodEntry(userId = userId, foodName = "Day 2").copy(dateConsumed = day2),
        )
        foodEntryDao.insertFoodEntry(
            TestHelper.createTestFoodEntry(userId = userId, foodName = "Day 3").copy(dateConsumed = day3),
        )
        foodEntryDao.insertFoodEntry(
            TestHelper.createTestFoodEntry(userId = userId, foodName = "Day 4").copy(dateConsumed = day4),
        )

        // Query for entries from day2 to day3 (inclusive)
        val entriesInRange = foodEntryDao.getFoodEntriesByDateRange(userId, day2, day3).first()
        assertThat(entriesInRange).hasSize(2)
        assertThat(entriesInRange.map { it.foodName }).containsExactly("Day 3", "Day 2")
    }

    @Test
    fun getTotalCaloriesByDate() = runTest {
        val user = TestHelper.createTestUser(email = "calories@test.com", username = "caloriesuser")
        val userId = userDao.insertUser(user)

        val targetDate = Date()

        // Insert food entries with different calories for today
        val entries = listOf(
            TestHelper.createTestFoodEntry(
                userId = userId,
                foodName = "Breakfast",
                caloriesPerServing = 300.0,
                servingSize = 1.0,
            ).copy(dateConsumed = targetDate),

            TestHelper.createTestFoodEntry(
                userId = userId,
                foodName = "Lunch",
                caloriesPerServing = 500.0,
                servingSize = 1.0,
            ).copy(dateConsumed = targetDate),

            TestHelper.createTestFoodEntry(
                userId = userId,
                foodName = "Dinner",
                caloriesPerServing = 400.0,
                servingSize = 1.5,
            ).copy(dateConsumed = targetDate),
        )

        entries.forEach { entry ->
            foodEntryDao.insertFoodEntry(entry)
        }

        val totalCalories = foodEntryDao.getTotalCaloriesByDate(userId, targetDate)
        // 300 + 500 + (400 * 1.5) = 1400
        assertThat(totalCalories).isEqualTo(1400.0)
    }

    @Test
    fun getNutritionSummaryByDate() = runTest {
        val user = TestHelper.createTestUser(email = "summary@test.com", username = "summaryuser")
        val userId = userDao.insertUser(user)

        val targetDate = Date()

        // Insert food entries with different nutritional values
        val entries = listOf(
            TestHelper.createTestFoodEntry(
                userId = userId,
                foodName = "Chicken Breast",
                caloriesPerServing = 165.0,
                servingSize = 1.0,
                proteinGrams = 31.0,
            ).copy(
                dateConsumed = targetDate,
                carbsGrams = 0.0,
                fatGrams = 3.6,
            ),

            TestHelper.createTestFoodEntry(
                userId = userId,
                foodName = "Brown Rice",
                caloriesPerServing = 216.0,
                servingSize = 1.0,
                proteinGrams = 5.0,
            ).copy(
                dateConsumed = targetDate,
                carbsGrams = 45.0,
                fatGrams = 1.8,
            ),
        )

        entries.forEach { entry ->
            foodEntryDao.insertFoodEntry(entry)
        }

        val nutritionSummary = foodEntryDao.getNutritionSummaryByDate(userId, targetDate)
        assertThat(nutritionSummary).isNotNull()
        assertThat(nutritionSummary?.totalCalories).isEqualTo(381.0) // 165 + 216
        assertThat(nutritionSummary?.totalProtein).isEqualTo(36.0) // 31 + 5
        assertThat(nutritionSummary?.totalCarbs).isEqualTo(45.0) // 0 + 45
        assertThat(nutritionSummary?.totalFat).isEqualTo(5.4) // 3.6 + 1.8
    }

    @Test
    fun getMostConsumedFoods() = runTest {
        val user = TestHelper.createTestUser(email = "mostconsumed@test.com", username = "mostconsumeduser")
        val userId = userDao.insertUser(user)

        // Insert multiple entries for the same foods
        val foods = mapOf(
            "Apple" to 3, // Most consumed
            "Banana" to 2,
            "Orange" to 1,
        )

        foods.forEach { (foodName, count) ->
            repeat(count) {
                foodEntryDao.insertFoodEntry(
                    TestHelper.createTestFoodEntry(userId = userId, foodName = foodName),
                )
            }
        }

        val mostConsumed = foodEntryDao.getMostConsumedFoods(userId, 3)
        assertThat(mostConsumed).hasSize(3)

        // Should be ordered by consumption count DESC
        assertThat(mostConsumed[0].foodName).isEqualTo("Apple")
        assertThat(mostConsumed[0].consumptionCount).isEqualTo(3)
        assertThat(mostConsumed[1].foodName).isEqualTo("Banana")
        assertThat(mostConsumed[1].consumptionCount).isEqualTo(2)
        assertThat(mostConsumed[2].foodName).isEqualTo("Orange")
        assertThat(mostConsumed[2].consumptionCount).isEqualTo(1)
    }

    @Test
    fun searchFoodEntries() = runTest {
        val user = TestHelper.createTestUser(email = "search@test.com", username = "searchuser")
        val userId = userDao.insertUser(user)

        // Insert food entries with different names
        val entries = listOf(
            TestHelper.createTestFoodEntry(userId = userId, foodName = "Grilled Chicken Breast"),
            TestHelper.createTestFoodEntry(userId = userId, foodName = "Chicken Salad"),
            TestHelper.createTestFoodEntry(userId = userId, foodName = "Beef Steak"),
            TestHelper.createTestFoodEntry(userId = userId, foodName = "Chicken Soup"),
        )

        entries.forEach { entry ->
            foodEntryDao.insertFoodEntry(entry)
        }

        val chickenEntries = foodEntryDao.searchFoodEntries(userId, "Chicken").first()
        assertThat(chickenEntries).hasSize(3)
        assertThat(chickenEntries.all { it.foodName.contains("Chicken", ignoreCase = true) }).isTrue()

        val grilledEntries = foodEntryDao.searchFoodEntries(userId, "Grilled").first()
        assertThat(grilledEntries).hasSize(1)
        assertThat(grilledEntries[0].foodName).isEqualTo("Grilled Chicken Breast")
    }

    @Test
    fun getCaloriesByMealType() = runTest {
        val user = TestHelper.createTestUser(email = "mealtypecals@test.com", username = "mealtypecalsuser")
        val userId = userDao.insertUser(user)

        val targetDate = Date()

        // Insert entries for different meal types
        foodEntryDao.insertFoodEntry(
            TestHelper.createTestFoodEntry(
                userId = userId,
                foodName = "Breakfast Item",
                caloriesPerServing = 300.0,
                mealType = MealType.BREAKFAST,
            ).copy(dateConsumed = targetDate),
        )

        foodEntryDao.insertFoodEntry(
            TestHelper.createTestFoodEntry(
                userId = userId,
                foodName = "Lunch Item",
                caloriesPerServing = 500.0,
                mealType = MealType.LUNCH,
            ).copy(dateConsumed = targetDate),
        )

        val breakfastCalories = foodEntryDao.getCaloriesByMealType(userId, MealType.BREAKFAST, targetDate)
        assertThat(breakfastCalories).isEqualTo(300.0)

        val lunchCalories = foodEntryDao.getCaloriesByMealType(userId, MealType.LUNCH, targetDate)
        assertThat(lunchCalories).isEqualTo(500.0)

        val dinnerCalories = foodEntryDao.getCaloriesByMealType(userId, MealType.DINNER, targetDate)
        assertThat(dinnerCalories).isEqualTo(0.0) // No dinner entries
    }

    @Test
    fun updateFoodEntry() = runTest {
        val user = TestHelper.createTestUser(email = "update@test.com", username = "updateuser")
        val userId = userDao.insertUser(user)

        val originalEntry = TestHelper.createTestFoodEntry(
            userId = userId,
            foodName = "Original Food",
            caloriesPerServing = 100.0,
        )
        val entryId = foodEntryDao.insertFoodEntry(originalEntry)

        // Update the entry
        val updatedEntry = originalEntry.copy(
            id = entryId,
            foodName = "Updated Food",
            caloriesPerServing = 150.0,
            servingSize = 2.0,
        )
        foodEntryDao.updateFoodEntry(updatedEntry)

        val retrievedEntry = foodEntryDao.getFoodEntryById(entryId)
        assertThat(retrievedEntry?.foodName).isEqualTo("Updated Food")
        assertThat(retrievedEntry?.caloriesPerServing).isEqualTo(150.0)
        assertThat(retrievedEntry?.servingSize).isEqualTo(2.0)
    }

    @Test
    fun deleteFoodEntry() = runTest {
        val user = TestHelper.createTestUser(email = "delete@test.com", username = "deleteuser")
        val userId = userDao.insertUser(user)

        val foodEntry = TestHelper.createTestFoodEntry(userId = userId, foodName = "To Be Deleted")
        val entryId = foodEntryDao.insertFoodEntry(foodEntry)

        // Verify entry exists
        var retrievedEntry = foodEntryDao.getFoodEntryById(entryId)
        assertThat(retrievedEntry).isNotNull()

        // Delete the entry
        foodEntryDao.deleteFoodEntry(foodEntry.copy(id = entryId))

        // Verify entry is deleted
        retrievedEntry = foodEntryDao.getFoodEntryById(entryId)
        assertThat(retrievedEntry).isNull()
    }

    @Test
    fun deleteFoodEntryById() = runTest {
        val user = TestHelper.createTestUser(email = "deletebyid@test.com", username = "deletebyiduser")
        val userId = userDao.insertUser(user)

        val foodEntry = TestHelper.createTestFoodEntry(userId = userId, foodName = "To Be Deleted By ID")
        val entryId = foodEntryDao.insertFoodEntry(foodEntry)

        // Verify entry exists
        var retrievedEntry = foodEntryDao.getFoodEntryById(entryId)
        assertThat(retrievedEntry).isNotNull()

        // Delete by ID
        foodEntryDao.deleteFoodEntryById(entryId)

        // Verify entry is deleted
        retrievedEntry = foodEntryDao.getFoodEntryById(entryId)
        assertThat(retrievedEntry).isNull()
    }

    @Test
    fun insertAllFoodEntries() = runTest {
        val user = TestHelper.createTestUser(email = "insertall@test.com", username = "insertalluser")
        val userId = userDao.insertUser(user)

        val entries = listOf(
            TestHelper.createTestFoodEntry(userId = userId, foodName = "Bulk Entry 1"),
            TestHelper.createTestFoodEntry(userId = userId, foodName = "Bulk Entry 2"),
            TestHelper.createTestFoodEntry(userId = userId, foodName = "Bulk Entry 3"),
        )

        val entryIds = foodEntryDao.insertAll(entries)
        assertThat(entryIds).hasSize(3)
        assertThat(entryIds.all { it > 0 }).isTrue()

        val userEntries = foodEntryDao.getFoodEntriesByUserId(userId).first()
        assertThat(userEntries).hasSize(3)
    }

    @Test
    fun testForeignKeyConstraint() = runTest {
        val foodEntry = TestHelper.createTestFoodEntry(
            userId = 999L, // Non-existent user
            foodName = "Invalid Entry",
        )

        try {
            foodEntryDao.insertFoodEntry(foodEntry)
            Assert.fail("Should throw foreign key constraint exception")
        } catch (e: Exception) {
            // Expected foreign key constraint violation
            assertThat(e.message).containsAnyOf("FOREIGN KEY", "constraint", "no such table")
        }
    }

    @Test
    fun testNutritionalCalculations() = runTest {
        val user = TestHelper.createTestUser(email = "calculations@test.com", username = "calculationsuser")
        val userId = userDao.insertUser(user)

        // Test serving size multiplication
        val foodEntry = TestHelper.createTestFoodEntry(
            userId = userId,
            foodName = "Test Food",
            caloriesPerServing = 100.0,
            servingSize = 2.5,
            proteinGrams = 10.0,
        ).copy(
            carbsGrams = 20.0,
            fatGrams = 5.0,
        )

        val entryId = foodEntryDao.insertFoodEntry(foodEntry)
        val retrievedEntry = foodEntryDao.getFoodEntryById(entryId)

        assertThat(retrievedEntry).isNotNull()

        // Calculate total nutrients (per serving * serving size)
        val totalCalories = retrievedEntry!!.caloriesPerServing * retrievedEntry.servingSize
        val totalProtein = retrievedEntry.proteinGrams * retrievedEntry.servingSize
        val totalCarbs = retrievedEntry.carbsGrams * retrievedEntry.servingSize
        val totalFat = retrievedEntry.fatGrams * retrievedEntry.servingSize

        assertThat(totalCalories).isEqualTo(250.0) // 100 * 2.5
        assertThat(totalProtein).isEqualTo(25.0) // 10 * 2.5
        assertThat(totalCarbs).isEqualTo(50.0) // 20 * 2.5
        assertThat(totalFat).isEqualTo(12.5) // 5 * 2.5
    }

    @Test
    fun testMealTypeDistribution() = runTest {
        val user = TestHelper.createTestUser(email = "distribution@test.com", username = "distributionuser")
        val userId = userDao.insertUser(user)

        val targetDate = Date()

        // Insert entries for all meal types
        MealType.values().forEach { mealType ->
            foodEntryDao.insertFoodEntry(
                TestHelper.createTestFoodEntry(
                    userId = userId,
                    foodName = "${mealType.name} Food",
                    mealType = mealType,
                    caloriesPerServing = 200.0,
                ).copy(dateConsumed = targetDate),
            )
        }

        // Verify each meal type has entries
        MealType.values().forEach { mealType ->
            val entries = foodEntryDao.getFoodEntriesByMealType(userId, mealType).first()
            assertThat(entries).hasSize(1)
            assertThat(entries[0].mealType).isEqualTo(mealType)
        }

        // Check total calories for the day
        val totalCalories = foodEntryDao.getTotalCaloriesByDate(userId, targetDate)
        val expectedTotal = MealType.values().size * 200.0
        assertThat(totalCalories).isEqualTo(expectedTotal)
    }
}
