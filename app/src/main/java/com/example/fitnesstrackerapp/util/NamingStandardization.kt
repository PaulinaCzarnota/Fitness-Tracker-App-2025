/**
 * Naming Standardization Utilities
 *
 * This file provides utilities and documentation for standardizing naming conventions
 * across the Fitness Tracker application codebase. It helps ensure consistent API
 * design and reduces confusion for developers.
 *
 * Key Standardization Rules:
 * 1. Use `userId` (not `uid`) for user identification
 * 2. Use `isSuccess` (not `isSuccessful`) for boolean success indicators
 * 3. Use consistent property naming across similar entities
 * 4. Deprecate old naming patterns while maintaining backwards compatibility
 */

package com.example.fitnesstrackerapp.util

/**
 * Standard naming conventions for the application.
 *
 * This object contains constants and utilities to ensure consistent naming
 * across the entire codebase.
 */
object NamingStandards {
    
    /**
     * Standard property names for user identification.
     */
    const val USER_ID = "userId"
    
    /**
     * Standard property names for success indicators.
     */
    const val IS_SUCCESS = "isSuccess"
    
    /**
     * Standard property names for completion indicators.
     */
    const val IS_COMPLETED = "isCompleted"
    
    /**
     * Standard property names for active state indicators.
     */
    const val IS_ACTIVE = "isActive"
    
    /**
     * Standard property names for enabled state indicators.
     */
    const val IS_ENABLED = "isEnabled"
}

/**
 * Deprecated naming patterns that should be migrated to standard forms.
 *
 * This object tracks deprecated naming patterns and provides migration guidance.
 */
@Suppress("unused")
object DeprecatedNaming {
    
    /**
     * @deprecated Use [NamingStandards.USER_ID] instead.
     * This ensures consistency across all user-related operations.
     */
    @Deprecated(
        message = "Use 'userId' instead for consistency across the application",
        replaceWith = ReplaceWith("userId"),
        level = DeprecationLevel.WARNING
    )
    const val UID = "uid"
    
    /**
     * @deprecated Use [NamingStandards.IS_SUCCESS] instead.
     * Shorter form is preferred for consistency with other boolean properties.
     */
    @Deprecated(
        message = "Use 'isSuccess' instead of 'isSuccessful' for consistency",
        replaceWith = ReplaceWith("isSuccess"),
        level = DeprecationLevel.WARNING
    )
    const val IS_SUCCESSFUL = "isSuccessful"
}

/**
 * Extension functions to help migrate from deprecated naming patterns.
 */

/**
 * Extension property for backwards compatibility with `isSuccessful` pattern.
 * 
 * @deprecated Use [isSuccess] property instead.
 */
@Deprecated(
    message = "Use 'isSuccess' property instead",
    replaceWith = ReplaceWith("isSuccess"),
    level = DeprecationLevel.WARNING
)
@get:Deprecated(
    message = "Use 'isSuccess' property instead",
    replaceWith = ReplaceWith("isSuccess"),
    level = DeprecationLevel.WARNING
)
val Boolean.isSuccessful: Boolean
    get() = this

/**
 * Utility class for tracking naming convention compliance.
 *
 * This class can be used in tests to ensure naming conventions are followed.
 */
class NamingConventionChecker {
    
    /**
     * Validates that a property name follows standard conventions.
     *
     * @param propertyName The name of the property to validate
     * @return A list of naming issues found, empty if compliant
     */
    fun validatePropertyName(propertyName: String): List<String> {
        val issues = mutableListOf<String>()
        
        when {
            propertyName == "uid" -> {
                issues.add("Use 'userId' instead of 'uid' for user identification")
            }
            propertyName == "isSuccessful" -> {
                issues.add("Use 'isSuccess' instead of 'isSuccessful' for boolean success indicators")
            }
            propertyName.startsWith("user_id") -> {
                issues.add("Use camelCase 'userId' instead of snake_case 'user_id'")
            }
            propertyName.startsWith("is_") -> {
                issues.add("Use camelCase for boolean properties (e.g., 'isActive' instead of 'is_active')")
            }
        }
        
        return issues
    }
    
    /**
     * Checks if a property name uses deprecated patterns.
     *
     * @param propertyName The name of the property to check
     * @return True if the property name uses deprecated patterns
     */
    fun isDeprecatedNaming(propertyName: String): Boolean {
        return propertyName in listOf(
            "uid",
            "isSuccessful",
            "user_id",
            "is_successful",
            "is_completed",
            "is_active",
            "is_enabled"
        )
    }
}

/**
 * Migration guidelines for standardizing naming conventions.
 *
 * This documentation provides step-by-step guidance for migrating
 * from deprecated naming patterns to standard conventions.
 */
object MigrationGuide {
    
    /**
     * Guidelines for migrating user identification properties.
     */
    const val USER_ID_MIGRATION = """
        Migration from 'uid' to 'userId':
        
        1. Search for all occurrences of 'uid' in your codebase
        2. Replace with 'userId' in:
           - Entity properties
           - Method parameters
           - Database column names (with migration)
           - API response fields
        3. Update database migrations to rename columns
        4. Update tests to use new naming
        
        Example:
        Before: data class User(val uid: Long, ...)
        After:  data class User(val userId: Long, ...)
    """
    
    /**
     * Guidelines for migrating success indicator properties.
     */
    const val SUCCESS_MIGRATION = """
        Migration from 'isSuccessful' to 'isSuccess':
        
        1. Search for all occurrences of 'isSuccessful' in your codebase
        2. Replace with 'isSuccess' in:
           - Entity properties
           - Method return types
           - Database column names (with migration)
           - API response fields
        3. Ensure backwards compatibility during transition period
        4. Update tests and documentation
        
        Example:
        Before: data class Result(val isSuccessful: Boolean, ...)
        After:  data class Result(val isSuccess: Boolean, ...)
    """
}
