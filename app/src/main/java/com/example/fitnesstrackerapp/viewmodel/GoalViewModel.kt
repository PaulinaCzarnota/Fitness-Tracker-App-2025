package com.example.fitnesstrackerapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstrackerapp.data.Goal
import com.example.fitnesstrackerapp.data.GoalDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel for managing goal-related operations and data between the UI and Room database.
 * Provides lifecycle-aware access to goal data.
 */
class GoalViewModel(private val goalDao: GoalDao) : ViewModel() {

    // LiveData containing the list of all goals in the database
    val allGoals: LiveData<List<Goal>> = goalDao.getAllGoals()

    /**
     * Insert a new goal asynchronously using the IO dispatcher.
     */
    fun addGoal(goal: Goal) {
        viewModelScope.launch(Dispatchers.IO) {
            goalDao.insertGoal(goal)
        }
    }

    /**
     * Update an existing goal in the database.
     */
    fun updateGoal(goal: Goal) {
        viewModelScope.launch(Dispatchers.IO) {
            goalDao.updateGoal(goal)
        }
    }

    /**
     * Delete a specific goal from the database.
     */
    fun deleteGoal(goal: Goal) {
        viewModelScope.launch(Dispatchers.IO) {
            goalDao.deleteGoal(goal)
        }
    }

    /**
     * Clear all goals — useful for reset or testing.
     */
    fun clearAllGoals() {
        viewModelScope.launch(Dispatchers.IO) {
            goalDao.clearAll()
        }
    }
}
