package com.example.fitnesstrackerapp.ui.components

import android.app.Application
import android.widget.Toast
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
 * RegisterScreen
 *
 * A Jetpack Compose screen that allows new users to register by entering
 * an email and password. Uses ViewModel to insert the new user into
 * the Room database. Displays input validation and feedback messages.
 *
 * @param navController NavController for navigation flow.
 */
@Composable
fun RegisterScreen(navController: NavController) {
    val context = LocalContext.current

    // Use the ViewModel factory to avoid unused warning
    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(context.applicationContext as Application)
    )

    // --- Form input states ---
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // --- UI feedback state ---
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // --- Screen layout ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Register", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        // --- Email input field ---
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorMessage = null
            },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // --- Password input field ---
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = null
            },
            label = { Text("Password") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- Error message ---
        errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // --- Register button ---
        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Please fill in all fields."
                    return@Button
                }

                isLoading = true

                // Call ViewModel to register the user
                userViewModel.register(email.trim(), password.trim()) { success ->
                    isLoading = false
                    if (success) {
                        Toast.makeText(context, "Registered successfully!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack() // Navigate back to login
                    } else {
                        errorMessage = "Registration failed. Email may already be in use."
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Registering..." else "Register")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- Link to login screen ---
        TextButton(onClick = { navController.popBackStack() }) {
            Text("Already have an account? Login")
        }
    }
}
