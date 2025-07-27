package com.example.fitnesstrackerapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstrackerapp.data.Goal
import com.example.fitnesstrackerapp.data.GoalDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * GoalViewModel
 *
 * ViewModel that manages the UI-related data for goal tracking.
 * Interacts with the GoalDao to read/write data to the Room database.
 * Exposes goal data using StateFlow for Jetpack Compose UI reactivity.
 */
class GoalViewModel(
    private val goalDao: GoalDao
) : ViewModel() {

    /**
     * A reactive StateFlow of all goals from the database.
     * Observed in the UI using collectAsStateWithLifecycle() or collectAsState().
     */
    val allGoals: StateFlow<List<Goal>> = goalDao.getAllGoals()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Inserts a new goal into the database.
     *
     * @param goal The Goal object to be added.
     */
    fun addGoal(goal: Goal) {
        viewModelScope.launch(Dispatchers.IO) {
            goalDao.insertGoal(goal)
        }
    }

    /**
     * Updates an existing goal entry.
     * Typically used when the user modifies goal target, name, or progress.
     *
     * @param goal The updated Goal object.
     */
    fun updateGoal(goal: Goal) {
        viewModelScope.launch(Dispatchers.IO) {
            goalDao.updateGoal(goal)
        }
    }

    /**
     * Deletes a specific goal from the database.
     * Called when the user manually removes a goal entry.
     *
     * @param goal The Goal to delete.
     */
    fun deleteGoal(goal: Goal) {
        viewModelScope.launch(Dispatchers.IO) {
            goalDao.deleteGoal(goal)
        }
    }

    /**
     * Clears all goal entries from the database.
     * Useful for "Reset" features or developer testing.
     */
    fun clearAllGoals() {
        viewModelScope.launch(Dispatchers.IO) {
            goalDao.clearAll()
        }
    }
}
