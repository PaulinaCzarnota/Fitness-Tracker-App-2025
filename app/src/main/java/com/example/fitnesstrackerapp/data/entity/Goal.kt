package com.example.fitnesstrackerapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val targetValue: Float = 0f,
    val currentValue: Float = 0f,
    val unit: String = "",
    val deadline: Date = Date(),
    val isCompleted: Boolean = false,
    val createdAt: Date = Date()
)
