package com.example.fitnesstrackerapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "",
    val type: String = "",
    val duration: Int = 0, // in minutes
    val distance: Float = 0f, // in kilometers
    val calories: Int = 0,
    val steps: Int = 0,
    val notes: String = "",
    val date: Date = Date(),
    val timestamp: Long = System.currentTimeMillis()
)
