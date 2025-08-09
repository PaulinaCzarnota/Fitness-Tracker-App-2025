/**
 * Data entity classes for the Fitness Tracker application user management.
 *
 * This file contains the User entity and related enums for storing user profile data,
 * authentication information, and fitness-related preferences. All data is stored
 * using Room database with proper indexing for optimal performance.
 *
 * Key Components:
 * - User entity with comprehensive profile information
 * - Gender enum for user demographics
 * - ActivityLevel enum with BMR multipliers for calorie calculations
 * - Security features for account protection
 * - Helper methods for BMI, age calculations, and validation
 */

package com.example.fitnesstrackerapp.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Calendar
import java.util.Date

/**
 * Enumeration representing user gender options.
 *
 * Provides inclusive gender options while maintaining privacy through
 * PREFER_NOT_TO_SAY option for users who wish to keep this information private.
 */
enum class Gender {
    MALE,
    FEMALE,
    OTHER,
    PREFER_NOT_TO_SAY,
}

/**
 * Enumeration representing user activity levels for calorie calculation.
 *
 * Each activity level includes a multiplier value used with Basal Metabolic Rate (BMR)
 * to calculate daily calorie needs based on user's lifestyle and exercise habits.
 *
 * @property multiplier The multiplication factor for BMR to calculate daily calorie needs
 */
enum class ActivityLevel(val multiplier: Double) {
    SEDENTARY(1.2),
    LIGHTLY_ACTIVE(1.375),
    MODERATELY_ACTIVE(1.55),
    VERY_ACTIVE(1.725),
    EXTREMELY_ACTIVE(1.9),
}

/**
 * Entity representing a user in the Fitness Tracker application.
 *
 * This entity stores comprehensive user information including:
 * - Authentication credentials with security features
 * - Personal profile information for fitness calculations
 * - Preferences and settings for app customization
 * - Account status and security tracking
 * - Fitness goals and activity preferences
 *
 * Features:
 * - Secure credential storage with salt and hash
 * - Profile information for fitness calculations
 * - Account status tracking and security measures
 * - Customizable goals and preferences
 * - Privacy and notification settings
 */
@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["is_active"]),
        Index(value = ["is_account_locked"]),
    ],
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "username")
    val username: String,

    @ColumnInfo(name = "password_hash")
    val passwordHash: String,

    @ColumnInfo(name = "password_salt")
    val passwordSalt: String,

    @ColumnInfo(name = "first_name")
    val firstName: String? = null,

    @ColumnInfo(name = "last_name")
    val lastName: String? = null,

    @ColumnInfo(name = "date_of_birth")
    val dateOfBirth: Date? = null,

    @ColumnInfo(name = "height_cm")
    val heightCm: Float? = null,

    @ColumnInfo(name = "weight_kg")
    val weightKg: Float? = null,

    @ColumnInfo(name = "gender")
    val gender: Gender? = null,

    @ColumnInfo(name = "activity_level")
    val activityLevel: ActivityLevel = ActivityLevel.MODERATELY_ACTIVE,

    @ColumnInfo(name = "fitness_level")
    val fitnessLevel: String? = null, // beginner, intermediate, advanced

    @ColumnInfo(name = "registration_date")
    val registrationDate: Date = Date(),

    @ColumnInfo(name = "last_login")
    val lastLogin: Date? = null,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "is_account_locked")
    val isAccountLocked: Boolean = false,

    @ColumnInfo(name = "failed_login_attempts")
    val failedLoginAttempts: Int = 0,

    @ColumnInfo(name = "last_password_change")
    val lastPasswordChange: Date = Date(),

    @ColumnInfo(name = "profile_image_path")
    val profileImagePath: String? = null,

    @ColumnInfo(name = "notification_enabled")
    val notificationEnabled: Boolean = true,

    @ColumnInfo(name = "privacy_settings")
    val privacySettings: String? = null, // JSON string for privacy preferences

    @ColumnInfo(name = "created_at")
    val createdAt: Date = Date(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Date = Date(),

    @ColumnInfo(name = "daily_step_goal")
    val dailyStepGoal: Int = 10000,

    @ColumnInfo(name = "daily_calorie_goal")
    val dailyCalorieGoal: Int = 2000,

    @ColumnInfo(name = "use_metric_units")
    val useMetricUnits: Boolean = true,

    // Firebase Authentication fields
    @ColumnInfo(name = "firebase_uid")
    val firebaseUid: String? = null,

    @ColumnInfo(name = "is_email_verified")
    val isEmailVerified: Boolean = false,

    @ColumnInfo(name = "provider")
    val provider: String? = null, // "email", "google.com", etc.

    @ColumnInfo(name = "photo_url")
    val photoUrl: String? = null,
) {
    /**
     * Calculates the user's age based on date of birth.
     * @return Age in years, or null if date of birth is not set
     */
    fun getAge(): Int? {
        return dateOfBirth?.let { dob ->
            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

            calendar.time = dob
            val birthYear = calendar.get(Calendar.YEAR)
            val birthMonth = calendar.get(Calendar.MONTH)
            val birthDay = calendar.get(Calendar.DAY_OF_MONTH)

            var age = currentYear - birthYear
            if (currentMonth < birthMonth || (currentMonth == birthMonth && currentDay < birthDay)) {
                age--
            }
            age
        }
    }

    /**
     * Calculates the user's Body Mass Index (BMI).
     * @return BMI value, or null if height or weight is not set
     */
    fun getBMI(): Float? {
        return if (heightCm != null && weightKg != null && heightCm > 0) {
            val heightM = heightCm / 100f
            weightKg / (heightM * heightM)
        } else {
            null
        }
    }

    /**
     * Gets BMI category based on calculated BMI value.
     * @return String describing BMI category or null if BMI cannot be calculated
     */
    fun getBMICategory(): String? {
        return getBMI()?.let { bmi ->
            when {
                bmi < 18.5f -> "Underweight"
                bmi < 25f -> "Normal weight"
                bmi < 30f -> "Overweight"
                else -> "Obese"
            }
        }
    }

    /**
     * Calculates Basal Metabolic Rate (BMR) using Mifflin-St Jeor Equation.
     * @return BMR value in calories/day, or null if required data is missing
     */
    fun getBMR(): Float? {
        return if (weightKg != null && heightCm != null && gender != null) {
            val age = getAge() ?: return null
            when (gender) {
                Gender.MALE -> 10 * weightKg + 6.25f * heightCm - 5 * age + 5
                Gender.FEMALE -> 10 * weightKg + 6.25f * heightCm - 5 * age - 161
                else -> 10 * weightKg + 6.25f * heightCm - 5 * age - 78 // Average of male/female
            }
        } else {
            null
        }
    }

    /**
     * Calculates Total Daily Energy Expenditure (TDEE).
     * @return TDEE value in calories/day, or null if BMR cannot be calculated
     */
    fun getTDEE(): Float? {
        return getBMR()?.let { bmr ->
            (bmr * activityLevel.multiplier).toFloat()
        }
    }

    /**
     * Gets the user's full name.
     * @return Full name or empty string if names are not set
     */
    fun getFullName(): String {
        return when {
            firstName != null && lastName != null -> "$firstName $lastName"
            firstName != null -> firstName
            lastName != null -> lastName
            else -> ""
        }
    }

    /**
     * Checks if user profile is complete for fitness calculations.
     * @return true if essential profile data is available
     */
    fun isProfileComplete(): Boolean {
        return heightCm != null && weightKg != null && dateOfBirth != null && gender != null
    }

    /**
     * Checks if the account is in a valid state for login.
     * @return true if account can be used for login
     */
    fun canLogin(): Boolean {
        return isActive && !isAccountLocked
    }

    /**
     * Gets display weight based on user's unit preference.
     * @return Pair of (value, unit) or null if weight is not set
     */
    fun getDisplayWeight(): Pair<Float, String>? {
        return weightKg?.let { weight ->
            if (useMetricUnits) {
                Pair(weight, "kg")
            } else {
                Pair(weight * 2.20462f, "lbs")
            }
        }
    }

    /**
     * Gets display height based on user's unit preference.
     * @return Pair of (value, unit) or null if height is not set
     */
    fun getDisplayHeight(): Pair<Float, String>? {
        return heightCm?.let { height ->
            if (useMetricUnits) {
                Pair(height, "cm")
            } else {
                val inches = height / 2.54f
                val feet = (inches / 12).toInt()
                val remainingInches = inches % 12
                Pair(feet + remainingInches / 12, "ft")
            }
        }
    }
}
