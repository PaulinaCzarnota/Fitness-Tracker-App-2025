package com.example.fitnesstrackerapp.fake

import com.example.fitnesstrackerapp.data.model.NutritionSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.Date

/**
 * Fake implementation of NutritionRepository for testing purposes.
 */
class FakeNutritionRepository {
    private var nutritionSummary: NutritionSummary? = null
    private var errorMessage: String? = null

    fun setNutritionSummary(summary: NutritionSummary) {
        nutritionSummary = summary
        errorMessage = null
    }

    fun setError(error: String) {
        errorMessage = error
        nutritionSummary = null
    }

    fun clearError() {
        errorMessage = null
    }

    fun getNutritionSummary(userId: Long, date: Date): Flow<NutritionSummary?> {
        return if (errorMessage != null) {
            throw RuntimeException(errorMessage)
        } else {
            flowOf(nutritionSummary)
        }
    }
}
