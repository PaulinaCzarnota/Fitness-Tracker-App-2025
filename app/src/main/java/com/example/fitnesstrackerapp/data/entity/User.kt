package com.example.fitnesstrackerapp.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Enum for user's gender.
 */
enum class Gender {
    MALE,
    FEMALE,
    OTHER,
    PREFER_NOT_TO_SAY
}

/**
 * Enum for user's activity level.
 *
 * @property multiplier The value to multiply with BMR to get daily calorie needs.
 */
enum class ActivityLevel(val multiplier: Double) {
    SEDENTARY(1.2),
    LIGHTLY_ACTIVE(1.375),
    MODERATELY_ACTIVE(1.55),
    VERY_ACTIVE(1.725),
    EXTREMELY_ACTIVE(1.9)
}

/**
 * Entity representing a user in the Fitness Tracker application.
 *
 * Features:
 * - Secure credential storage
 * - Profile information
 * - Account status tracking
 * - Security measures (account locking, login attempts)
 */
@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["isActive"]),
        Index(value = ["isAccountLocked"])
    ]
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
    val activityLevel: ActivityLevel? = null,

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
    val updatedAt: Date = Date()
) {
    /**
     * Calculates the user's age based on date of birth.
     * @return Age in years, or null if date of birth is not set
     */
    fun getAge(): Int? {
        return dateOfBirth?.let { dob ->
            val now = Date()
            val diffInMillis = now.time - dob.time
            val ageInYears = diffInMillis / (365.25 * 24 * 60 * 60 * 1000)
            ageInYears.toInt()
        }
    }

    /**
     * Calculates the user's BMI (Body Mass Index).
     * @return BMI value, or null if height or weight is not set
     */
    fun getBMI(): Float? {
        return if (heightCm != null && weightKg != null && heightCm > 0) {
            val heightInMeters = heightCm / 100f
            weightKg / (heightInMeters * heightInMeters)
        } else {
            null
        }
    }

    /**
     * Gets the user's full name.
     * @return Full name combining first and last name, or username if names not available
     */
    fun getFullName(): String {
        return when {
            firstName != null && lastName != null -> "$firstName $lastName"
            firstName != null -> firstName
            lastName != null -> lastName
            else -> username
        }
    }

    /**
     * Checks if the account needs password reset (90+ days since last change).
     * @return true if password should be reset
     */
    fun shouldResetPassword(): Boolean {
        val ninetyDaysAgo = Date(System.currentTimeMillis() - (90 * 24 * 60 * 60 * 1000L))
        return lastPasswordChange.before(ninetyDaysAgo)
    }

    /**
     * Checks if the account is locked due to failed login attempts.
     * @return true if account is locked
     */
    fun isLocked(): Boolean {
        return isAccountLocked || failedLoginAttempts >= MAX_FAILED_ATTEMPTS
    }

    companion object {
        const val MAX_FAILED_ATTEMPTS = 5

        const val FITNESS_LEVEL_BEGINNER = "beginner"
        const val FITNESS_LEVEL_INTERMEDIATE = "intermediate"
        const val FITNESS_LEVEL_ADVANCED = "advanced"
    }
}
