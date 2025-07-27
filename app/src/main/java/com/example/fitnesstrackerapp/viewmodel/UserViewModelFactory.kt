package com.example.fitnesstrackerapp.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * UserViewModelFactory
 *
 * A factory class used by ViewModelProvider to create instances of
 * UserViewModel with a custom constructor (requires Application).
 *
 * This is necessary because ViewModelProvider by default only supports
 * ViewModels with empty constructors.
 *
 * Usage in Composable:
 * val viewModel: UserViewModel = viewModel(
 *     factory = UserViewModelFactory(applicationContext as Application)
 * )
 *
 * @param application Android Application context (used for Room database access).
 */
class UserViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

    /**
     * Creates an instance of the given ViewModel class.
     *
     * @param modelClass Class of the ViewModel to create.
     * @return The instance of UserViewModel.
     * @throws IllegalArgumentException If the ViewModel class does not match.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            UserViewModel(application) as T
        } else {
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
