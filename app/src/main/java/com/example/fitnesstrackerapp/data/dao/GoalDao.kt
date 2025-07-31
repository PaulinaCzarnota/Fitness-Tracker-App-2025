package com.example.fitnesstrackerapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.fitnesstrackerapp.data.entity.Goal
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    
    @Insert
    suspend fun insertGoal(goal: Goal): Long
    
    @Update
    suspend fun updateGoal(goal: Goal)
    
    @Delete
    suspend fun deleteGoal(goal: Goal)
    
    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY deadline ASC")
    fun getGoalsByUser(userId: String): Flow<List<Goal>>
    
    @Query("SELECT * FROM goals WHERE userId = :userId AND isCompleted = 0 ORDER BY deadline ASC")
    fun getActiveGoals(userId: String): Flow<List<Goal>>
    
    @Query("SELECT * FROM goals WHERE userId = :userId AND isCompleted = 1 ORDER BY deadline DESC")
    fun getCompletedGoals(userId: String): Flow<List<Goal>>
    
    @Query("UPDATE goals SET currentValue = :currentValue WHERE id = :goalId")
    suspend fun updateGoalProgress(goalId: Long, currentValue: Float)
}
