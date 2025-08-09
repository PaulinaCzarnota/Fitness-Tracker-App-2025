package com.example.fitnesstrackerapp.data.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fitnesstrackerapp.data.database.AppDatabase
import com.example.fitnesstrackerapp.data.entity.*
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.runner.RunWith
import java.io.IOException
import java.util.*

/**
 * Simple smoke test to verify DAO mappings work correctly.
 * This tests the core database operations without the complex UI compilation issues.
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class DaoSmokeTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AppDatabase
    private lateinit var foodEntryDao: FoodEntryDao
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

        foodEntryDao = database.foodEntryDao()
        nutritionEntryDao = database.nutritionEntryDao()
        userDao = database.userDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertFoodEntryAndGetMacros() = runTest {
        // Create a test user
        val user = User(
            email = "test@example.com",
            username = "testuser",
            firstName = "Test",
            lastName = "User",
            dateOfBirth = Date(),
            gender = Gender.OTHER,
            heightCm = 175.0,
            weightKg = 70.0,
            activityLevel = ActivityLevel.MODERATELY_ACTIVE,
            createdAt = Date(),
            updatedAt = Date(),
            isActive = true
        )
        val userId = userDao.insertUser(user)

        // Create a test food entry
        val foodEntry = FoodEntry(
            userId = userId,
            foodName = "Test Chicken",
            caloriesPerServing = 200.0,
            servingSize = 1.0,
            proteinGrams = 25.0,
            carbsGrams = 5.0,
            fatGrams = 8.0,
            fiberGrams = 2.0,
            sugarGrams = 1.0,
            sodiumMg = 300.0,
            mealType = MealType.LUNCH,
            dateConsumed = Date(),
            servingUnit = "piece"
        )

        // Insert the food entry
        val entryId = foodEntryDao.insertFoodEntry(foodEntry)
        assertThat(entryId).isGreaterThan(0)

        // Test the macro totals query - this is what was failing before our fix
        val macros = foodEntryDao.getTotalMacrosForDate(userId, Date())
        assertThat(macros).isNotNull()
        assertThat(macros?.totalProtein).isEqualTo(25.0)
        assertThat(macros?.totalCarbs).isEqualTo(5.0)
        assertThat(macros?.totalFat).isEqualTo(8.0)

        // Test nutrition summary query
        val nutritionSummary = foodEntryDao.getNutritionSummaryForDateRange(userId, Date(), Date())
        assertThat(nutritionSummary).isNotNull()
        assertThat(nutritionSummary?.totalCalories).isEqualTo(200.0)
        assertThat(nutritionSummary?.totalProtein).isEqualTo(25.0)
        assertThat(nutritionSummary?.totalCarbs).isEqualTo(5.0)
        assertThat(nutritionSummary?.totalFat).isEqualTo(8.0)
    }

    @Test
    fun insertNutritionEntryAndGetSummary() = runTest {
        // Create a test user
        val user = User(
            email = "test2@example.com",
            username = "testuser2",
            firstName = "Test",
            lastName = "User",
            dateOfBirth = Date(),
            gender = Gender.OTHER,
            heightCm = 175.0,
            weightKg = 70.0,
            activityLevel = ActivityLevel.MODERATELY_ACTIVE,
            createdAt = Date(),
            updatedAt = Date(),
            isActive = true
        )
        val userId = userDao.insertUser(user)

        // Create a test nutrition entry
        val nutritionEntry = NutritionEntry(
            userId = userId,
            foodName = "Test Food",
            brandName = "Test Brand",
            servingSize = 1.0,
            servingUnit = "cup",
            caloriesPerServing = 150.0,
            proteinGrams = 20.0,
            carbsGrams = 10.0,
            fatGrams = 5.0,
            saturatedFatGrams = 2.0,
            transFatGrams = 0.0,
            cholesterolMg = 50.0,
            fiberGrams = 3.0,
            sugarGrams = 8.0,
            addedSugarsGrams = 2.0,
            sodiumMg = 200.0,
            potassiumMg = 400.0,
            vitaminCMg = 10.0,
            vitaminDMcg = 2.0,
            calciumMg = 100.0,
            ironMg = 3.0,
            mealType = MealType.BREAKFAST,
            dateConsumed = Date(),
            isHomemade = false,
            confidenceLevel = 0.9
        )

        // Insert the nutrition entry
        val entryId = nutritionEntryDao.insertNutritionEntry(nutritionEntry)
        assertThat(entryId).isGreaterThan(0)

        // Test the macro totals query
        val macros = nutritionEntryDao.getTotalMacrosForDate(userId, Date())
        assertThat(macros).isNotNull()
        assertThat(macros?.totalProtein).isEqualTo(20.0)
        assertThat(macros?.totalCarbs).isEqualTo(10.0)
        assertThat(macros?.totalFat).isEqualTo(5.0)

        // Test comprehensive nutrition summary query
        val nutritionSummary = nutritionEntryDao.getNutritionSummaryForDate(userId, Date())
        assertThat(nutritionSummary).isNotNull()
        assertThat(nutritionSummary?.totalCalories).isEqualTo(150.0)
        assertThat(nutritionSummary?.totalProtein).isEqualTo(20.0)
        assertThat(nutritionSummary?.totalCarbs).isEqualTo(10.0)
        assertThat(nutritionSummary?.totalFat).isEqualTo(5.0)
        assertThat(nutritionSummary?.totalSodium).isEqualTo(200.0)
        assertThat(nutritionSummary?.totalVitaminC).isEqualTo(10.0)
        assertThat(nutritionSummary?.totalCalcium).isEqualTo(100.0)
        assertThat(nutritionSummary?.totalIron).isEqualTo(3.0)
    }
}
