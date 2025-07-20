package com.example.fitnesstrackerapp.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fitnesstrackerapp.data.FitnessDatabase
import com.example.fitnesstrackerapp.viewmodel.WorkoutViewModel

class WorkoutViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorkoutViewModel::class.java)) {
            val database = FitnessDatabase.getDatabase(application)
            val dao = database.workoutDao()
            @Suppress("UNCHECKED_CAST")
            return WorkoutViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
