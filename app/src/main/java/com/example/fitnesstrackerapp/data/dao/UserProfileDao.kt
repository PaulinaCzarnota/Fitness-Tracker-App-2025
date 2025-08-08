/**
 * User Profile Data Access Object
 *
 * Provides database operations for user profile management.
 * Handles CRUD operations for user profile data.
 */

package com.example.fitnesstrackerapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.fitnesstrackerapp.data.entity.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    /**
     * Insert a new user profile
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userProfile: UserProfile): Long

    /**
     * Update an existing user profile
     */
    @Update
    suspend fun update(userProfile: UserProfile)

    /**
     * Delete a user profile
     */
    @Delete
    suspend fun delete(userProfile: UserProfile)

    /**
     * Get user profile by user ID
     */
    @Query("SELECT * FROM user_profiles WHERE userId = :userId LIMIT 1")
    suspend fun getByUserId(userId: Long): UserProfile?

    /**
     * Get user profile by user ID as Flow
     */
    @Query("SELECT * FROM user_profiles WHERE userId = :userId LIMIT 1")
    fun getByUserIdFlow(userId: Long): Flow<UserProfile?>

    /**
     * Update user height
     */
    @Query("UPDATE user_profiles SET height = :height, updatedAt = :updatedAt WHERE userId = :userId")
    suspend fun updateHeight(userId: Long, height: Double, updatedAt: java.util.Date)

    /**
     * Update user weight
     */
    @Query("UPDATE user_profiles SET weight = :weight, updatedAt = :updatedAt WHERE userId = :userId")
    suspend fun updateWeight(userId: Long, weight: Double, updatedAt: java.util.Date)

    /**
     * Update daily step goal
     */
    @Query("UPDATE user_profiles SET dailyStepGoal = :stepGoal, updatedAt = :updatedAt WHERE userId = :userId")
    suspend fun updateStepGoal(userId: Long, stepGoal: Int, updatedAt: java.util.Date)

    /**
     * Update metric units preference
     */
    @Query("UPDATE user_profiles SET useMetricUnits = :useMetric, updatedAt = :updatedAt WHERE userId = :userId")
    suspend fun updateMetricUnits(userId: Long, useMetric: Boolean, updatedAt: java.util.Date)
}
