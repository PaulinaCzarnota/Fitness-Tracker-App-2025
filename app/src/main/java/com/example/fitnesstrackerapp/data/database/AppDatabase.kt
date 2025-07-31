package com.example.fitnesstrackerapp.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.fitnesstrackerapp.data.converter.DateConverter
import com.example.fitnesstrackerapp.data.dao.FoodDao
import com.example.fitnesstrackerapp.data.dao.GoalDao
import com.example.fitnesstrackerapp.data.dao.WorkoutDao
import com.example.fitnesstrackerapp.data.entity.FoodEntry
import com.example.fitnesstrackerapp.data.entity.Goal
import com.example.fitnesstrackerapp.data.entity.Workout

@Database(
    entities = [Workout::class, Goal::class, FoodEntry::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun goalDao(): GoalDao
    abstract fun foodDao(): FoodDao
}
