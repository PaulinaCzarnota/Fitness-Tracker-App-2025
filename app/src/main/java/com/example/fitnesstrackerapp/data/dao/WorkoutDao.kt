package com.example.fitnesstrackerapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.fitnesstrackerapp.data.entity.Workout
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    
    @Insert
    suspend fun insertWorkout(workout: Workout): Long
    
    @Update
    suspend fun updateWorkout(workout: Workout)
    
    @Delete
    suspend fun deleteWorkout(workout: Workout)
    
    @Query("SELECT * FROM workouts WHERE userId = :userId ORDER BY date DESC")
    fun getWorkoutsByUser(userId: String): Flow<List<Workout>>
    
    @Query("SELECT * FROM workouts WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getWorkoutsByDateRange(userId: String, startDate: Long, endDate: Long): Flow<List<Workout>>
    
    @Query("SELECT SUM(calories) FROM workouts WHERE userId = :userId")
    suspend fun getTotalCaloriesBurned(userId: String): Int?
    
    @Query("SELECT SUM(steps) FROM workouts WHERE userId = :userId")
    suspend fun getTotalSteps(userId: String): Int?
    
    @Query("SELECT SUM(duration) FROM workouts WHERE userId = :userId")
    suspend fun getTotalDuration(userId: String): Int?
}
