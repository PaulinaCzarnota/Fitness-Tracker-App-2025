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
 * Comprehensive unit tests for NutritionEntryDao.
 *
 * Tests all nutrition-related database operations including:
 * - CRUD operations for nutrition entries
 * - Meal type filtering and categorization
 * - Advanced nutritional data aggregation and calculation
 * - Date-based queries for nutrition tracking
 * - Micronutrient analysis and health insights
 * - Quality scoring and recommendation algorithms
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class NutritionEntryDaoTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AppDatabase
    private lateinit var nutritionEntryDao: NutritionEntryDao
    private lateinit var userDao: UserDao

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        )
            .allowMainThreadQueries()
            .build()

        nutritionEntryDao = database.nutritionEntryDao()
        userDao = database.userDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndGetNutritionEntry() = runTest {
        // Create user first
        val user = TestHelper.createTestUser(email = "nutrition@test.com", username = "nutritionuser")
        val userId = userDao.insertUser(user)

        // Create and insert nutrition entry
        val nutritionEntry = TestHelper.createTestNutritionEntry(
            userId = userId,
            foodName = "Organic Chicken Breast",
            caloriesPerServing = 165.0,
            servingSize = 100.0,
            mealType = MealType.LUNCH,
            proteinGrams = 31.0,
            vitaminCMg = 5.0,
            calciumMg = 15.0,
            ironMg = 1.0,
            isHomemade = true,
            confidenceLevel = 0.95,
        )

        val nutritionEntryId = nutritionEntryDao.insertNutritionEntry(nutritionEntry)
        assertThat(nutritionEntryId).isGreaterThan(0)

        val retrievedEntry = nutritionEntryDao.getNutritionEntryById(nutritionEntryId)
        assertThat(retrievedEntry).isNotNull()
        assertThat(retrievedEntry?.foodName).isEqualTo("Organic Chicken Breast")
        assertThat(retrievedEntry?.caloriesPerServing).isEqualTo(165.0)
        assertThat(retrievedEntry?.servingSize).isEqualTo(100.0)
        assertThat(retrievedEntry?.mealType).isEqualTo(MealType.LUNCH)
        assertThat(retrievedEntry?.proteinGrams).isEqualTo(31.0)
        assertThat(retrievedEntry?.vitaminCMg).isEqualTo(5.0)
        assertThat(retrievedEntry?.calciumMg).isEqualTo(15.0)
        assertThat(retrievedEntry?.ironMg).isEqualTo(1.0)
        assertThat(retrievedEntry?.isHomemade).isTrue()
        assertThat(retrievedEntry?.confidenceLevel).isEqualTo(0.95)
        assertThat(retrievedEntry?.userId).isEqualTo(userId)
    }

    @Test
    fun getNutritionEntriesByUser() = runTest {
        val user = TestHelper.createTestUser(email = "entries@test.com", username = "entriesuser")
        val userId = userDao.insertUser(user)

        // Insert multiple nutrition entries
        val entries = listOf(
            TestHelper.createTestNutritionEntry(userId = userId, foodName = "Greek Yogurt", mealType = MealType.BREAKFAST),
            TestHelper.createTestNutritionEntry(userId = userId, foodName = "Quinoa Salad", mealType = MealType.LUNCH),
            TestHelper.createTestNutritionEntry(userId = userId, foodName = "Grilled Salmon", mealType = MealType.DINNER),
        )

        entries.forEach { entry ->
            nutritionEntryDao.insertNutritionEntry(entry)
        }

        val userEntries = nutritionEntryDao.getNutritionEntriesByUserId(userId).first()
        assertThat(userEntries).hasSize(3)

        // Should be ordered by date consumed DESC
        val foodNames = userEntries.map { it.foodName }
        assertThat(foodNames).containsExactly("Grilled Salmon", "Quinoa Salad", "Greek Yogurt")
    }

    @Test
    fun getNutritionEntriesByMealType() = runTest {
        val user = TestHelper.createTestUser(email = "mealtype@test.com", username = "mealtypeuser")
        val userId = userDao.insertUser(user)

        // Insert entries for different meal types
        nutritionEntryDao.insertNutritionEntry(
            TestHelper.createTestNutritionEntry(userId = userId, foodName = "Oatmeal", mealType = MealType.BREAKFAST),
        )
        nutritionEntryDao.insertNutritionEntry(
            TestHelper.createTestNutritionEntry(userId = userId, foodName = "Smoothie", mealType = MealType.BREAKFAST),
        )
        nutritionEntryDao.insertNutritionEntry(
            TestHelper.createTestNutritionEntry(userId = userId, foodName = "Turkey Sandwich", mealType = MealType.LUNCH),
        )

        val date = Date()
        val breakfastEntries = nutritionEntryDao.getNutritionEntriesByMealType(userId, MealType.BREAKFAST, date).first()
        assertThat(breakfastEntries).hasSize(2)
        assertThat(breakfastEntries.all { it.mealType == MealType.BREAKFAST }).isTrue()

        val lunchEntries = nutritionEntryDao.getNutritionEntriesByMealType(userId, MealType.LUNCH, date).first()
        assertThat(lunchEntries).hasSize(1)
        assertThat(lunchEntries[0].foodName).isEqualTo("Turkey Sandwich")
    }

    @Test
    fun getNutritionEntriesForDate() = runTest {
        val user = TestHelper.createTestUser(email = "bydate@test.com", username = "bydateuser")
        val userId = userDao.insertUser(user)

        val targetDate = Date()
        Date(targetDate.time - (targetDate.time % (24 * 60 * 60 * 1000)))

        // Insert entries for today
        val todayEntry1 = TestHelper.createTestNutritionEntry(
            userId = userId,
            foodName = "Morning Coffee",
        ).copy(dateConsumed = targetDate)

        val todayEntry2 = TestHelper.createTestNutritionEntry(
            userId = userId,
            foodName = "Lunch Salad",
        ).copy(dateConsumed = targetDate)

        // Insert entry for yesterday
        val yesterday = Date(targetDate.time - (24 * 60 * 60 * 1000))
        val yesterdayEntry = TestHelper.createTestNutritionEntry(
            userId = userId,
            foodName = "Yesterday's Dinner",
        ).copy(dateConsumed = yesterday)

        nutritionEntryDao.insertNutritionEntry(todayEntry1)
        nutritionEntryDao.insertNutritionEntry(todayEntry2)
        nutritionEntryDao.insertNutritionEntry(yesterdayEntry)

        val todayEntries = nutritionEntryDao.getNutritionEntriesForDate(userId, targetDate).first()
        assertThat(todayEntries).hasSize(2)
        assertThat(todayEntries.map { it.foodName }).containsExactly("Lunch Salad", "Morning Coffee")
    }

    @Test
    fun getNutritionEntriesForDateRange() = runTest {
        val user = TestHelper.createTestUser(email = "daterange@test.com", username = "daterangeuser")
        val userId = userDao.insertUser(user)

        val currentTime = System.currentTimeMillis()
        val day1 = Date(currentTime - (2 * 24 * 60 * 60 * 1000)) // 2 days ago
        val day2 = Date(currentTime - (1 * 24 * 60 * 60 * 1000)) // 1 day ago
        val day3 = Date(currentTime) // today
        val day4 = Date(currentTime + (1 * 24 * 60 * 60 * 1000)) // tomorrow

        // Insert entries for different dates
        nutritionEntryDao.insertNutritionEntry(
            TestHelper.createTestNutritionEntry(userId = userId, foodName = "Day 1 Food").copy(dateConsumed = day1),
        )
        nutritionEntryDao.insertNutritionEntry(
            TestHelper.createTestNutritionEntry(userId = userId, foodName = "Day 2 Food").copy(dateConsumed = day2),
        )
        nutritionEntryDao.insertNutritionEntry(
            TestHelper.createTestNutritionEntry(userId = userId, foodName = "Day 3 Food").copy(dateConsumed = day3),
        )
        nutritionEntryDao.insertNutritionEntry(
            TestHelper.createTestNutritionEntry(userId = userId, foodName = "Day 4 Food").copy(dateConsumed = day4),
        )

        // Query for entries from day2 to day3 (inclusive)
        val entriesInRange = nutritionEntryDao.getNutritionEntriesForDateRange(userId, day2, day3).first()
        assertThat(entriesInRange).hasSize(2)
        assertThat(entriesInRange.map { it.foodName }).containsExactly("Day 3 Food", "Day 2 Food")
    }

    @Test
    fun getTotalCaloriesForDate() = runTest {
        val user = TestHelper.createTestUser(email = "calories@test.com", username = "caloriesuser")
        val userId = userDao.insertUser(user)

        val targetDate = Date()

        // Insert nutrition entries with different calories for today
        val entries = listOf(
            TestHelper.createTestNutritionEntry(
                userId = userId,
                foodName = "Breakfast",
                caloriesPerServing = 300.0,
                servingSize = 1.0,
            ).copy(dateConsumed = targetDate),

            TestHelper.createTestNutritionEntry(
                userId = userId,
                foodName = "Lunch",
                caloriesPerServing = 500.0,
                servingSize = 1.0,
            ).copy(dateConsumed = targetDate),

            TestHelper.createTestNutritionEntry(
                userId = userId,
                foodName = "Dinner",
                caloriesPerServing = 400.0,
                servingSize = 1.5,
            ).copy(dateConsumed = targetDate),
        )

        entries.forEach { entry ->
            nutritionEntryDao.insertNutritionEntry(entry)
        }

        val totalCalories = nutritionEntryDao.getTotalCaloriesForDate(userId, targetDate)
        // 300 + 500 + (400 * 1.5) = 1400
        assertThat(totalCalories).isEqualTo(1400.0)
    }

    @Test
    fun getNutritionSummaryForDate() = runTest {
        val user = TestHelper.createTestUser(email = "summary@test.com", username = "summaryuser")
        val userId = userDao.insertUser(user)

        val targetDate = Date()

        // Insert nutrition entries with comprehensive nutritional values
        val entries = listOf(
            TestHelper.createTestNutritionEntry(
                userId = userId,
                foodName = "Chicken Breast",
                caloriesPerServing = 165.0,
                servingSize = 1.0,
                proteinGrams = 31.0,
                carbsGrams = 0.0,
                fatGrams = 3.6,
                saturatedFatGrams = 1.0,
                fiberGrams = 0.0,
                sugarGrams = 0.0,
                sodiumMg = 74.0,
                vitaminCMg = 5.0,
                calciumMg = 15.0,
                ironMg = 1.0,
            ).copy(dateConsumed = targetDate),

            TestHelper.createTestNutritionEntry(
                userId = userId,
                foodName = "Brown Rice",
                caloriesPerServing = 216.0,
                servingSize = 1.0,
                proteinGrams = 5.0,
                carbsGrams = 45.0,
                fatGrams = 1.8,
                saturatedFatGrams = 0.4,
                fiberGrams = 3.5,
                sugarGrams = 0.7,
                sodiumMg = 10.0,
                vitaminCMg = 0.0,
                calciumMg = 23.0,
                ironMg = 2.2,
            ).copy(dateConsumed = targetDate),
        )

        entries.forEach { entry ->
            nutritionEntryDao.insertNutritionEntry(entry)
        }

        val nutritionSummary = nutritionEntryDao.getNutritionSummaryForDate(userId, targetDate)
        assertThat(nutritionSummary).isNotNull()
        assertThat(nutritionSummary?.totalCalories).isEqualTo(381.0) // 165 + 216
        assertThat(nutritionSummary?.totalProtein).isEqualTo(36.0) // 31 + 5
        assertThat(nutritionSummary?.totalCarbs).isEqualTo(45.0) // 0 + 45
        assertThat(nutritionSummary?.totalFat).isEqualTo(5.4) // 3.6 + 1.8
        assertThat(nutritionSummary?.totalSaturatedFat).isEqualTo(1.4) // 1.0 + 0.4
        assertThat(nutritionSummary?.totalFiber).isEqualTo(3.5) // 0 + 3.5
        assertThat(nutritionSummary?.totalSodium).isEqualTo(84.0) // 74 + 10
        assertThat(nutritionSummary?.totalVitaminC).isEqualTo(5.0) // 5 + 0
        assertThat(nutritionSummary?.totalCalcium).isEqualTo(38.0) // 15 + 23
        assertThat(nutritionSummary?.totalIron).isEqualTo(3.2) // 1.0 + 2.2
    }

    @Test
    fun getMostConsumedFoods() = runTest {
        val user = TestHelper.createTestUser(email = "mostconsumed@test.com", username = "mostconsumeduser")
        val userId = userDao.insertUser(user)

        // Insert multiple entries for the same foods
        val foods = mapOf(
            "Avocado" to 4, // Most consumed
            "Banana" to 3,
            "Apple" to 2,
            "Orange" to 1,
        )

        foods.forEach { (foodName, count) ->
            repeat(count) {
                nutritionEntryDao.insertNutritionEntry(
                    TestHelper.createTestNutritionEntry(userId = userId, foodName = foodName),
                )
            }
        }

        val mostConsumed = nutritionEntryDao.getMostConsumedFoods(userId, 4)
        assertThat(mostConsumed).hasSize(4)

        // Should be ordered by consumption count DESC
        assertThat(mostConsumed[0].foodName).isEqualTo("Avocado")
        assertThat(mostConsumed[0].frequency).isEqualTo(4)
        assertThat(mostConsumed[1].foodName).isEqualTo("Banana")
        assertThat(mostConsumed[1].frequency).isEqualTo(3)
        assertThat(mostConsumed[2].foodName).isEqualTo("Apple")
        assertThat(mostConsumed[2].frequency).isEqualTo(2)
        assertThat(mostConsumed[3].foodName).isEqualTo("Orange")
        assertThat(mostConsumed[3].frequency).isEqualTo(1)
    }

    @Test
    fun searchNutritionEntries() = runTest {
        val user = TestHelper.createTestUser(email = "search@test.com", username = "searchuser")
        val userId = userDao.insertUser(user)

        // Insert nutrition entries with different names
        val entries = listOf(
            TestHelper.createTestNutritionEntry(userId = userId, foodName = "Grilled Chicken Breast"),
            TestHelper.createTestNutritionEntry(userId = userId, foodName = "Chicken Caesar Salad"),
            TestHelper.createTestNutritionEntry(userId = userId, foodName = "Grass Fed Beef Steak"),
            TestHelper.createTestNutritionEntry(userId = userId, foodName = "Chicken Vegetable Soup"),
        )

        entries.forEach { entry ->
            nutritionEntryDao.insertNutritionEntry(entry)
        }

        val chickenEntries = nutritionEntryDao.searchNutritionEntries(userId, "Chicken").first()
        assertThat(chickenEntries).hasSize(3)
        assertThat(chickenEntries.all { it.foodName.contains("Chicken", ignoreCase = true) }).isTrue()

        val grilledEntries = nutritionEntryDao.searchNutritionEntries(userId, "Grilled").first()
        assertThat(grilledEntries).hasSize(1)
        assertThat(grilledEntries[0].foodName).isEqualTo("Grilled Chicken Breast")
    }

    @Test
    fun getHighestCalorieFoods() = runTest {
        val user = TestHelper.createTestUser(email = "highcal@test.com", username = "highcaluser")
        val userId = userDao.insertUser(user)

        // Insert foods with different calorie values
        val foods = listOf(
            TestHelper.createTestNutritionEntry(userId = userId, foodName = "Nuts", caloriesPerServing = 600.0),
            TestHelper.createTestNutritionEntry(userId = userId, foodName = "Avocado", caloriesPerServing = 320.0),
            TestHelper.createTestNutritionEntry(userId = userId, foodName = "Banana", caloriesPerServing = 105.0),
            TestHelper.createTestNutritionEntry(userId = userId, foodName = "Apple", caloriesPerServing = 80.0),
        )

        foods.forEach { food ->
            nutritionEntryDao.insertNutritionEntry(food)
        }

        val highestCaloriesFoods = nutritionEntryDao.getHighestCalorieFoods(userId, 3)
        assertThat(highestCaloriesFoods).hasSize(3)

        // Should be ordered by max calories DESC
        assertThat(highestCaloriesFoods[0].foodName).isEqualTo("Nuts")
        assertThat(highestCaloriesFoods[1].foodName).isEqualTo("Avocado")
        assertThat(highestCaloriesFoods[2].foodName).isEqualTo("Banana")
    }

    @Test
    fun getBestQualityFoods() = runTest {
        val user = TestHelper.createTestUser(email = "quality@test.com", username = "qualityuser")
        val userId = userDao.insertUser(user)

        // Insert foods with different confidence levels
        val foods = listOf(
            TestHelper.createTestNutritionEntry(
                userId = userId,
                foodName = "Organic Spinach",
                confidenceLevel = 0.98,
                isHomemade = true,
            ),
            TestHelper.createTestNutritionEntry(
                userId = userId,
                foodName = "Fresh Salmon",
                confidenceLevel = 0.95,
            ),
            TestHelper.createTestNutritionEntry(
                userId = userId,
                foodName = "Processed Snack",
                confidenceLevel = 0.6,
            ),
        )

        foods.forEach { food ->
            nutritionEntryDao.insertNutritionEntry(food)
        }

        val bestQualityFoods = nutritionEntryDao.getBestQualityFoods(userId, 2)
        assertThat(bestQualityFoods).hasSize(2)

        // Should be ordered by quality/confidence DESC
        assertThat(bestQualityFoods[0].foodName).isEqualTo("Organic Spinach")
        assertThat(bestQualityFoods[1].foodName).isEqualTo("Fresh Salmon")
    }

    @Test
    fun getHomemadeNutritionEntries() = runTest {
        val user = TestHelper.createTestUser(email = "homemade@test.com", username = "homemadeuser")
        val userId = userDao.insertUser(user)

        // Insert mix of homemade and store-bought foods
        val foods = listOf(
            TestHelper.createTestNutritionEntry(
                userId = userId,
                foodName = "Homemade Soup",
                isHomemade = true,
            ),
            TestHelper.createTestNutritionEntry(
                userId = userId,
                foodName = "Store Bought Bread",
                isHomemade = false,
            ),
            TestHelper.createTestNutritionEntry(
                userId = userId,
                foodName = "Garden Salad",
                isHomemade = true,
            ),
        )

        foods.forEach { food ->
            nutritionEntryDao.insertNutritionEntry(food)
        }

        val homemadeEntries = nutritionEntryDao.getHomemadeNutritionEntries(userId).first()
        assertThat(homemadeEntries).hasSize(2)
        assertThat(homemadeEntries.all { it.isHomemade }).isTrue()
        assertThat(homemadeEntries.map { it.foodName }).containsExactly("Garden Salad", "Homemade Soup")
    }

    @Test
    fun getVitaminCIntakeForDateRange() = runTest {
        val user = TestHelper.createTestUser(email = "vitaminc@test.com", username = "vitamincuser")
        val userId = userDao.insertUser(user)

        val startDate = Date(System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000))
        val endDate = Date()

        // Insert entries with vitamin C
        val foods = listOf(
            TestHelper.createTestNutritionEntry(
                userId = userId,
                foodName = "Orange",
                vitaminCMg = 70.0,
                servingSize = 1.0,
            ).copy(dateConsumed = startDate),
            TestHelper.createTestNutritionEntry(
                userId = userId,
                foodName = "Strawberries",
                vitaminCMg = 85.0,
                servingSize = 1.0,
            ).copy(dateConsumed = endDate),
        )

        foods.forEach { food ->
            nutritionEntryDao.insertNutritionEntry(food)
        }

        val totalVitaminC = nutritionEntryDao.getTotalVitaminCForDateRange(userId, startDate, endDate)
        assertThat(totalVitaminC).isEqualTo(155.0) // 70 + 85
    }

    @Test
    fun getNutritionEntriesWithQualityIssues() = runTest {
        val user = TestHelper.createTestUser(email = "quality@test.com", username = "qualityuser")
        val userId = userDao.insertUser(user)

        // Insert foods with quality issues (high sodium, low fiber)
        val foods = listOf(
            TestHelper.createTestNutritionEntry(
                userId = userId,
                foodName = "High Sodium Food",
                sodiumMg = 800.0, // Above default threshold of 600mg
                fiberGrams = 0.5, // Below default threshold of 1g
            ),
            TestHelper.createTestNutritionEntry(
                userId = userId,
                foodName = "Healthy Food",
                sodiumMg = 50.0,
                fiberGrams = 5.0,
            ),
            TestHelper.createTestNutritionEntry(
                userId = userId,
                foodName = "Low Fiber Food",
                sodiumMg = 200.0,
                fiberGrams = 0.2, // Below threshold
            ),
        )

        foods.forEach { food ->
            nutritionEntryDao.insertNutritionEntry(food)
        }

        val qualityIssueEntries = nutritionEntryDao.getNutritionEntriesWithQualityIssues(userId).first()
        assertThat(qualityIssueEntries).hasSize(2)
        assertThat(qualityIssueEntries.map { it.foodName }).containsExactly("Low Fiber Food", "High Sodium Food")
    }

    @Test
    fun updateNutritionEntry() = runTest {
        val user = TestHelper.createTestUser(email = "update@test.com", username = "updateuser")
        val userId = userDao.insertUser(user)

        val originalEntry = TestHelper.createTestNutritionEntry(
            userId = userId,
            foodName = "Original Food",
            caloriesPerServing = 100.0,
            confidenceLevel = 0.7,
        )
        val entryId = nutritionEntryDao.insertNutritionEntry(originalEntry)

        // Update the entry
        val updatedEntry = originalEntry.copy(
            id = entryId,
            foodName = "Updated Food",
            caloriesPerServing = 150.0,
            servingSize = 2.0,
            confidenceLevel = 0.9,
            vitaminCMg = 25.0,
        )
        nutritionEntryDao.updateNutritionEntry(updatedEntry)

        val retrievedEntry = nutritionEntryDao.getNutritionEntryById(entryId)
        assertThat(retrievedEntry?.foodName).isEqualTo("Updated Food")
        assertThat(retrievedEntry?.caloriesPerServing).isEqualTo(150.0)
        assertThat(retrievedEntry?.servingSize).isEqualTo(2.0)
        assertThat(retrievedEntry?.confidenceLevel).isEqualTo(0.9)
        assertThat(retrievedEntry?.vitaminCMg).isEqualTo(25.0)
    }

    @Test
    fun deleteNutritionEntry() = runTest {
        val user = TestHelper.createTestUser(email = "delete@test.com", username = "deleteuser")
        val userId = userDao.insertUser(user)

        val nutritionEntry = TestHelper.createTestNutritionEntry(userId = userId, foodName = "To Be Deleted")
        val entryId = nutritionEntryDao.insertNutritionEntry(nutritionEntry)

        // Verify entry exists
        var retrievedEntry = nutritionEntryDao.getNutritionEntryById(entryId)
        assertThat(retrievedEntry).isNotNull()

        // Delete the entry
        nutritionEntryDao.deleteNutritionEntry(nutritionEntry.copy(id = entryId))

        // Verify entry is deleted
        retrievedEntry = nutritionEntryDao.getNutritionEntryById(entryId)
        assertThat(retrievedEntry).isNull()
    }

    @Test
    fun deleteNutritionEntryById() = runTest {
        val user = TestHelper.createTestUser(email = "deletebyid@test.com", username = "deletebyiduser")
        val userId = userDao.insertUser(user)

        val nutritionEntry = TestHelper.createTestNutritionEntry(userId = userId, foodName = "To Be Deleted By ID")
        val entryId = nutritionEntryDao.insertNutritionEntry(nutritionEntry)

        // Verify entry exists
        var retrievedEntry = nutritionEntryDao.getNutritionEntryById(entryId)
        assertThat(retrievedEntry).isNotNull()

        // Delete by ID
        nutritionEntryDao.deleteNutritionEntryById(entryId)

        // Verify entry is deleted
        retrievedEntry = nutritionEntryDao.getNutritionEntryById(entryId)
        assertThat(retrievedEntry).isNull()
    }

    @Test
    fun insertAllNutritionEntries() = runTest {
        val user = TestHelper.createTestUser(email = "insertall@test.com", username = "insertalluser")
        val userId = userDao.insertUser(user)

        val entries = listOf(
            TestHelper.createTestNutritionEntry(userId = userId, foodName = "Bulk Entry 1"),
            TestHelper.createTestNutritionEntry(userId = userId, foodName = "Bulk Entry 2"),
            TestHelper.createTestNutritionEntry(userId = userId, foodName = "Bulk Entry 3"),
        )

        val entryIds = nutritionEntryDao.insertAll(entries)
        assertThat(entryIds).hasSize(3)
        assertThat(entryIds.all { it > 0 }).isTrue()

        val userEntries = nutritionEntryDao.getNutritionEntriesByUserId(userId).first()
        assertThat(userEntries).hasSize(3)
    }

    @Test
    fun testNutritionalCalculations() = runTest {
        val user = TestHelper.createTestUser(email = "calculations@test.com", username = "calculationsuser")
        val userId = userDao.insertUser(user)

        // Test serving size multiplication with comprehensive nutrients
        val nutritionEntry = TestHelper.createTestNutritionEntry(
            userId = userId,
            foodName = "Test Food",
            caloriesPerServing = 100.0,
            servingSize = 2.5,
            proteinGrams = 10.0,
            carbsGrams = 20.0,
            fatGrams = 5.0,
            saturatedFatGrams = 1.0,
            fiberGrams = 3.0,
            sugarGrams = 8.0,
            sodiumMg = 150.0,
            vitaminCMg = 30.0,
            calciumMg = 100.0,
            ironMg = 2.0,
        )

        val entryId = nutritionEntryDao.insertNutritionEntry(nutritionEntry)
        val retrievedEntry = nutritionEntryDao.getNutritionEntryById(entryId)

        assertThat(retrievedEntry).isNotNull()

        // Calculate total nutrients (per serving * serving size)
        val totalCalories = retrievedEntry!!.getTotalCalories()
        val totalProtein = retrievedEntry.getTotalProtein()
        val totalCarbs = retrievedEntry.getTotalCarbs()
        val totalFat = retrievedEntry.getTotalFat()
        val totalSaturatedFat = retrievedEntry.getTotalSaturatedFat()
        val totalFiber = retrievedEntry.getTotalFiber()
        val totalSugar = retrievedEntry.getTotalSugar()
        val totalSodium = retrievedEntry.getTotalSodium()
        val totalVitaminC = retrievedEntry.getTotalVitaminC()
        val totalCalcium = retrievedEntry.getTotalCalcium()
        val totalIron = retrievedEntry.getTotalIron()

        assertThat(totalCalories).isEqualTo(250.0) // 100 * 2.5
        assertThat(totalProtein).isEqualTo(25.0) // 10 * 2.5
        assertThat(totalCarbs).isEqualTo(50.0) // 20 * 2.5
        assertThat(totalFat).isEqualTo(12.5) // 5 * 2.5
        assertThat(totalSaturatedFat).isEqualTo(2.5) // 1 * 2.5
        assertThat(totalFiber).isEqualTo(7.5) // 3 * 2.5
        assertThat(totalSugar).isEqualTo(20.0) // 8 * 2.5
        assertThat(totalSodium).isEqualTo(375.0) // 150 * 2.5
        assertThat(totalVitaminC).isEqualTo(75.0) // 30 * 2.5
        assertThat(totalCalcium).isEqualTo(250.0) // 100 * 2.5
        assertThat(totalIron).isEqualTo(5.0) // 2 * 2.5
    }

    @Test
    fun testNutritionQualityScore() = runTest {
        val user = TestHelper.createTestUser(email = "quality@test.com", username = "qualityuser")
        val userId = userDao.insertUser(user)

        // Test high quality food
        val highQualityEntry = TestHelper.createTestNutritionEntry(
            userId = userId,
            foodName = "High Quality Food",
            caloriesPerServing = 200.0,
            servingSize = 1.0,
            proteinGrams = 30.0, // High protein
            carbsGrams = 25.0, // Moderate carbs
            fatGrams = 8.0, // Moderate fat
            saturatedFatGrams = 2.0, // Low saturated fat
            fiberGrams = 8.0, // High fiber
            sugarGrams = 5.0, // Low sugar
            addedSugarsGrams = 0.0, // No added sugars
            sodiumMg = 100.0, // Low sodium
            vitaminCMg = 50.0, // High vitamin C
            calciumMg = 150.0, // Good calcium
            ironMg = 5.0, // Good iron
            confidenceLevel = 0.95,
        )

        val entryId = nutritionEntryDao.insertNutritionEntry(highQualityEntry)
        val retrievedEntry = nutritionEntryDao.getNutritionEntryById(entryId)!!

        val qualityScore = retrievedEntry.getNutritionQualityScore()
        assertThat(qualityScore).isAtLeast(7.0) // Should be high quality

        val qualityRating = retrievedEntry.getNutritionalDensityRating()
        assertThat(qualityRating).isAnyOf("Good", "Excellent")

        // Test the entry is considered high quality food
        assertThat(retrievedEntry.isHighProtein()).isTrue()
        assertThat(retrievedEntry.isHighFiber()).isTrue()
        assertThat(retrievedEntry.isLowSodium()).isTrue()
        assertThat(retrievedEntry.isWholeFood()).isTrue()
    }

    @Test
    fun testForeignKeyConstraint() = runTest {
        val nutritionEntry = TestHelper.createTestNutritionEntry(
            userId = 999L, // Non-existent user
            foodName = "Invalid Entry",
        )

        try {
            nutritionEntryDao.insertNutritionEntry(nutritionEntry)
            Assert.fail("Should throw foreign key constraint exception")
        } catch (e: Exception) {
            // Expected foreign key constraint violation
            assertThat(e.message).containsAnyOf("FOREIGN KEY", "constraint", "no such table")
        }
    }

    @Test
    fun testMealTypeDistribution() = runTest {
        val user = TestHelper.createTestUser(email = "distribution@test.com", username = "distributionuser")
        val userId = userDao.insertUser(user)

        val targetDate = Date()

        // Insert entries for all meal types
        MealType.entries.forEach { mealType ->
            nutritionEntryDao.insertNutritionEntry(
                TestHelper.createTestNutritionEntry(
                    userId = userId,
                    foodName = "${mealType.name} Food",
                    mealType = mealType,
                    caloriesPerServing = 200.0,
                ).copy(dateConsumed = targetDate),
            )
        }

        // Verify each meal type has entries
        MealType.entries.forEach { mealType ->
            val entries = nutritionEntryDao.getNutritionEntriesByMealType(userId, mealType, targetDate).first()
            assertThat(entries).hasSize(1)
            assertThat(entries[0].mealType).isEqualTo(mealType)
        }

        // Check total calories for the day
        val totalCalories = nutritionEntryDao.getTotalCaloriesForDate(userId, targetDate)
        val expectedTotal = MealType.entries.size * 200.0
        assertThat(totalCalories).isEqualTo(expectedTotal)
    }
}
