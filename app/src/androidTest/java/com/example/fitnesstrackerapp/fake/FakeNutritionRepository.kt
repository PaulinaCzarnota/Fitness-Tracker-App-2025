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

    /**
     * Sets the nutrition summary to be returned by the repository.
     */
    fun setNutritionSummary(summary: NutritionSummary) {
        nutritionSummary = summary
        errorMessage = null
    }

    /**
     * Sets an error message to simulate a failure. Any call to getNutritionSummary will throw this error.
     */
    fun setError(error: String) {
        errorMessage = error
        nutritionSummary = null
    }

    /**
     * Clears any error state, so getNutritionSummary will return the summary again.
     */
    fun clearError() {
        errorMessage = null
    }

    /**
     * Returns the nutrition summary for the given user and date.
     * If an error is set, this will throw an exception to simulate a failure.
     * Note: Throws immediately, not inside a flow builder, for test simplicity.
     */
    fun getNutritionSummary(userId: Long, date: Date): Flow<NutritionSummary?> {
        return if (errorMessage != null) {
            throw RuntimeException(errorMessage)
        } else {
            flowOf(nutritionSummary)
        }
    }
}
