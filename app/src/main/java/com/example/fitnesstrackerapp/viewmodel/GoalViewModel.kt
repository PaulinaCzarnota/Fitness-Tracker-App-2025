package com.example.fitnesstrackerapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstrackerapp.data.Goal
import com.example.fitnesstrackerapp.data.GoalDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * GoalViewModel
 *
 * A lifecycle-aware ViewModel that manages the business logic
 * and data operations for fitness goals.
 *
 * This ViewModel separates the UI from the data layer (Room DB)
 * and allows the Compose UI to observe goal updates via LiveData.
 *
 * @param goalDao DAO interface to interact with the goal table in Room.
 */
class GoalViewModel(private val goalDao: GoalDao) : ViewModel() {

    /**
     * LiveData stream of all fitness goals in the database.
     * Automatically updates the UI when the data changes.
     */
    val allGoals: LiveData<List<Goal>> = goalDao.getAllGoals()

    /**
     * Inserts a new goal into the database.
     * Called when the user creates a new fitness goal.
     *
     * @param goal The Goal object to insert.
     */
    fun addGoal(goal: Goal) {
        viewModelScope.launch(Dispatchers.IO) {
            goalDao.insertGoal(goal)
        }
    }

    /**
     * Updates an existing goal, such as marking it as completed
     * or updating the current progress.
     *
     * @param goal The modified Goal object.
     */
    fun updateGoal(goal: Goal) {
        viewModelScope.launch(Dispatchers.IO) {
            goalDao.updateGoal(goal)
        }
    }

    /**
     * Deletes a single goal from the database.
     *
     * @param goal The Goal to delete.
     */
    fun deleteGoal(goal: Goal) {
        viewModelScope.launch(Dispatchers.IO) {
            goalDao.deleteGoal(goal)
        }
    }

    /**
     * Deletes all goals from the database.
     * Useful for reset actions or admin tools.
     */
    fun clearAllGoals() {
        viewModelScope.launch(Dispatchers.IO) {
            goalDao.clearAll()
        }
    }
}
