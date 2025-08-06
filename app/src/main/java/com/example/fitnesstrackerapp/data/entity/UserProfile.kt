/**
 * User Profile Entity
 *
 * Represents user profile information for the fitness tracker.
 * Maps to the user_profiles table in the Room database.
 */
package com.example.fitnesstrackerapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val height: Double? = null, // in cm
    val weight: Double? = null, // in kg
    val age: Int? = null,
    val gender: String? = null,
    val activityLevel: String = "moderate",
    val dailyStepGoal: Int = 10000,
    val dailyCalorieGoal: Int = 2000,
    val useMetricUnits: Boolean = true,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date()
)
