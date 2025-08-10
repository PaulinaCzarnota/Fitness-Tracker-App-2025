package com.example.fitnesstrackerapp.data.entity

/**
 * Goal Status enumeration for the Fitness Tracker application.
 *
 * This file defines the lifecycle states of fitness goals, providing a comprehensive
 * status tracking system for user objectives. Each status represents a distinct
 * phase in the goal lifecycle with specific behaviors and UI representations.
 *
 * Usage:
 * - Goal lifecycle management
 * - Progress tracking and reporting
 * - UI state representation
 * - Notification and reminder logic
 *
 * Each status represents a different phase in the goal lifecycle:
 * - ACTIVE: Goal is currently being pursued
 * - COMPLETED: Goal has been successfully achieved
 * - PAUSED: Goal is temporarily suspended but can be resumed
 * - CANCELLED: Goal has been permanently abandoned
 * - OVERDUE: Goal has passed its target date without completion
 */
enum class GoalStatus {
    ACTIVE, // Goal is currently being pursued and tracked
    COMPLETED, // Goal has been successfully achieved
    PAUSED, // Goal is temporarily suspended but can be resumed
    CANCELLED, // Goal has been permanently abandoned by user
    OVERDUE, // Goal has passed target date without completion
}
