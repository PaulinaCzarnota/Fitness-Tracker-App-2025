package com.example.fitnesstrackerapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstrackerapp.data.Goal
import com.example.fitnesstrackerapp.data.GoalDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * GoalViewModel.kt
 *
 * ViewModel for handling all goal-related data operations.
 * It separates the UI from the data layer and ensures lifecycle-aware interaction
 * with the Room database through GoalDao.
 *
 * @param goalDao DAO for accessing goal-related database operations.
 */
class GoalViewModel(private val goalDao: GoalDao) : ViewModel() {

    /**
     * LiveData list of all goals in the database.
     * Observed by the UI to automatically reflect data changes.
     */
    val allGoals: LiveData<List<Goal>> = goalDao.getAllGoals()

    /**
     * Inserts a new goal into the database.
     * Runs in a background thread using the IO dispatcher.
     *
     * @param goal The goal to insert.
     */
    fun addGoal(goal: Goal) {
        viewModelScope.launch(Dispatchers.IO) {
            goalDao.insertGoal(goal)
        }
    }

    /**
     * Updates an existing goal.
     * Typically called when progress changes or the goal is achieved.
     *
     * @param goal The goal object with updated values.
     */
    fun updateGoal(goal: Goal) {
        viewModelScope.launch(Dispatchers.IO) {
            goalDao.updateGoal(goal)
        }
    }

    /**
     * Deletes a specific goal from the database.
     *
     * @param goal The goal to be removed.
     */
    fun deleteGoal(goal: Goal) {
        viewModelScope.launch(Dispatchers.IO) {
            goalDao.deleteGoal(goal)
        }
    }

    /**
     * Clears all goal entries.
     * Useful for reset features, test cases, or admin tools.
     */
    fun clearAllGoals() {
        viewModelScope.launch(Dispatchers.IO) {
            goalDao.clearAll()
        }
    }
}
