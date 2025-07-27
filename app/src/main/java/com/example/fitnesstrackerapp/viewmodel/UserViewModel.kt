package com.example.fitnesstrackerapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitnesstrackerapp.data.FitnessDatabase
import com.example.fitnesstrackerapp.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * UserViewModel
 *
 * ViewModel responsible for handling user login and registration
 * using Room database and exposing methods to the UI layer.
 *
 * It uses coroutines to perform operations asynchronously,
 * keeping the UI thread unblocked.
 */
class UserViewModel(application: Application) : AndroidViewModel(application) {

    // Get instance of UserDao from the singleton Room database
    private val userDao = FitnessDatabase.getDatabase(application).userDao()

    /**
     * Register a new user by inserting into the Room database.
     *
     * @param email Email entered by the user.
     * @param password Password entered by the user.
     * @param onResult Callback returning true if registration successful, false if user exists.
     */
    fun register(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Trim inputs and create new User object
                val newUser = User(email = email.trim(), password = password.trim())

                // Attempt to insert user (may fail on duplicate due to unique constraint)
                userDao.insertUser(newUser)

                // Notify success on main thread
                withContext(Dispatchers.Main) {
                    onResult(true)
                }
            } catch (e: Exception) {
                // Notify failure (user already exists or DB error)
                withContext(Dispatchers.Main) {
                    onResult(false)
                }
            }
        }
    }

    /**
     * Authenticate an existing user by verifying email and password.
     *
     * @param email Email entered by the user.
     * @param password Password entered by the user.
     * @param onResult Callback returning true if login successful, false otherwise.
     */
    fun login(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            // Query the database for a user with matching credentials
            val user = userDao.authenticate(email.trim(), password.trim())

            // Return the result on main thread
            withContext(Dispatchers.Main) {
                onResult(user != null)
            }
        }
    }
}
