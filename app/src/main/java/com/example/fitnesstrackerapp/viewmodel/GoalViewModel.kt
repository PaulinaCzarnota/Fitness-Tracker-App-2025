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
 * ViewModel that handles all logic related to user goals in the Fitness Tracker App.
 * Communicates with [GoalDao] to manage Room database operations.
 * Exposes a [StateFlow] to keep the UI reactive and up to date.
 *
 * @param goalDao DAO interface for accessing goal-related data.
 */
class GoalViewModel(
    private val goalDao: GoalDao
) : ViewModel() {

    /**
     * A reactive stream of all goals in the database, observed by the UI.
     * Backed by Kotlin [StateFlow] for efficient recomposition in Jetpack Compose.
     */
    val allGoals: StateFlow<List<Goal>> = goalDao.getAllGoals()
        .stateIn(
            scope = viewModelScope, // Coroutine scope tied to this ViewModel's lifecycle
            started = SharingStarted.WhileSubscribed(5000), // Avoids leaks and unnecessary work
            initialValue = emptyList()
        )

    /**
     * Inserts a new goal entry into the Room database.
     *
     * @param goal The [Goal] object to be inserted.
     */
    fun addGoal(goal: Goal) {
        viewModelScope.launch(Dispatchers.IO) {
            goalDao.insertGoal(goal)
        }
    }

    /**
     * Updates an existing goal entry.
     * Use this to modify description, progress, or completion status.
     *
     * @param goal The [Goal] entity with updated values.
     */
    fun updateGoal(goal: Goal) {
        viewModelScope.launch(Dispatchers.IO) {
            goalDao.updateGoal(goal)
        }
    }

    /**
     * Deletes a goal from the database.
     * Usually triggered when a user removes a goal manually.
     *
     * @param goal The [Goal] object to delete.
     */
    fun deleteGoal(goal: Goal) {
        viewModelScope.launch(Dispatchers.IO) {
            goalDao.deleteGoal(goal)
        }
    }

    /**
     * Deletes all goal entries from the database.
     * Use with caution — this operation is irreversible.
     */
    fun clearAllGoals() {
        viewModelScope.launch(Dispatchers.IO) {
            goalDao.clearAll()
        }
    }
}
