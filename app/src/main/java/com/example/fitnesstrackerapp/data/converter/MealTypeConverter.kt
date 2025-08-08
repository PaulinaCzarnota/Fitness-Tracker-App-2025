package com.example.fitnesstrackerapp.data.converter

import androidx.room.TypeConverter
import com.example.fitnesstrackerapp.data.entity.MealType

class MealTypeConverter {
    @TypeConverter
    fun toMealType(value: String?): MealType? {
        return value?.let { MealType.valueOf(it) }
    }

    @TypeConverter
    fun fromMealType(mealType: MealType?): String? {
        return mealType?.name
    }
}
