package com.example.fitnesstrackerapp.ui.components

import android.app.Application
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.fitnesstrackerapp.viewmodel.UserViewModel
import com.example.fitnesstrackerapp.viewmodel.UserViewModelFactory

/**
 * LoginScreen
 *
 * Composable function for user login using email and password.
 * Validates input, displays errors, performs authentication,
 * and navigates to home screen on successful login.
 */
@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current

    // Inject UserViewModel using the factory so the class is actually used
    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(context.applicationContext as Application)
    )

    // --- State for form input ---
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // --- State for UI feedback ---
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // --- Layout for login form ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Login", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        // --- Email Input Field ---
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorMessage = null
            },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // --- Password Input Field ---
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = null
            },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- Error message ---
        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // --- Login Button ---
        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Please fill in all fields."
                    return@Button
                }

                isLoading = true

                // Call ViewModel to validate credentials
                userViewModel.login(email.trim(), password.trim()) { success ->
                    isLoading = false
                    if (success) {
                        saveLoginState(context, true)
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        errorMessage = "Invalid email or password."
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Logging in..." else "Login")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- Navigation to Register Screen ---
        TextButton(
            onClick = { navController.navigate("register") }
        ) {
            Text("Don't have an account? Register")
        }
    }
}

/**
 * saveLoginState
 *
 * Saves user login state using SharedPreferences.
 * Used for session management between app launches.
 */
fun saveLoginState(context: Context, isLoggedIn: Boolean) {
    context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        .edit()
        .putBoolean("logged_in", isLoggedIn)
        .apply()
}
