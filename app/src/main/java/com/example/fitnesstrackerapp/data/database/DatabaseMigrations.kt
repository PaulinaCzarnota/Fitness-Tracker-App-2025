/**
 * Database Migrations
 *
 * Responsibilities:
 * - Handles database schema updates
 * - Preserves user data during upgrades
 * - Maintains data integrity
 */

package com.example.fitnesstrackerapp.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add nutrition tracking tables
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS meal_entries (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                userId TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                mealType TEXT NOT NULL,
                totalCalories INTEGER NOT NULL,
                totalProtein REAL NOT NULL,
                totalCarbs REAL NOT NULL,
                totalFat REAL NOT NULL,
                notes TEXT
            )
        """)

        database.execSQL("""
            CREATE TABLE IF NOT EXISTS food_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                servingSize REAL NOT NULL,
                servingUnit TEXT NOT NULL,
                calories INTEGER NOT NULL,
                protein REAL NOT NULL,
                carbs REAL NOT NULL,
                fat REAL NOT NULL,
                fiber REAL,
                sugar REAL,
                isCustom INTEGER NOT NULL DEFAULT 0
            )
        """)

        // Add new columns to workouts table
        database.execSQL("""
            ALTER TABLE workouts
            ADD COLUMN distance REAL
        """)

        database.execSQL("""
            ALTER TABLE workouts
            ADD COLUMN steps INTEGER
        """)

        // Add goals table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS fitness_goals (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                userId TEXT NOT NULL,
                type TEXT NOT NULL,
                target REAL NOT NULL,
                currentProgress REAL NOT NULL DEFAULT 0.0,
                startDate INTEGER NOT NULL,
                endDate INTEGER NOT NULL,
                reminderFrequency TEXT NOT NULL,
                isCompleted INTEGER NOT NULL DEFAULT 0
            )
        """)

        // Create indices for better query performance
        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_workouts_userId_startTime 
            ON workouts(userId, startTime)
        """)

        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_meal_entries_userId_timestamp 
            ON meal_entries(userId, timestamp)
        """)

        database.execSQL("""
            CREATE INDEX IF NOT EXISTS index_fitness_goals_userId_type 
            ON fitness_goals(userId, type)
        """)
    }
}
