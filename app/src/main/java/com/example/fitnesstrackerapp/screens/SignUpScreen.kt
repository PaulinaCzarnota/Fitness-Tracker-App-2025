/**
 * Sign-up screen for the Fitness Tracker App.
 *
 * Provides a user interface for new users to register using email and password. Features input validation, password confirmation, secure password fields, loading and error states, and navigation to login. Implements Material Design 3 for accessibility and user experience.
 */
package com.example.fitnesstrackerapp.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.fitnesstrackerapp.R
import com.example.fitnesstrackerapp.ui.auth.AuthViewModel
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Top-level composable for the sign-up screen.
 *
 * Displays the registration form, handles user sign-up logic, and provides navigation to the login screen.
 *
 * @param navController NavController for navigation between screens.
 * @param onNavigateToLogin Callback to navigate to the login screen.
 * @param viewModel AuthViewModel that handles authentication logic.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SignUpScreen(
    navController: NavController,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel
) {
    // Local state
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    
    // UI state from ViewModel
    val authState by viewModel.uiState.collectAsStateWithLifecycle()

    // UI utilities
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    // Form validation
    val isEmailValid = email.isNotBlank() && 
                      android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val isPasswordValid = password.length >= 6
    val isFormValid = isEmailValid && isPasswordValid

    // Handle AuthState changes - navigation/error/snackbar
    LaunchedEffect(authState) {
        if (authState.isAuthenticated) {
            // Navigate to home and clear back stack
            navController.navigate("home") {
                popUpTo("register") { inclusive = true }
                popUpTo("login") { inclusive = true }
            }
        }

        authState.error?.let { errorMsg ->
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = errorMsg,
                    duration = SnackbarDuration.Short
                )
            }
            viewModel.clearError()
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Text(
                    text = stringResource(R.string.create_account),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = stringResource(R.string.create_account_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Email field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(R.string.email)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    singleLine = true,
                    isError = email.isNotBlank() && !isEmailValid
                )
                
                if (email.isNotBlank() && !isEmailValid) {
                    Text(
                        text = stringResource(R.string.invalid_email_format),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Password field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.password)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        val image = if (passwordVisible) {
                            Icons.Filled.Visibility
                        } else {
                            Icons.Filled.VisibilityOff
                        }
                        
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = image,
                                contentDescription = if (passwordVisible) {
                                    stringResource(R.string.hide_password)
                                } else {
                                    stringResource(R.string.show_password)
                                }
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            if (isFormValid) {
                                viewModel.register(email, password, "")
                            }
                        }
                    ),
                    singleLine = true,
                    isError = password.isNotBlank() && !isPasswordValid
                )
                
                if (password.isNotBlank() && !isPasswordValid) {
                    Text(
                        text = stringResource(R.string.password_min_length, 6),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Sign up button
                val loading = authState.isLoading
                Button(
                    onClick = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        viewModel.register(email, password, "")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = isFormValid && !loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.sign_up).uppercase(Locale.getDefault()),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Already have an account? Login
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.already_have_account),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    TextButton(
                        onClick = onNavigateToLogin,
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.login).uppercase(Locale.getDefault()),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                
                // Terms and privacy policy
                Text(
                    text = stringResource(R.string.terms_and_privacy_policy),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}
